package com.aichixiguaj.aisports.ext

import android.content.Context
import android.content.Intent
import androidx.core.app.ActivityOptionsCompat

/**
 *  可带参意图
 */
inline fun <reified T> Context.openActivity(block: Intent.() -> Unit) {
    val intent = Intent(this, T::class.java)
    intent.block()
    this.startActivity(intent)
}

/**
 * 无参意图
 */
inline fun <reified T> Context.openActivity() {
    val intent = Intent(this, T::class.java)
    this.startActivity(intent)
}

/**
 *  自定义Action意图
 *  系统Action意图
 */
inline fun Context.openActionActivity(block: Intent.() -> Unit) {
    Intent().let {
        it.block()
        startActivity(it)
    }
}

/**
 * 无参意图
 */
inline fun <reified T> Context.openActivityWithAnim(option: ActivityOptionsCompat) {
    val intent = Intent(this, T::class.java)
    this.startActivity(intent,option.toBundle())
}

