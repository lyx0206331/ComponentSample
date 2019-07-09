package com.adrian.viewmodule.smartedittext

import android.content.Context
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.widget.EditText
import com.adrian.viewmodule.R
import java.util.regex.Pattern

/**
 * date:2019/7/5 15:01
 * author:RanQing
 * description:可自定义输入规则的EditText初步实现，有需要大家可自行扩展
 */
class SmartEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.editTextStyle
) :
    EditText(context, attrs, defStyleAttr) {

    /** 判断输入规则的正则表达式.注：不要使用初次判断为false的表达式,否则会导致无法输入，如包含总长度必须>1（如:{2,}）此类判断的条件，可用："*", "?", "+", "{0,}","{,num}", "{1,}"等匹配 */
    var regexp: String?

    /** 非法输入提示 */
    var illegalInputTips: ((v: EditText, editable: Editable?) -> Unit)? = null

    /** 自定义输入规则.此规则优先级大于regexp，有此规则时，不判断regexp */
    var matchRule: ((v: EditText, editable: Editable?) -> Boolean)? = null

    /** 合法输入监听 */
    var legalInputListener: ((v: EditText, editable: Editable?) -> Unit)? = null

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.SmartEditText, defStyleAttr, 0)
        regexp = ta.getString(R.styleable.SmartEditText_set_regexp)
        ta.recycle()

        addTextChangedListener(object : TextWatcher {

            var tmp = ""

            override fun afterTextChanged(p0: Editable?) {
                val content = p0?.toString() ?: ""
                if (matchRule != null) {
                    if (matchRule?.invoke(this@SmartEditText, p0) == false) {
                        illegalInputTips?.invoke(this@SmartEditText, p0)
                        setText(tmp)
                        setSelection(tmp.length)
                        return
                    }
                } else if (!isMatched(content)) {
                    illegalInputTips?.invoke(this@SmartEditText, p0)
                    setText(tmp)
                    setSelection(tmp.length)
                    return
                }
                legalInputListener?.invoke(this@SmartEditText, p0)
                tmp = content
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })
    }

    /**
     * 判断是否匹配正则表达式
     */
    private fun isMatched(content: String): Boolean {
        return if (TextUtils.isEmpty(regexp) || TextUtils.isEmpty(content)) true else Pattern.matches(regexp!!, content)
    }

    private fun logE(msg: String?) {
        Log.e("SmartEditText", msg ?: "")
    }
}