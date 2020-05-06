package com.adrian.viewmodule.videoPlayer

import android.content.res.Configuration
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.adrian.viewmodule.R
import kotlinx.android.synthetic.main.item_player_layout.*

class VideoViewActivity : AppCompatActivity() {

    private lateinit var controller: VideoViewController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_view)

        val videoPath = "${Environment.getExternalStorageDirectory().absolutePath}/video_9.mp4"
        //http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4
        //http://www.w3school.com.cn/example/html5/mov_bbb.mp4
        //http://vfx.mtime.cn/Video/2019/02/04/mp4/190204084208765161.mp4
//        val videoPath = "http://vfx.mtime.cn/Video/2019/02/04/mp4/190204084208765161.mp4"
        videoView.setVideoPath(videoPath)
//        videoView.setVideoURI(Uri.fromFile(File(videoPath)))
//        val controller = MediaController(this)
//        videoView.setMediaController

//        val controller = VideoViewController(this)
//        videoView.controller = controller

        controller = VideoViewController(this).setParentContainer(videoParentContainer).setVideoView(videoView)
            .setContentView(llConentView).setPlayStateButton(ibPlayOrPause).setLoadingView(flLoading)
            .setTitleView(ibBack, View.OnClickListener {
                onBackPressed()
            }).build()

        controller.isAutoReplay = true

        controller.onOrientationChangeListener = {
            Log.e("ORIENTATION", "isVertical: $it")
        }

//        ibPlayOrPause.setOnClickListener {
//            videoView.start()
//            ibPlayOrPause.visibility = View.GONE
//        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        controller.orientationChanged()
    }

    override fun onBackPressed() {
        if (!controller.isVertical) {
            controller.changeOrientation()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.onDestroy()
    }
}
