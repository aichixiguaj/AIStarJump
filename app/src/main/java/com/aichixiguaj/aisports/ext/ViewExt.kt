package com.aichixiguaj.aisports.ext

import android.view.View
import androidx.annotation.IdRes

/**
 *    @author   ： AiChiXiGuaJ
 *    @date      ： 2022/3/10 14:28
 *    @email    ： aichixiguaj@qq.com
 *    @desc     :    view 扩展
 */

fun View.gone() {
    this.visibility = View.GONE
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.setVisibleOrGone(boolean: Boolean) {
    if (boolean) {
        this.visible()
    } else {
        this.gone()
    }
}

fun View.setVisibleOrInvisible(boolean: Boolean) {
    if (boolean) {
        this.visible()
    } else {
        this.invisible()
    }
}

fun <T : View> View.getViewFromId(@IdRes id: Int) = this.findViewById<T>(id)