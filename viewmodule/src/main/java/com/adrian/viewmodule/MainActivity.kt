package com.adrian.viewmodule

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.adrian.viewmodule.draggableview.DragGridActivity
import com.adrian.viewmodule.draggableview.DragListActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnDragGrid.setOnClickListener { launchActivity(DragGridActivity::class.java) }
        btnDragList.setOnClickListener { launchActivity(DragListActivity::class.java) }
    }

    private fun <T : Activity> launchActivity(desClz: Class<T>) {
        val intent = Intent(this, desClz)
        startActivity(intent)
    }

}
