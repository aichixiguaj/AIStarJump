package com.aichixiguaj.aisports.base

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.aichixiguaj.aisports.ext.openActionActivity
import com.aichixiguaj.aisports.util.ActivityManagerTool

@Suppress("MemberVisibilityCanBePrivate")
abstract class BaseActivity<T : ViewDataBinding>(@LayoutRes val layoutId: Int = 0) :
    AppCompatActivity() {

    protected var pageManager = ActivityManagerTool.getAppManager()

    protected var getSavedInstanceState: Bundle? = null

    protected lateinit var mBinding: T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSavedInstanceState = savedInstanceState
        mBinding = DataBindingUtil.setContentView(this, layoutId)
        pageManager.addActivity(this)

        init()
        initBackEvent()
    }


    override fun onDestroy() {
        pageManager.removeActivity(this)
        super.onDestroy()
    }

    fun backToPrevious() {
        if (pageManager.isFirstPage()) {
            backDeskHome()
        } else {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun initBackEvent() {
        onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (pageManager.isFirstPage()) {
                        backDeskHome()
                    } else {
                        finish()
                    }
                }
            })
    }


    abstract fun init()

    /**
     *  回到桌面
     */
    fun backDeskHome() {
        openActionActivity {
            action = Intent.ACTION_MAIN
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            addCategory(Intent.CATEGORY_HOME)
        }
    }

}