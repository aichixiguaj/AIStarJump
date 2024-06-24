package com.aichixiguaj.aisports.ui.activity.main

import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowMetrics
import com.aichixiguaj.aisports.R
import com.aichixiguaj.aisports.base.BaseActivity
import com.aichixiguaj.aisports.databinding.ActivityMainBinding
import com.aichixiguaj.aisports.ext.openActivity
import com.aichixiguaj.aisports.ext.showShortToast
import com.aichixiguaj.aisports.ui.activity.star_jump.StarJumpActivity
import com.aichixiguaj.aisports.util.UtilObject
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import timber.log.Timber

class MainActivity : BaseActivity<ActivityMainBinding>(R.layout.activity_main) {

    override fun init() {
        initScreenHeightWidth()

        mBinding.toJump.setOnClickListener {
            it.isEnabled = false
            checkNeedPermission()
            it.postDelayed({
                it.isEnabled = true
            }, 1000)
        }
    }

    @Suppress("DEPRECATION")
    private fun initScreenHeightWidth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics: WindowMetrics = windowManager.currentWindowMetrics
            windowMetrics.bounds.apply {
                UtilObject.cacheRepository.saveScreenWidthHeight(width(), height())
                Timber.e("屏幕宽${width()}  高${height()}")
            }
        } else {
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.apply {
                UtilObject.cacheRepository.saveScreenWidthHeight(widthPixels, heightPixels)
                Timber.e("屏幕宽${widthPixels}  高${heightPixels}")
            }
        }
    }

    private fun checkNeedPermission() {
        XXPermissions.with(this)
            // 申请单个权限
            .permission(Permission.CAMERA)
            .request(object : OnPermissionCallback {

                override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                    if (!allGranted) {
                        showShortToast("获取部分权限成功，但部分权限未正常授予")
                        return
                    }
                    openActivity<StarJumpActivity>()
                }

                override fun onDenied(permissions: MutableList<String>, doNotAskAgain: Boolean) {
                    if (doNotAskAgain) {
                        showShortToast("被永久拒绝授权，请手动授予相机权限")
                        // 如果是被永久拒绝就跳转到应用权限系统设置页面
                        XXPermissions.startPermissionActivity(this@MainActivity, permissions)
                    } else {
                        showShortToast("获取相机权限")
                    }
                }
            })
    }

}