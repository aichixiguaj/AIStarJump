package com.aichixiguaj.aisports.ext

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

fun <T : ViewDataBinding> Context.inflateView(@LayoutRes layoutRes: Int, parent: ViewGroup?) =
    DataBindingUtil.inflate<T>(LayoutInflater.from(this), layoutRes, parent, false)