package com.adrian.viewmodule.smartedittext

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.widget.EditText
import com.adrian.viewmodule.R
import java.util.regex.Pattern

/**
 * date:2019/7/5 15:01
 * author:RanQing
 * description:
 */
class SmartEditText @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    EditText(context, attrs, defStyleAttr) {

    /** 用户判断输入内容的正则表达式 */
    var regexp: String?

    var onIllegalListener: ((v: EditText, editable: Editable?) -> Unit)? = null

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.SmartEditText, defStyleAttr, 0)
        regexp = ta.getString(R.styleable.SmartEditText_set_regexp)
        ta.recycle()

        addTextChangedListener(object : TextWatcher {
            var tmp: Editable = Editable.Factory.getInstance().newEditable("")
            override fun afterTextChanged(p0: Editable?) {
                p0?.let {
                    if (!isMatched(it)) {
                        onIllegalListener?.invoke(this@SmartEditText, p0)
                        text = it
                        setSelection(it.length)
                        return
                    }
                    tmp = p0
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })
    }

    /**
     * 判断是否匹配正则表达式
     */
    private fun isMatched(editable: Editable): Boolean {
        return Pattern.matches(regexp, editable)
    }
}