package com.adrian.viewmodule.smartedittext

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.adrian.viewmodule.R
import kotlinx.android.synthetic.main.activity_smart_edit_text.*

class SmartEditTextActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_smart_edit_text)

        smartEditText4Regxp.illegalInputTips = { _, _ ->
            Toast.makeText(this, R.string.password_hint, Toast.LENGTH_SHORT).show()
        }

        smartEditText4Match.matchRule = { _, p0 ->
            Log.e("FILTER", "p0: $p0")
            p0?.length ?: 0 <= 6
        }
        smartEditText4Match.illegalInputTips = { _, _ ->
            Toast.makeText(this, "输入太长", Toast.LENGTH_SHORT).show()
        }
    }
}
