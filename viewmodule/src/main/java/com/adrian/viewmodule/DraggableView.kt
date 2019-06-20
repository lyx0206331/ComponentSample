package com.adrian.viewmodule

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import kotlin.math.abs

/**
 * date:2019/6/12 19:26
 * author:RanQing
 * description:可拖动排序控件
 */
class DraggableView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) :
    RecyclerView(context, attrs, defStyle) {

    /** 是否禁止拖动 */
    var dragEnable = true
    /** 是否显示拖拽动画 */
    var showDragAnimation = true

    var onItemChangeListener: IOnItemChangeListener? = null

    /** 放大动画 */
    private var zoomAnim =
        ScaleAnimation(1.0f, 1.1f, 1.0f, 1.1f, Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF, .5f)
    /** 恢复动画 */
    private var revertAnim =
        ScaleAnimation(1.1f, 1.0f, 1.1f, 1.0f, Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF, .5f)

    /** item触摸回调 */
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
            val oldPosition = viewHolder.adapterPosition
            val targetPosition = target.adapterPosition
            onItemChangeListener?.onItemMoved(oldPosition, targetPosition)
            return false
        }

        override fun onSwiped(viewHolder: ViewHolder, direction: Int) {}

        override fun isItemViewSwipeEnabled(): Boolean = true

        override fun isLongPressDragEnabled(): Boolean = false

        override fun canDropOver(recyclerView: RecyclerView, current: ViewHolder, target: ViewHolder): Boolean {
            return onItemChangeListener?.onItemDroppable(target.adapterPosition) ?: false
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                val alpha = 1 - abs(dX) / viewHolder.itemView.width.toFloat()
                viewHolder.itemView.alpha = alpha
                viewHolder.itemView.translationX = dX
            } else {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            viewHolder.itemView.alpha = 1.0f
            viewHolder.itemView.setBackgroundColor(Color.WHITE)
            if (showDragAnimation) {
                revertView(viewHolder.itemView)
            }
        }

        override fun onSelectedChanged(viewHolder: ViewHolder?, actionState: Int) {
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                viewHolder?.let {
                    it.itemView.setBackgroundColor(Color.LTGRAY)
                    if (showDragAnimation) {
                        zoomView(it.itemView)
                    }
                }
            }
            super.onSelectedChanged(viewHolder, actionState)
        }
    })

    init {
        attrs?.apply {
            val ta = context.obtainStyledAttributes(this, R.styleable.DragRecyclerView, defStyle, 0)
            dragEnable = ta.getBoolean(R.styleable.DragRecyclerView_drv_drag_enable, true)
            showDragAnimation = ta.getBoolean(R.styleable.DragRecyclerView_drv_show_drag_anim, true)
            ta.recycle()
        }
    }

    private fun zoomView(v: View) {
        v.animation = zoomAnim
        zoomAnim.fillAfter = true
        zoomAnim.duration = 200
        zoomAnim.start()
    }

    private fun revertView(v: View) {
        v.animation = revertAnim
        revertAnim.fillAfter = true
        revertAnim.duration = 400
        revertAnim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(p0: Animation?) {
            }

            override fun onAnimationEnd(p0: Animation?) {
                v.clearAnimation()
            }

            override fun onAnimationStart(p0: Animation?) {
            }
        })
        revertAnim.start()
    }

    fun setDragAdapter(listener: IOnItemChangeListener): DraggableView {
        if (listener is Adapter<*>) {
            this.onItemChangeListener = listener
            itemTouchHelper.attachToRecyclerView(this)
            super.setAdapter(listener)
        } else {
            throw IllegalArgumentException()
        }
        return this
    }

    fun bindEvent(onItemTouchEvent: IOnItemTouchEvent): DraggableView {
        HoldTouchHelper.bind(this, onItemTouchEvent)
        return this
    }

    fun startDrag(viewHolder: ViewHolder) {
        itemTouchHelper.startDrag(viewHolder)
    }

    fun startDrag(position: Int) {
        itemTouchHelper.startDrag(getChildViewHolder(getChildAt(position)))
    }
}

class HoldTouchHelper(val recyclerView: RecyclerView, val event: IOnItemTouchEvent?) {

    companion object {
        fun bind(recyclerView: RecyclerView, event: IOnItemTouchEvent) {
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
                    event?.onLongPress(recyclerView, vh, vh.adapterPosition)
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

/**
 * 分隔线
 */
class DividerGridItemDecoration : RecyclerView.ItemDecoration {
    private var divider: Drawable?
    private var lineWidth = 1

    constructor(context: Context) {
        val ta = context.obtainStyledAttributes(intArrayOf(android.R.attr.listDivider))
        divider = ta.getDrawable(0)
        ta.recycle()
    }

    constructor(color: Int) {
        divider = ColorDrawable(color)
    }

    constructor() : this(Color.parseColor("#cccccc"))

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        drawHorizontal(c, parent)
        drawVertical(c, parent)
    }

    private fun drawHorizontal(c: Canvas, parent: RecyclerView) {
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val top = child.bottom + params.bottomMargin
            val bottom = top + lineWidth
            val left = child.left - params.leftMargin
            val right = child.right + params.rightMargin + lineWidth
            divider?.setBounds(left, top, right, bottom)
            divider?.draw(c)
        }
    }

    private fun drawVertical(c: Canvas, parent: RecyclerView) {
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val top = child.top - params.topMargin
            val bottom = child.bottom + params.bottomMargin
            val left = child.right + params.rightMargin
            val right = left + lineWidth
            divider?.setBounds(left, top, right, bottom)
            divider?.draw(c)
        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.set(0, 0, lineWidth, lineWidth)
    }
}

/**
 * 可拖拽适配器基类
 */
abstract class DraggableBaseAdapter<T : RecyclerView.ViewHolder, M : DraggableBaseEntity>(
    val data: List<M>
) : RecyclerView.Adapter<T>(), IOnItemChangeListener {

    fun getItem(position: Int): M {
        return data[position]
    }

    override fun getItemCount(): Int = data.size

    override fun onItemDraggable(position: Int): Boolean = data[position].dragEnable

    override fun onItemMoved(from: Int, target: Int) {
        if (from < target) {
            for (i in from until target) {
                if (i < target) {
                    Collections.swap(data, i, i + 1)
                } else {
                    break
                }
            }
        } else {
            for (i in from downTo target - 1) {
                if (i > target) {
                    Collections.swap(data, i, i - 1)
                } else {
                    break
                }
            }
        }
        notifyItemMoved(from, target)
    }

    override fun onItemDroppable(position: Int): Boolean = data[position].dropEnable
}

/**
 * 可拖拽内容实体基类
 */
open class DraggableBaseEntity(
    var text: String? = null,
    var dragEnable: Boolean = true,
    var dropEnable: Boolean = false
)

/**
 * 点击事件接口
 */
interface IOnItemTouchEvent {
    /**
     * 长按点击
     */
    fun onLongPress(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, position: Int)

    /**
     * 短按点击
     */
    fun onItemClick(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, position: Int)
}

/**
 * 拖动响应接口
 */
interface IOnItemChangeListener {
    /**
     * item是否可拖动
     */
    fun onItemDraggable(position: Int): Boolean

    /**
     * item移动
     */
    fun onItemMoved(from: Int, target: Int)
    /**
     * item可否删除
     */
    fun onItemDroppable(position: Int): Boolean
}