package com.aichixiguaj.aisports.ext

import android.widget.Toast
import com.aichixiguaj.aisports.App

/**
 *@author：AiChiXiGuaJ
 *@description：吐司扩展
 *@email：aichixiguaj@qq.com
 *@date：2021/6/8 22:33
 */

internal var toast: Toast? = null

/**
 *  显示短Toast
 */
fun showShortToast(msg: String) {
    showToast(msg, Toast.LENGTH_SHORT)
}

/**
 *  显示长Toast
 */
fun showLongToast(msg: String) {
    showToast(msg, Toast.LENGTH_LONG)
}

/**
 *  显示Toast
 */
private fun showToast(msg: String, toastLength: Int) {
    toast?.cancel()
    toast = Toast.makeText(App.get(), msg, toastLength).apply { show() }
    toast = null
}