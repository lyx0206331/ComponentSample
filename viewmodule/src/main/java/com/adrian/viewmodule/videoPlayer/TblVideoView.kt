package com.adrian.viewmodule.videoPlayer

import android.app.Activity
import android.content.Context
import android.database.ContentObserver
import android.media.AudioManager
import android.os.Handler
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

class VideoViewController @JvmOverloads constructor(
    val context: Activity
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

    private var videoView: VideoView? = null

    private var contentView: View? = null

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
    var isSimpleMode = false
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
            //            val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            am.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                if (isChecked) /*AudioManager.ADJUST_MUTE*/ 0 else initVolume,
                /*AudioManager.FLAG_PLAY_SOUND*/0
            )
        }

        ibOrientation.setOnClickListener {
            //            isVertical = context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
//            context.requestedOrientation =
//                if (isVertical) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        sbVideoProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            var isTouch = false

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (isTouch) {
                    val playPosition = videoView?.duration ?: 0 * progress / MAX
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

        videoView?.setOnPlayingListener(object : VideoView.OnPlayingListener {
            override fun onPlaying(position: Int, duration: Int) {
                Log.e("VIDEOVIEW", "position:$position duration:$duration")
                curPlayTime = position
                totalDuration = duration
                sbVideoProgress.progress = position * MAX / duration
            }
        })

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
        return this
    }

    fun setVideoView(videoView: VideoView): VideoViewController {
        this.videoView = videoView
        return this
    }

    fun setParentContainer(container: RelativeLayout): VideoViewController {
        this.parentContainer = container
        return this
    }

    fun setContentView(contentView: View): VideoViewController {
        this.contentView = contentView
        return this
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