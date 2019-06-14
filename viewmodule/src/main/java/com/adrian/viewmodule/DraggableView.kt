package com.adrian.viewmodule

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

/**
 * date:2019/6/12 19:26
 * author:RanQing
 * description:可拖动排序控件
 */
class DraggableView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) :
    RecyclerView(context, attrs, defStyle) {

    private var dragEnable = true
    private var showDragAnimation = true

    private val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: ViewHolder): Int {
            if (!dragEnable) {
                return ItemTouchHelper.ACTION_STATE_IDLE
            }
            return if (recyclerView.layoutManager is GridLayoutManager) {
                val dragFlags =
                    ItemTouchHelper.UP.or(ItemTouchHelper.DOWN).or(ItemTouchHelper.LEFT).or(ItemTouchHelper.RIGHT)
                makeMovementFlags(dragFlags, ItemTouchHelper.ACTION_STATE_IDLE)
            } else {
                val dragFlags = ItemTouchHelper.UP.or(ItemTouchHelper.DOWN)
                makeMovementFlags(dragFlags, ItemTouchHelper.ACTION_STATE_IDLE)
            }
        }

        override fun onMove(recyclerView: RecyclerView, viewHolder: ViewHolder, target: ViewHolder): Boolean {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    })

    init {
        attrs?.apply {
            val ta = context.obtainStyledAttributes(this, R.styleable.DragRecyclerView)
            dragEnable = ta.getBoolean(R.styleable.DragRecyclerView_drv_drag_enable, true)
            showDragAnimation = ta.getBoolean(R.styleable.DragRecyclerView_drv_show_drag_anim, true)
            ta.recycle()
        }
    }
}

class HoldTouchHelper(val recyclerView: RecyclerView, val event: OnItemTouchEvent?) {

    companion object {
        fun bind(recyclerView: RecyclerView, event: OnItemTouchEvent) {
            HoldTouchHelper(recyclerView, event)
        }
    }

    init {
        val simpleOnGestureListener = object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                event?.let {
                    val child = recyclerView.findChildViewUnder(e.x, e.y)
                    child?.apply {
                        val vh = recyclerView.getChildViewHolder(this)
                        it.onItemClick(recyclerView, vh, vh.adapterPosition)
                    }
                }
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                val child = recyclerView.findChildViewUnder(e.x, e.y)
                child?.apply {
                    val vh = recyclerView.getChildViewHolder(this)
                    event?.onItemClick(recyclerView, vh, vh.adapterPosition)
                }
            }
        }

        val detector = GestureDetectorCompat(recyclerView.context, simpleOnGestureListener)
        val onItemTouchListener = object : RecyclerView.OnItemTouchListener {
            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
                detector.onTouchEvent(e)
            }

            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                detector.onTouchEvent(e)
                return false
            }

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
            }
        }

        recyclerView.addOnItemTouchListener(onItemTouchListener)
    }
}

interface OnItemTouchEvent {
    fun onLongPress(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, position: Int)
    fun onItemClick(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, position: Int)
}

interface OnItemChangeListener {
    /**
     * item can be draged
     */
    fun onItemDrag(position: Int)
}