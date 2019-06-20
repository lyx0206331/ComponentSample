package com.adrian.viewmodule

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * date:2019/6/20 16:29
 * author:RanQing
 * description:可拖拽垂直线性列表示例
 */
class DragListActivity : DraggableBaseActivity<DraggableBaseActivity.DragEntity>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun getData(): List<DragEntity> {
        val data = ArrayList<DragEntity>()
        data.add(DragEntity("今日热点", R.mipmap.hot, dragEnable = true, dropEnable = true))
        data.add(DragEntity("国际新闻", R.mipmap.news_international, dragEnable = true, dropEnable = true))
        data.add(DragEntity("及时快讯", R.mipmap.news, dragEnable = true, dropEnable = true))
        data.add(DragEntity("美女图片", R.mipmap.beauty, dragEnable = true, dropEnable = true))
        data.add(DragEntity("正经要闻", R.mipmap.politics, dragEnable = true, dropEnable = true))
        data.add(DragEntity("体育赛事", R.mipmap.sports, dragEnable = true, dropEnable = true))
        data.add(DragEntity("茶余饭后", R.mipmap.gossip, dragEnable = true, dropEnable = true))
        return data
    }

    override fun getDragAdapter(data: List<DragEntity>): DraggableBaseAdapter<*, DragEntity> {
        return ListDragAdapter(this, data)
    }

    override fun getLayoutManager(): RecyclerView.LayoutManager {
        return LinearLayoutManager(this)
    }

    class ListDragAdapter(val context: Context, data: List<DragEntity>) :
        DraggableBaseAdapter<ListDragAdapter.ListViewHolder, DragEntity>(data) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
            val itemView = LayoutInflater.from(context).inflate(R.layout.item_list, parent, false)
            return ListViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
            holder.tv.text = data[position].text
        }

        class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tv = itemView.findViewById<TextView>(R.id.textView)
        }
    }
}