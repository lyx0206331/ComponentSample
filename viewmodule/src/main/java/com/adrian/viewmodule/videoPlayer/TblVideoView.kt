package com.adrian.viewmodule.videoPlayer

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.database.ContentObserver
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.adrian.viewmodule.R

/**
 * date:2019/8/5 14:09
 * author:RanQing
 * description:
 */
class TblVideoView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    RelativeLayout(context, attrs, defStyleAttr) {

    var videoView: VideoView

    var controller: VideoViewController

    init {
        videoView = VideoView(context)
        val vvLp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        vvLp.addRule(CENTER_IN_PARENT)
        videoView.layoutParams = vvLp
        addView(videoView)

        controller = VideoViewController(context as Activity)
        val ctrlLp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        ctrlLp.addRule(ALIGN_PARENT_BOTTOM)
        controller.layoutParams = ctrlLp
        addView(controller)

        controller.videoView = videoView
    }

    fun setVideoPath(path: String) {
        videoView.setVideoPath(path)
        videoView.start()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
//        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE){
//            val params = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
//        }
    }
}

class VideoViewController @JvmOverloads constructor(
    val context: Activity,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    companion object {
        const val MAX = 100
    }

    var controller: View

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

    var videoView: VideoView? = null
        set(value) {
            field = value
            totalDuration = value?.duration ?: 0
            value?.setOnInfoListener(object : MediaPlayer.OnInfoListener {
                override fun onInfo(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
                    Log.e("PLAYER", "what:$what, extra:$extra")
                    return false
                }
            })
        }

    /** 声音是否关闭,默认未关闭 */
    private var isSoundClose = false
    /** 是否竖屏,默认竖屏 */
    private var isVertical = true
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
    var isSimleMode = false
        set(value) {
            field = value
            if (value) {
                cbSoundSwitch.visibility = View.GONE
                tvCurTime.visibility = View.GONE
                tvDuration.visibility = View.GONE
                ibOrientation.visibility = View.GONE
                sbVideoProgress.thumb = null
                sbVideoProgress.isFocusableInTouchMode = false
            } else {
                cbSoundSwitch.visibility = View.VISIBLE
                tvCurTime.visibility = View.VISIBLE
                tvDuration.visibility = View.VISIBLE
                ibOrientation.visibility = View.VISIBLE
                sbVideoProgress.thumb = context.resources.getDrawable(R.drawable.shape_video_seekbar_thumb)
                sbVideoProgress.isFocusableInTouchMode = true
            }
        }

    private val volumeObserver = SettingsContentObserver(Handler())

    private val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private var initVolume: Int

    init {
        controller = LayoutInflater.from(context).inflate(R.layout.layout_video_controller, this, true)

        cbSoundSwitch = controller.findViewById(R.id.cbSoundSwitch)
        tvCurTime = controller.findViewById(R.id.tvCurTime)
        sbVideoProgress = controller.findViewById(R.id.sbVideoProgress)
        tvDuration = controller.findViewById(R.id.tvDuration)
        ibOrientation = controller.findViewById(R.id.ibOrientation)

        sbVideoProgress.max = MAX
        sbVideoProgress.setPadding(0, 0, 0, 0)

        initVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC)

        cbSoundSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            isSoundClose = isChecked
//            val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            am.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                if (isChecked) AudioManager.ADJUST_MUTE else initVolume,
                AudioManager.FLAG_PLAY_SOUND
            )
        }
        ibOrientation.setOnClickListener {
            isVertical = !isVertical
            context.requestedOrientation =
                if (isVertical) ActivityInfo.SCREEN_ORIENTATION_PORTRAIT else ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        sbVideoProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val playProgress = videoView?.duration ?: 0 * progress / MAX
                curPlayTime = playProgress
                videoView?.seekTo(playProgress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        context.contentResolver.registerContentObserver(
            android.provider.Settings.System.CONTENT_URI,
            true,
            volumeObserver
        )
    }

    /**
     * 格式化时间
     */
    private fun formatTime(time: Int): String {
        return when {
            time <= 0 -> "00:00"
            time < 3600 -> {
                val second = time % 60
                val minute = time / 60
                String.format("%02d:%02d", minute, second)
            }
            else -> {
                val second = time % 60
                val minute = time % 3600 / 60
                val hour = time / 3600
                String.format("%02d:%02d:%02d", hour, minute, second)
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        context.contentResolver.unregisterContentObserver(volumeObserver)
    }

    inner class SettingsContentObserver(handler: Handler) : ContentObserver(handler) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val curVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC)
            Log.e("VOLUME", "volume=$curVolume")
        }
    }
}