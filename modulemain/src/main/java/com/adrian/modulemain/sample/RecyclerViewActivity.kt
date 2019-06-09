package com.adrian.modulemain.sample

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.adrian.modulemain.R
import com.adrian.modulemain.baseadapter.BaseRecyclerAdapter
import kotlinx.android.synthetic.main.modulemain_activity_recycler_view.*
import kotlinx.android.synthetic.main.modulemain_item_textview.*

class RecyclerViewActivity : AppCompatActivity() {

    val list = arrayListOf("abc", "bcd", "asdfa", "东奔西走", "基本原则")

    val adapter by lazy { StringAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.modulemain_activity_recycler_view)

        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        recyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayout.VERTICAL))
        recyclerView.itemAnimator = DefaultItemAnimator()
        adapter.setData(list)
        adapter.onItemClickListener =
            { v, position -> Toast.makeText(this, "点击${adapter.getItem(position)}", Toast.LENGTH_SHORT).show() }
        adapter.onItemLongClickListener = { v, position ->
            Toast.makeText(this, "长按${adapter.getItem(position)}", Toast.LENGTH_SHORT).show()
            true
        }
        recyclerView.adapter = adapter

    }

    class StringAdapter : BaseRecyclerAdapter<String>(R.layout.modulemain_item_textview) {
        override fun onBindViewHolder(holder: CommonViewHolder, position: Int) {
            super.onBindViewHolder(holder, position)
            holder.textview.text = getItem(position)
        }
    }
}
