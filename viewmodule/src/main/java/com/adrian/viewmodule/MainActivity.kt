package com.adrian.viewmodule

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        draggableView.addItemDecoration(DividerGridItemDecoration())
        draggableView.setHasFixedSize(false)
        draggableView.layoutManager = GridLayoutManager(this, 3)

        val data = ArrayList<DragEntity>()
        data.add(DragEntity("今日热点", R.mipmap.hot, dragEnable = true, dropEnable = true))
        data.add(DragEntity("国际新闻", R.mipmap.news_international, dragEnable = true, dropEnable = true))
        data.add(DragEntity("及时快讯", R.mipmap.news, dragEnable = true, dropEnable = true))
        data.add(DragEntity("美女图片", R.mipmap.beauty, dragEnable = true, dropEnable = true))
        data.add(DragEntity("正经要闻", R.mipmap.politics, dragEnable = true, dropEnable = true))
        data.add(DragEntity("体育赛事", R.mipmap.sports, dragEnable = true, dropEnable = true))
        data.add(DragEntity("茶余饭后", R.mipmap.gossip, dragEnable = true, dropEnable = true))
        data.add(DragEntity("添加更多", R.mipmap.more, dragEnable = false, dropEnable = false))

        draggableView.dragEnable = true
        draggableView.showDragAnimation = true
        draggableView.setDragAdapter(DragAdapter(this, data))
        draggableView.bindEvent(itemTouchEvent)

    }

    val itemTouchEvent = object : IOnItemTouchEvent {
        override fun onLongPress(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, position: Int) {
            if ((recyclerView.adapter as DraggableBaseAdapter<*, *>).onItemDraggable(position)) {
                (recyclerView as DraggableView).startDrag(position)
            }
        }

        override fun onItemClick(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, position: Int) {
            val text = (recyclerView.adapter as DragAdapter).getItem(position).text
            Toast.makeText(this@MainActivity, text, Toast.LENGTH_SHORT).show()
        }
    }

    class DragEntity(
        text: String? = null,
        var drawbableId: Int,
        dragEnable: Boolean = true,
        dropEnable: Boolean = false
    ) : DraggableBaseEntity(text, dragEnable, dropEnable)

    class DragAdapter(val context: Context, data: List<DragEntity>) :
        DraggableBaseAdapter<DragAdapter.GridViewHolder, DragEntity>(context, data) {

        fun getItem(position: Int): DragEntity {
            return data[position]
        }

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
