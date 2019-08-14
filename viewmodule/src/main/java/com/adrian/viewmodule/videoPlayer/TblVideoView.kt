package com.adrian.viewmodule.videoPlayer

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.database.ContentObserver
import android.graphics.SurfaceTexture
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.*
import com.adrian.viewmodule.R
import java.io.IOException

/**
 * date:2019/8/5 14:09
 * author:RanQing
 * description:
 */

@SuppressLint("NewApi")
class TblVideoView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) :
    TextureView(context, attrs, defStyle), MediaController.MediaPlayerControl {

    companion object {

        private val TAG = "info"

        // all possible internal states
        private val STATE_ERROR = -1
        private val STATE_IDLE = 0
        private val STATE_PREPARING = 1
        private val STATE_PREPARED = 2
        private val STATE_PLAYING = 3
        private val STATE_PAUSED = 4
        private val STATE_PLAYBACK_COMPLETED = 5
    }

    // currentState is a VideoView1 object's current state.
    // targetState is the state that a method caller intends to reach.
    // For instance, regardless the VideoView1 object's current state,
    // calling pause() intends to bring the object to a target state
    // of STATE_PAUSED.
    private var mCurrentState = STATE_IDLE
    private var mTargetState = STATE_IDLE

    // Stuff we need for playing and showing a video
    private var mMediaPlayer: MediaPlayer? = null
    private var mVideoWidth: Int = 0
    private var mVideoHeight: Int = 0
    private var mSurfaceWidth: Int = 0
    private var mSurfaceHeight: Int = 0
    private var mSurfaceTexture: SurfaceTexture? = null
    private var mSurface: Surface? = null
    private var mMediaController: MediaController? = null
    private var mOnCompletionListener: MediaPlayer.OnCompletionListener? = null
    private var mOnPreparedListener: MediaPlayer.OnPreparedListener? = null

    private var mOnErrorListener: MediaPlayer.OnErrorListener? = null
    private var mOnInfoListener: MediaPlayer.OnInfoListener? = null

    private var mSeekWhenPrepared: Int = 0 // recording the seek position while
    // preparing
    private var mCurrentBufferPercentage: Int = 0
    private var mAudioSession: Int = 0
    var uri: Uri? = null

    private val isInPlaybackState: Boolean
        get() = mMediaPlayer != null && mCurrentState != STATE_ERROR && mCurrentState != STATE_IDLE && mCurrentState != STATE_PREPARING

    // Listeners
    private val mBufferingUpdateListener = MediaPlayer.OnBufferingUpdateListener { mp, percent ->
        mCurrentBufferPercentage = percent

        Log.d("info", "----------$percent")
    }

    private val mCompleteListener = MediaPlayer.OnCompletionListener { mp ->
        mCurrentState = STATE_PLAYBACK_COMPLETED
        mTargetState = STATE_PLAYBACK_COMPLETED
        mSurface?.release()

        mMediaController?.hide()

        mOnCompletionListener?.onCompletion(mp)

        mMediaControllListener?.onComplete()
    }

    private val mPreparedListener = MediaPlayer.OnPreparedListener { mp ->
        mCurrentState = STATE_PREPARED

        mOnPreparedListener?.onPrepared(mMediaPlayer)

        mMediaController?.isEnabled = true


        mVideoWidth = mp.videoWidth
        mVideoHeight = mp.videoHeight

        val seekToPosition = mSeekWhenPrepared // mSeekWhenPrepared may be
        // changed after seekTo()
        // call
        if (seekToPosition != 0) {
            seekTo(seekToPosition)
        }

        requestLayout()
        invalidate()
        if (mVideoWidth != 0 && mVideoHeight != 0) {
            if (mTargetState == STATE_PLAYING) {
                mMediaPlayer?.start()
                mMediaControllListener?.onStart()
            }
        } else {
            if (mTargetState == STATE_PLAYING) {
                mMediaPlayer?.start()
                mMediaControllListener?.onStart()
            }
        }
    }

    private val mVideoSizeChangedListener = MediaPlayer.OnVideoSizeChangedListener { mp, width, height ->
        mVideoWidth = mp.videoWidth
        mVideoHeight = mp.videoHeight
        if (mVideoWidth != 0 && mVideoHeight != 0) {
            requestLayout()
        }
    }

    private val mErrorListener = MediaPlayer.OnErrorListener { mp, what, extra ->
        Log.d(TAG, "Error: $what,$extra")
        mCurrentState = STATE_ERROR
        mTargetState = STATE_ERROR

        mMediaController?.hide()

        /* If an error handler has been supplied, use it and finish. */
        if (mOnErrorListener?.onError(mMediaPlayer, what, extra) == true) {
            return@OnErrorListener true
        }

        /*
             * Otherwise, pop up an error dialog so the user knows that
             * something bad has happened. Only try and pop up the dialog if
             * we're attached to a window. When we're going away and no longer
             * have a window, don't bother showing the user an error.
             */
        if (windowToken != null) {
        }
        true
    }

    internal var mSurfaceTextureListener: TextureView.SurfaceTextureListener =
        object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                Log.d(TAG, "onSurfaceTextureAvailable.")
                mSurfaceTexture = surface
                openVideo()
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
                Log.d(TAG, "onSurfaceTextureSizeChanged: $width/$height")
                mSurfaceWidth = width
                mSurfaceHeight = height
                val isValidState = mTargetState == STATE_PLAYING
                val hasValidSize = mVideoWidth == width && mVideoHeight == height
                if (mMediaPlayer != null && isValidState && hasValidSize) {
                    if (mSeekWhenPrepared != 0) {
                        seekTo(mSeekWhenPrepared)
                    }
                    start()
                }
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {

                mSurface = null
                mMediaController?.hide()

                release(true)
                return true
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
                playingListener?.onPlaying(currentPosition, duration)
            }
        }

    internal var mMediaControllListener: MediaControllListener? = null

    internal var playingListener: OnPlayingListener? = null

    init {
        initVideoView()
    }

    private fun initVideoView() {
        mVideoHeight = 0
        mVideoWidth = 0
        //        setBackgroundColor(getResources().getColor(android.R.color.transparent));
        isFocusable = false
        surfaceTextureListener = mSurfaceTextureListener
    }

    fun resolveAdjustedSize(desiredSize: Int, measureSpec: Int): Int {
        var result = desiredSize
        val specMode = View.MeasureSpec.getMode(measureSpec)
        val specSize = View.MeasureSpec.getSize(measureSpec)

        when (specMode) {
            View.MeasureSpec.UNSPECIFIED ->
                /*
                 * Parent says we can be as big as we want. Just don't be larger
                 * than max size imposed on ourselves.
                 */
                result = desiredSize

            View.MeasureSpec.AT_MOST ->
                /*
                 * Parent says we can be as big as we want, up to specSize. Don't be
                 * larger than specSize, and don't be larger than the max size
                 * imposed on ourselves.
                 */
                result = Math.min(desiredSize, specSize)

            View.MeasureSpec.EXACTLY ->
                // No choice. Do what we are told.
                result = specSize
        }
        return result
    }

    fun setVideoPath(path: String) {
        Log.d(TAG, "Setting video path to: $path")
        setVideoURI(Uri.parse(path))
    }

    fun setVideoURI(_videoURI: Uri) {
        uri = _videoURI
        mSeekWhenPrepared = 0
        requestLayout()
        invalidate()
        openVideo()
    }

    override fun setSurfaceTexture(surfaceTexture: SurfaceTexture) {
        mSurfaceTexture = surfaceTexture
    }

    fun openVideo() {
        if (uri == null || mSurfaceTexture == null) {
            Log.d(TAG, "Cannot open video, uri or surface texture is null.")
            return
        }
        // Tell the music playback service to pause
        // TODO: these constants need to be published somewhere in the
        // framework.
        val i = Intent("com.android.music.musicservicecommand")
        i.putExtra("command", "pause")
        context.sendBroadcast(i)
        release(false)
        try {
            mSurface = Surface(mSurfaceTexture)
            mMediaPlayer = MediaPlayer()
            if (mAudioSession != 0) {
                mMediaPlayer?.audioSessionId = mAudioSession
            } else {
                mAudioSession = mMediaPlayer?.audioSessionId ?: 0
            }

            mMediaPlayer?.setOnBufferingUpdateListener(mBufferingUpdateListener)
            mMediaPlayer?.setOnCompletionListener(mCompleteListener)
            mMediaPlayer?.setOnPreparedListener(mPreparedListener)
            mMediaPlayer?.setOnErrorListener(mErrorListener)
            mMediaPlayer?.setOnInfoListener(mOnInfoListener)
            mMediaPlayer?.setOnVideoSizeChangedListener(mVideoSizeChangedListener)

            mMediaPlayer?.setSurface(mSurface)
            mCurrentBufferPercentage = 0
            mMediaPlayer?.setDataSource(context, uri!!)

            mMediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
            mMediaPlayer?.setScreenOnWhilePlaying(true)

            mMediaPlayer?.prepareAsync()
            mCurrentState = STATE_PREPARING
        } catch (e: IllegalStateException) {
            mCurrentState = STATE_ERROR
            mTargetState = STATE_ERROR
            Log.d(TAG, e.message) // TODO auto-generated catch block
        } catch (e: IOException) {
            mCurrentState = STATE_ERROR
            mTargetState = STATE_ERROR
            Log.d(TAG, e.message) // TODO auto-generated catch block
        }

    }

    fun stopPlayback() {
        if (mMediaPlayer != null) {
            mMediaPlayer?.stop()
            mMediaPlayer?.release()
            mMediaPlayer = null
            mMediaControllListener?.onStop()
        }
    }

    fun setMediaController(controller: MediaController) {
        mMediaController?.hide()
        mMediaController = controller
        attachMediaController()
    }

    private fun attachMediaController() {
        mMediaPlayer?.let {
            mMediaController?.setMediaPlayer(this)
            val anchorView = if (this.parent is View) this.parent as View else this
            mMediaController?.setAnchorView(anchorView)
            mMediaController?.isEnabled = isInPlaybackState
        }
    }

    private fun release(cleartargetstate: Boolean) {
        Log.d(TAG, "Releasing media player.")
        if (mMediaPlayer != null) {
            mMediaPlayer?.reset()
            mMediaPlayer?.release()
            mMediaPlayer = null
            mCurrentState = STATE_IDLE
            if (cleartargetstate) {
                mTargetState = STATE_IDLE
            }
        } else {
            Log.d(TAG, "Media player was null, did not release.")
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Will resize the view if the video dimensions have been found.
        // video dimensions are found after onPrepared has been called by
        // MediaPlayer
        var width = View.getDefaultSize(mVideoWidth, widthMeasureSpec)
        var height = View.getDefaultSize(mVideoHeight, heightMeasureSpec)
        if (mVideoWidth > 0 && mVideoHeight > 0) {
            if (mVideoWidth * height > width * mVideoHeight) {
                Log.d(TAG, "Video too tall, change size.")
                height = width * mVideoHeight / mVideoWidth
            } else if (mVideoWidth * height < width * mVideoHeight) {
                Log.d(TAG, "Video too wide, change size.")
                width = height * mVideoWidth / mVideoHeight
            } else {
                Log.d(TAG, "Aspect ratio is correct.")
            }
        }
        setMeasuredDimension(width, height)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (isInPlaybackState && mMediaController != null) {
            toggleMediaControlsVisiblity()
        }
        return false
    }

    override fun onTrackballEvent(ev: MotionEvent): Boolean {
        if (isInPlaybackState && mMediaController != null) {
            toggleMediaControlsVisiblity()
        }
        return false
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        val isKeyCodeSupported =
            (keyCode != KeyEvent.KEYCODE_BACK && keyCode != KeyEvent.KEYCODE_VOLUME_UP && keyCode != KeyEvent.KEYCODE_VOLUME_DOWN
                    && keyCode != KeyEvent.KEYCODE_VOLUME_MUTE && keyCode != KeyEvent.KEYCODE_MENU && keyCode != KeyEvent.KEYCODE_CALL
                    && keyCode != KeyEvent.KEYCODE_ENDCALL)
        if (isInPlaybackState && isKeyCodeSupported && mMediaController != null) {
            if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                if (mMediaPlayer?.isPlaying == true) {
                    pause()
                    mMediaController?.show()
                } else {
                    start()
                    mMediaController?.hide()
                }
                return true
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
                if (mMediaPlayer?.isPlaying != true) {
                    start()
                    mMediaController?.hide()
                }
                return true
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                if (mMediaPlayer?.isPlaying == true) {
                    pause()
                    mMediaController?.show()
                }
                return true
            } else {
                toggleMediaControlsVisiblity()
            }
        }

        return super.onKeyDown(keyCode, event)
    }

    private fun toggleMediaControlsVisiblity() {
        if (mMediaController?.isShowing == true) {
            mMediaController?.hide()
        } else {
            mMediaController?.show()
        }
    }

    override fun start() {
        // This can potentially be called at several points, it will go through
        // when all conditions are ready
        // 1. When setting the video URI
        // 2. When the surface becomes available
        // 3. From the activity
        if (isInPlaybackState) {
            mMediaPlayer?.start()
            mCurrentState = STATE_PLAYING
            mMediaControllListener?.onStart()
        } else {
            Log.d(TAG, "Could not start. Current state $mCurrentState")
        }
        mTargetState = STATE_PLAYING
    }

    override fun pause() {
        if (isInPlaybackState) {
            if (mMediaPlayer?.isPlaying == true) {
                mMediaPlayer?.pause()
                mCurrentState = STATE_PAUSED
                mMediaControllListener?.onPause()
            }
        }
        mTargetState = STATE_PAUSED
    }

    fun suspend() {
        release(false)
    }

    fun resume() {
        openVideo()
    }

    override fun getDuration(): Int {
        return if (isInPlaybackState) {
            mMediaPlayer?.duration ?: 0
        } else -1
    }

    override fun getCurrentPosition(): Int {
        return if (isInPlaybackState) {
            mMediaPlayer?.currentPosition ?: 0
        } else 0
    }

    override fun seekTo(msec: Int) {
        mSeekWhenPrepared = if (isInPlaybackState) {
            mMediaPlayer!!.seekTo(msec)
            0
        } else {
            msec
        }
    }

    override fun isPlaying(): Boolean {
        return isInPlaybackState && (mMediaPlayer?.isPlaying == true)
    }

    override fun getBufferPercentage(): Int {
        return if (mMediaPlayer != null) {
            mCurrentBufferPercentage
        } else 0
    }

    override fun canPause(): Boolean {
        return false
    }

    override fun canSeekBackward(): Boolean {
        return false
    }

    override fun canSeekForward(): Boolean {
        return false
    }

    override fun getAudioSessionId(): Int {
        if (mAudioSession == 0) {
            val foo = MediaPlayer()
            mAudioSession = foo.audioSessionId
            foo.release()
        }
        return mAudioSession
    }

    /**
     * Register a callback to be invoked when the media file is loaded and ready
     * to go.
     *
     * @param l The callback that will be run
     */
    fun setOnPreparedListener(l: MediaPlayer.OnPreparedListener) {
        mOnPreparedListener = l
    }

    /**
     * Register a callback to be invoked when the end of a media file has been
     * reached during playback.
     *
     * @param l The callback that will be run
     */
    fun setOnCompletionListener(l: MediaPlayer.OnCompletionListener) {
        mOnCompletionListener = l
    }

    /**
     * Register a callback to be invoked when an error occurs during playback or
     * setup. If no listener is specified, or if the listener returned false,
     * VideoView1 will inform the user of any errors.
     *
     * @param l The callback that will be run
     */
    fun setOnErrorListener(l: MediaPlayer.OnErrorListener) {
        mOnErrorListener = l
    }

    /**
     * Register a callback to be invoked when an informational event occurs
     * during playback or setup.
     *
     * @param l The callback that will be run
     */
    fun setOnInfoListener(l: MediaPlayer.OnInfoListener) {
        mOnInfoListener = l
    }

    fun setOnPlayingListener(onPlayingListener: OnPlayingListener) {
        this.playingListener = onPlayingListener
    }

    interface MediaControllListener {
        fun onStart()

        fun onPause()

        fun onStop()

        fun onComplete()
    }

    fun setMediaControllListener(mediaControllListener: MediaControllListener) {
        mMediaControllListener = mediaControllListener
    }

    override fun setVisibility(visibility: Int) {
        println("setVisibility: $visibility")
        super.setVisibility(visibility)
    }

    interface OnPlayingListener {
        fun onPlaying(position: Int, duration: Int)
    }
}

