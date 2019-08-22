package com.adrian.viewmodule

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.adrian.viewmodule.draggableview.DragGridActivity
import com.adrian.viewmodule.draggableview.DragListActivity
import com.adrian.viewmodule.smartedittext.SmartEditTextActivity
import com.adrian.viewmodule.videoPlayer.VideoViewActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnDragGrid.setOnClickListener { launchActivity(DragGridActivity::class.java) }
        btnDragList.setOnClickListener { launchActivity(DragListActivity::class.java) }
        btnSmartEditText.setOnClickListener { launchActivity(SmartEditTextActivity::class.java) }
        btnVideoView.setOnClickListener { launchActivity(VideoViewActivity::class.java) }

        etv.setExpandListener(object : ExpandableTextView.OnExpandListener{
            override fun onExpand(view: ExpandableTextView?) {
                Log.e("TEST_ETV", "onExpand")
            }

            override fun onShrink(view: ExpandableTextView?) {
                Log.e("TEST_ETV", "onShrink")
            }
        })
    }

    private fun <T : Activity> launchActivity(desClz: Class<T>) {
        val intent = Intent(this, desClz)
        startActivity(intent)
    }

}
