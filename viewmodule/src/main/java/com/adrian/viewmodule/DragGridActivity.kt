package com.adrian.viewmodule

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * date:2019/6/20 16:02
 * author:RanQing
 * description:可拖拽网格布局示例
 */
class DragGridActivity : DraggableBaseActivity<DraggableBaseActivity.DragEntity>() {
    override fun getLayoutManager(): RecyclerView.LayoutManager {
        return GridLayoutManager(this, 3)
    }

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
        data.add(DragEntity("添加更多", R.mipmap.more, dragEnable = false, dropEnable = false))
        return data
    }

    override fun getDragAdapter(data: List<DragEntity>): GridDragAdapter {
        return GridDragAdapter(this, data)
    }

    class GridDragAdapter(val context: Context, data: List<DragEntity>) :
        DraggableBaseAdapter<GridDragAdapter.GridViewHolder, DragEntity>(data) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridViewHolder {
            val itemView = LayoutInflater.from(context).inflate(R.layout.item_grid, parent, false)
            return GridViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: GridViewHolder, position: Int) {
            holder.tv.text = data[position].text
            holder.iv.setImageResource(data[position].drawbableId)
        }

        class GridViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val iv = itemView.findViewById<ImageView>(R.id.imageView)
            val tv = itemView.findViewById<TextView>(R.id.textView)
        }
    }
}