package com.aichixiguaj.aisports.util

import android.app.Activity
import java.util.*

class ActivityManagerTool {

    // Activity栈
    private lateinit var activityStack: Stack<Activity>

    companion object {
        // 单例模式
        private lateinit var instance: ActivityManagerTool

        /**
         * 获取实例
         */
        fun getAppManager(): ActivityManagerTool {
            synchronized(this) {
                if (!Companion::instance.isInitialized) {
                    instance = ActivityManagerTool()
                }
            }
            return instance
        }
    }

    /**
     *  如果是第一个界面
     */
    fun isFirstPage(): Boolean {
        if (::activityStack.isInitialized) {
            return activityStack.size == 1
        }
        return false
    }

    /**
     * 添加Activity到堆栈
     */
    fun addActivity(activity: Activity) {
        if (!::activityStack.isInitialized) {
            activityStack = Stack()
        }
        activityStack.add(activity)
    }

    /**
     * 从栈中移除Activity
     */
    fun removeActivity(activity: Activity) {
        if (::activityStack.isInitialized) {
            activityStack.remove(activity)
        }
    }

    /**
     *  销毁上一个activity
     */
    fun finishPreviousActivity() {
        if (::activityStack.isInitialized) {
            val finishIndex = activityStack.size - 2
            if (finishIndex >= 0) {
                val activity = activityStack[finishIndex]
                activity.finish()
                removeActivity(activity)
            }
        }
    }

    /**
     * 获取当前Activity（堆栈中最后一个压入的）
     */
    fun currentActivity(): Activity {
        return activityStack.lastElement()
    }

    /**
     * 结束当前Activity（堆栈中最后一个压入的）
     */
    fun finishActivity() {
        val activity = activityStack.lastElement()
        finishActivity(activity)
    }

    /**
     * 结束指定的Activity
     */
    fun finishActivity(activity: Activity?) {
        activity?.let {
            activityStack.remove(activity)
            activity.finish()
        }
    }

    /**
     * 结束指定类名的Activity
     */
    fun finishActivity(cls: Class<*>) {
        for (activity in activityStack) {
            if (activity.javaClass == cls) {
                activityStack.remove(activity)
                finishActivity(activity)
                break
            }
        }
    }

    /**
     * 结束所有Activity
     */
    fun finishAllActivity() {
        for (index in activityStack.indices) {
            if (null != activityStack[index]) {
                activityStack[index].finish()
            }
        }
        activityStack.clear()
    }

}