class VideoViewController @JvmOverloads constructor(
    val context: Context
) {

    companion object {
        const val MAX = 100
    }

    /** 控制器 */
    private var controller: View = LayoutInflater.from(context).inflate(R.layout.layout_video_controller, null, false)
    /** 父容器 */
    private var parentContainer: RelativeLayout? = null

    /** 声音开关 */
    private var cbSoundSwitch: CheckBox
    /** 当前播放时间 */
    private var tvCurTime: TextView
    /** 视频进度条 */
    private var sbVideoProgress: SeekBar
    /** 视频总时长 */
    private var tvDuration: TextView
    /** 横竖屏切换 */
    private var ibOrientation: ImageButton
    /** title */
    private var titleView: View? = null
    /** 播放器 */
    private var videoView: TblVideoView? = null
    /** 播放暂停按钮 */
    private var ibPlayOrPause: ImageButton? = null
    /** 额外内容 */
    private var contentView: View? = null
    /** 加载缓冲进度条 */
    private var loadingView: FrameLayout? = null
    /** 是否自动重播 */
    var isAutoReplay = false
    /** 方向改变监听 */
    var onOrientationChangeListener: ((isVertical: Boolean) -> Unit)? = null

    /** 声音是否关闭,默认未关闭 */
    private var isSoundClose = false
    /** 是否竖屏,默认竖屏.必须Manifest中配置configChanges="orientation|screenSize|keyboardHidden"才生效 */
    var isVertical = true
        set(value) {
            field = value
            isSimpleMode = !field
            contentView?.visibility = if (isVertical) View.VISIBLE else View.GONE
            onOrientationChangeListener?.invoke(field)
        }
        get() = context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    /** 当前播放时长 */
    private var curPlayTime = 0
        set(value) {
            field = value
            tvCurTime.text = formatTime(value)
        }
    /** 视频总时长 */
    private var totalDuration = 0
        set(value) {
            field = value
            tvDuration.text = formatTime(value)
        }

    /** 是否简单模式,简单模式下音量开关、时间、进度条指示器及横竖屏切换键隐藏 */
    private var isSimpleMode = false
        set(value) {
            field = value
            if (value) {
                cbSoundSwitch.visibility = View.GONE
                tvCurTime.visibility = View.GONE
                tvDuration.visibility = View.GONE
                ibOrientation.visibility = View.GONE
                sbVideoProgress.thumb = null
                sbVideoProgress.isFocusableInTouchMode = false
                sbVideoProgress.visibility = View.VISIBLE
                sbVideoProgress.isEnabled = true
                contentView?.visibility = View.GONE
                val lp = controller.layoutParams as RelativeLayout.LayoutParams
                lp.setMargins(0, 0, 0, 0)
            } else {
                cbSoundSwitch.visibility = View.VISIBLE
                tvCurTime.visibility = View.VISIBLE
                tvDuration.visibility = View.VISIBLE
                ibOrientation.visibility = View.VISIBLE
                sbVideoProgress.thumb = context.resources.getDrawable(R.drawable.shape_video_seekbar_thumb)
                sbVideoProgress.isFocusableInTouchMode = true
                contentView?.visibility = View.VISIBLE
                val lp = controller.layoutParams as RelativeLayout.LayoutParams
                lp.setMargins(0, 0, 0, 450)
            }
        }

    /** 设置音量观察者 */
    private val volumeObserver = SettingsContentObserver(Handler())

    private val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    /** 初始音量,会随音量键变化 */
    private var initVolume: Int

    init {

        cbSoundSwitch = controller.findViewById(R.id.cbSoundSwitch)
        tvCurTime = controller.findViewById(R.id.tvCurTime)
        sbVideoProgress = controller.findViewById(R.id.sbVideoProgress)
        tvDuration = controller.findViewById(R.id.tvDuration)
        ibOrientation = controller.findViewById(R.id.ibOrientation)

        sbVideoProgress.max = MAX
        sbVideoProgress.setPadding(0, 0, 0, 0)

        initVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC)

        isSoundClose = initVolume == 0
        cbSoundSwitch.isChecked = isSoundClose

        //注册音量调节监听
        context.contentResolver.registerContentObserver(
            android.provider.Settings.System.CONTENT_URI,
            true,
            volumeObserver
        )
    }

    private fun initListener() {
        cbSoundSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            isSoundClose = isChecked
            am.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                if (isChecked) 0 else initVolume, 0
            )
        }

        ibOrientation.setOnClickListener {
            changeOrientation()
        }

        sbVideoProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            var isTouch = false

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (isTouch) {
                    val playPosition = (videoView?.duration ?: 0) * progress / MAX
                    curPlayTime = playPosition
                    videoView?.seekTo(playPosition)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isTouch = true
                videoView?.pause()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isTouch = false
                videoView?.start()
            }
        })

        videoView?.setOnPlayingListener(object : TblVideoView.OnPlayingListener {
            override fun onPlaying(position: Int, duration: Int) {
//                Log.e("VIDEOVIEW", "position:$position duration:$duration")
                curPlayTime = position
                totalDuration = duration
                sbVideoProgress.progress = position * MAX / duration
            }
        })

        videoView?.setOnCompletionListener(MediaPlayer.OnCompletionListener {
            if (isAutoReplay) {
                it.start()
                it.isLooping = true
            }
        })

        videoView?.setOnInfoListener(MediaPlayer.OnInfoListener { mp, what, extra ->
            if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                loadingView?.visibility = View.VISIBLE
            } else {
                loadingView?.visibility = View.GONE
            }
            true
        })

        videoView?.setMediaControllListener(object : TblVideoView.MediaControllListener {
            override fun onStart() {
                ibPlayOrPause?.setImageResource(R.mipmap.ic_puase)
                changePlayPauseState(true)
            }

            override fun onPause() {
                ibPlayOrPause?.setImageResource(R.mipmap.ic_play)
                changePlayPauseState(false)
            }

            override fun onStop() {
            }

            override fun onComplete() {
            }
        })

        ibPlayOrPause?.setOnClickListener {
            if (controller.visibility == View.VISIBLE) {
                if (videoView?.isPlaying == true) {
                    videoView?.pause()
                } else {
                    videoView?.start()
                }
            } else {
                ibPlayOrPause?.alpha = 1f
                controller.visibility = View.VISIBLE
                sbVideoProgress.isEnabled = true
            }
        }

    }

    private fun changePlayPauseState(isPlaying: Boolean) {
        if (!isPlaying) {
//            ibPlayOrPause?.setImageResource(R.mipmap.ic_play)
            ibPlayOrPause?.alpha = 1f
            if (isVertical) {
                controller.visibility = View.VISIBLE
                sbVideoProgress.isEnabled = false
            }
        } else {
//            ibPlayOrPause?.setImageResource(R.mipmap.ic_puase)
            ibPlayOrPause?.alpha = 0f
            if (isVertical) {
                controller.visibility = View.GONE
                sbVideoProgress.isEnabled = true
            }
        }
    }

    /**
     * 改变方向
     */
    fun changeOrientation() {
        (context as Activity).requestedOrientation =
            if (isVertical) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    fun build(): VideoViewController {
        initListener()

        val controllerLp = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        controllerLp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        controller.layoutParams = controllerLp
        parentContainer?.addView(controller)
        isSimpleMode = false
        isVertical = true
        return this
    }

    /**
     * 设置播放器
     */
    fun setVideoView(videoView: TblVideoView): VideoViewController {
        this.videoView = videoView
        return this
    }

    /**
     * 设置父布局
     */
    fun setParentContainer(container: RelativeLayout): VideoViewController {
        this.parentContainer = container
        return this
    }

    /** 设置播放状态按钮 */
    fun setPlayStateButton(ibPlayOrPause: ImageButton): VideoViewController {
        this.ibPlayOrPause = ibPlayOrPause
        return this
    }

    /**
     * 设置标题栏
     */
    fun setTitleView(titleView: View, listener: View.OnClickListener): VideoViewController {
        this.titleView = titleView
        this.titleView?.setOnClickListener(listener)
        return this
    }

    /**
     * 界面其它内容
     */
    fun setContentView(contentView: View): VideoViewController {
        this.contentView = contentView
        return this
    }

    /**
     * 加载缓冲进度条
     */
    fun setLoadingView(loadingView: FrameLayout): VideoViewController {
        this.loadingView = loadingView
        return this
    }

    /**
     * 方向已改变,在[android.app.Activity.onConfigurationChanged]中声明
     */
    fun orientationChanged() {
        isVertical = context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    }

    /**
     * 格式化时间
     */
    private fun formatTime(time: Int): String {
        val total = time / 1000
        return when {
            total <= 0 -> "00:00"
            total < 3600 -> {
                val second = total % 60
                val minute = total / 60
                String.format("%02d:%02d", minute, second)
            }
            else -> {
                val second = total % 60
                val minute = total % 3600 / 60
                val hour = total / 3600
                String.format("%02d:%02d:%02d", hour, minute, second)
            }
        }
    }

    fun onDestroy() {
        am.setStreamVolume(AudioManager.STREAM_MUSIC, initVolume, 0)
        context.contentResolver.unregisterContentObserver(volumeObserver)
    }

    inner class SettingsContentObserver(handler: Handler) : ContentObserver(handler) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val curVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC)
            initVolume = if (curVolume > 0) curVolume else initVolume
//            Log.e("VOLUME", "curVolume=$curVolume initvolume=$initVolume")
            cbSoundSwitch.isChecked = curVolume == 0
        }
    }
}