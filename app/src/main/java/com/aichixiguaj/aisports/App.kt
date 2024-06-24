package com.aichixiguaj.aisports

import android.app.Application
import com.aichixiguaj.aisports.util.UtilObject
import com.tencent.bugly.crashreport.CrashReport
import com.tencent.mmkv.MMKV
import timber.log.Timber

/**
 *    @author   ： AiChiXiGuaJ
 *    @date      ： 2023/7/10 14:53
 *    @email    ： aichixiguaj@qq.com
 *    @desc     :
 */
class App : Application() {

    companion object {

        private lateinit var application: Application

        fun get() = application
    }

    override fun onCreate() {
        super.onCreate()
        application = this
        initSDK()
    }

    /**
     *  初始化SDK
     */
    private fun initSDK() {
        // 初始化MMKV
        MMKV.initialize(this)
        // 日志上报Bugly
        CrashReport.initCrashReport(applicationContext, "167f6f8385", true)
        // 初始化日志
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        UtilObject.init()
    }
}