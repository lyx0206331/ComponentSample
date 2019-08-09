package com.adrian.viewmodule.videoPlayer

import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.adrian.viewmodule.R
import kotlinx.android.synthetic.main.activity_video_view.*

class VideoViewActivity : AppCompatActivity() {

    private lateinit var controller: VideoViewController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_view)

        val videoPath = "${Environment.getExternalStorageDirectory().absolutePath}/video_9.mp4"
        videoView.setVideoPath(videoPath)
//        videoView.setVideoURI(Uri.fromFile(File(videoPath)))
//        val controller = MediaController(this)
//        videoView.setMediaController(controller)
//        val controller = VideoViewController(this)
//        videoView.controller = controller

        controller = VideoViewController(this).setParentContainer(videoParentContainer).setVideoView(videoView).setContentView(llConentView).build()

        ibPlayOrPause.setOnClickListener {
            videoView.start()
            ibPlayOrPause.visibility = View.GONE
        }
//        ibRotation.setOnClickListener {
//            val isVerticle = if (it.tag == null) true else it.tag as Boolean
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.onDestroy()
    }
}
