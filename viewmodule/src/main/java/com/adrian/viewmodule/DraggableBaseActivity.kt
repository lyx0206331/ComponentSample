package com.adrian.viewmodule

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_draggable_base.*

/**
 * date:2019/6/20 13:50
 * author:RanQing
 * description:可拖拽界面示例基类
 */
abstract class DraggableBaseActivity<T : DraggableBaseEntity> : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_draggable_base)

        draggableView.addItemDecoration(DividerGridItemDecoration())
        draggableView.setHasFixedSize(false)
        draggableView.layoutManager = getLayoutManager()

        draggableView.dragEnable = true
        draggableView.showDragAnimation = true
        draggableView.setDragAdapter(getDragAdapter(getData()))
        draggableView.bindEvent(itemTouchEvent)
    }

    abstract fun getData(): List<T>

    abstract fun getDragAdapter(data: List<T>): DraggableBaseAdapter<*, T>

    abstract fun getLayoutManager(): RecyclerView.LayoutManager


    val itemTouchEvent = object : IOnItemTouchEvent {
        override fun onLongPress(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, position: Int) {
            if ((recyclerView.adapter as DraggableBaseAdapter<*, *>).onItemDraggable(position)) {
                (recyclerView as DraggableView).startDrag(position)
            }
        }

        override fun onItemClick(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, position: Int) {
            val text = (recyclerView.adapter as DraggableBaseAdapter<*, *>).getItem(position).text
            Toast.makeText(this@DraggableBaseActivity, text, Toast.LENGTH_SHORT).show()
        }
    }

    class DragEntity(
        text: String? = null,
        var drawbableId: Int,
        dragEnable: Boolean = true,
        dropEnable: Boolean = false
    ) : DraggableBaseEntity(text, dragEnable, dropEnable)


}
