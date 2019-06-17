package com.adrian.modulemain.application

import android.app.Application
import android.util.Log
import com.alibaba.android.arouter.launcher.ARouter

/**
 * date:2019/5/9 17:09
 * author:RanQing
 * description:
 */
class MainApplication: Application() {

    private var isDebug = true

    override fun onCreate() {
        super.onCreate()
        Log.d("Application", "MainApplication")
        if (isDebug) {
            ARouter.openLog()
            ARouter.openDebug()
        }
        ARouter.init(this)
    }
}