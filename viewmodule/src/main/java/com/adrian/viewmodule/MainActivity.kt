package com.adrian.viewmodule

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.adrian.viewmodule.draggableview.DragGridActivity
import com.adrian.viewmodule.draggableview.DragListActivity
import com.adrian.viewmodule.smartedittext.SmartEditTextActivity
import com.adrian.viewmodule.videoPlayer.VideoViewActivity
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnDragGrid.setOnClickListener { launchActivity(DragGridActivity::class.java) }
        btnDragList.setOnClickListener { launchActivity(DragListActivity::class.java) }
        btnSmartEditText.setOnClickListener { launchActivity(SmartEditTextActivity::class.java) }
        btnVideoView.setOnClickListener { launchActivity(VideoViewActivity::class.java) }

//        etv.setExpandListener(object : ExpandableTextView.OnExpandListener{
//            override fun onExpand(view: ExpandableTextView?) {
//                Log.e("TEST_ETV", "onExpand")
//            }
//
//            override fun onShrink(view: ExpandableTextView?) {
//                Log.e("TEST_ETV", "onShrink")
//            }
//        })

        val tabs = arrayListOf("标题一", "标题二", "标题三", "标题四", "标题五")
        initTabTitle(tabs)
    }

    private fun initTabTitle(tabs: ArrayList<String>) {
        var holer: TabViewHoler?
        tabs.forEachIndexed { index, s ->
            val tab = tabTitle.newTab()
            tab?.setCustomView(R.layout.item_outing_topic)
            holer = TabViewHoler(tab?.customView)
            holer?.tvTitle?.text = s
            if (index == 0) {
                holer?.tvTitle?.isSelected = true
                holer?.tvTitle?.setTextSize(TypedValue.COMPLEX_UNIT_PX, 40f)
            }
            tabTitle.addTab(tab)
        }
        tabTitle.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(p0: TabLayout.Tab?) {
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {
                holer = TabViewHoler(p0?.customView)
                holer?.tvTitle?.isSelected = false
                holer?.tvTitle?.setTextSize(TypedValue.COMPLEX_UNIT_PX, 30f)
            }

            override fun onTabSelected(p0: TabLayout.Tab?) {
                holer = TabViewHoler(p0?.customView)
                holer?.tvTitle?.isSelected = true
                holer?.tvTitle?.setTextSize(TypedValue.COMPLEX_UNIT_PX, 40f)
            }
        })
    }

    inner class TabViewHoler(parent: View?) {
        val tvTitle = parent?.findViewById<TextView>(R.id.tvTopicName)
    }

    private fun <T : Activity> launchActivity(desClz: Class<T>) {
        val intent = Intent(this, desClz)
        startActivity(intent)
    }

}
