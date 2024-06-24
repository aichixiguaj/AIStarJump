package com.aichixiguaj.aisports.base

import android.hardware.camera2.CameraCharacteristics
import androidx.lifecycle.lifecycleScope
import com.aichixiguaj.aisports.R
import com.aichixiguaj.aisports.const_value.ConstValue
import com.aichixiguaj.aisports.databinding.CameraActivityBinding
import com.aichixiguaj.aisports.ext.showShortToast
import com.aichixiguaj.aisports.util.CameraSource
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Exception

/**
 *    @author   ： AiChiXiGuaJ
 *    @date      ： 2023/7/17 9:48
 *    @email    ： aichixiguaj@qq.com
 *    @desc     :    相机
 */
@Suppress("MemberVisibilityCanBePrivate")
abstract class CameraActivity : BaseActivity<CameraActivityBinding>(R.layout.camera_activity),
    CameraSource.CameraSourceListener {

    protected var cameraSource: CameraSource? = null

    // 子类初始化摄像头朝向请在initEvent()里面
    protected var firstLensFacing = CameraCharacteristics.LENS_FACING_FRONT

    // 初始化相机错误次数(开发板可能只有一个摄像头)
    protected var initCameraErrorCount = 0

    override fun init() {
        initChildView()
        initEvent()
        checkCameraPermission()
    }

    /**
     *  初始化子view
     */
    private fun initChildView() {
        mBinding.childLayout.apply {
            initView(this)?.apply {
                removeAllViews()
                addView(this)
            }
        }
    }

    /**
     *  检查相机权限
     */
    private fun checkCameraPermission() {
        val granted = XXPermissions.isGranted(this, Permission.CAMERA)
        if (granted) {
            openCamera()
        } else {
            backToPrevious()
            showShortToast("未获取到相机权限")
        }
    }

    /**
     *  打开相机
     */
    private fun openCamera() {
        // 初始化Camera
        if (cameraSource == null) {
            cameraSource = CameraSource(mBinding.textureView, this)
        }

        // 准备前置摄像头或者后置
        cameraSource?.prepareCamera(firstLensFacing)

        lifecycleScope.launch(Dispatchers.Main) {
            try {
                // 初始化相机
                cameraSource?.initCamera()
            } catch (e: Exception) {
                e.printStackTrace()
                Timber.e("出现错误：${e.message}")
                handleCameraInitException(e)
            }
        }
        // 加载模型
        initAIModel()
    }

    /**
     *  处理相机初始化错误
     */
    private fun handleCameraInitException(e: Exception) {
        var errorType = ConstValue.CAMERA_ERROR_INIT_KNOW
        val errorMsg: String
        val exceptionMsg = "${e.message}"
        if (exceptionMsg.contains("CAMERA_DISABLED", true)) {
            errorType = ConstValue.CAMERA_ERROR_DISABLED
            errorMsg = "相机被禁用"
        } else if (exceptionMsg.contains("unknown camera id", true)) {
            errorType = ConstValue.CAMERA_ERROR_NOT_SUPPORTED
            errorMsg = "不支持的相机设备"
        } else if (exceptionMsg.contains("Failed to parse camera Id", true)) {
            errorType = ConstValue.CAMERA_ERROR_NOT_SUPPORTED
            errorMsg = "不支持的相机设备"
        } else if (exceptionMsg.contains("CameraDevice was already closed", true)) {
            return
        } else {
            errorMsg = "无法初始化相机：${e.message}"
        }
        Timber.e(errorMsg)
        lifecycleScope.launch {
            openCameraError(errorType, errorMsg)
        }
    }

    fun openCameraError(typeCode: Int, msg: String) {
        var showMsg = false
        var backPage = false

        when (typeCode) {
            ConstValue.CAMERA_ERROR_INIT_KNOW -> {
                showMsg = true
                backPage = true
            }

            ConstValue.CAMERA_ERROR_NOT_SUPPORTED -> {
                initCameraErrorCount += 1
                Timber.e("相机初始化错误次数 $initCameraErrorCount")
                if (initCameraErrorCount >= 2) {
                    showMsg = true
                    backPage = true
                } else {
                    switchCamera() { isSuccess, e ->
                        if (!isSuccess) {
                            handleCameraInitException(e!!)
                        }
                    }
                }
            }

            ConstValue.CAMERA_ERROR_DISABLED -> {
                showMsg = true
                backPage = true
            }
        }

        if (showMsg) {
            showShortToast(msg)
        }
        if (backPage) {
            backToPrevious()
        }
    }

    /**
     *  切换相机
     */
    fun switchCamera(changeListener: (Boolean, Exception?) -> Unit) {
        lifecycleScope.launch {
            try {
                changeLensFacing()
                cameraSource?.switchCamera(firstLensFacing)
                changeListener.invoke(true, null)
            } catch (e: Exception) {
                changeListener.invoke(false, e)
                e.printStackTrace()
            }
        }
    }

    /**
     *  切换摄像头朝向
     */
    private fun changeLensFacing() {
        firstLensFacing = if (firstLensFacing == CameraCharacteristics.LENS_FACING_FRONT) {
            Timber.e("切換为后置摄像头")
            CameraCharacteristics.LENS_FACING_BACK
        } else {
            Timber.e("切換为前置摄像头")
            CameraCharacteristics.LENS_FACING_FRONT
        }
        cameraSource?.prepareCamera(firstLensFacing)
    }

    override fun onStart() {
        super.onStart()
        openCamera()
    }

    override fun onResume() {
        cameraSource?.resume()
        super.onResume()
    }

    override fun onPause() {
        cameraSource?.close()
        cameraSource = null
        super.onPause()
    }

    override fun onFPSListener(fps: Int) {
        lifecycleScope.launch {
            val showFPS = "FPS: $fps"
            mBinding.showFPS.text = showFPS

            cameraFPS(fps)
        }
    }

    override fun onDetectedInfo(personScore: Float?, poseLabels: List<Pair<Int, Float>>?) {
        lifecycleScope.launch {
            cameraDetectedInfo(personScore, poseLabels)
        }
    }

    override fun cameraSupportCount(count: Int) {
        lifecycleScope.launch {
            cameraCount(count)
        }
    }

    override fun updateCameraLensFacing(isFront: Boolean) {
        lifecycleScope.launch {
            // 处理当前的摄像头状态
            firstLensFacing = if (isFront) {
                CameraCharacteristics.LENS_FACING_FRONT
            } else {
                CameraCharacteristics.LENS_FACING_BACK
            }

            lensFacingIsFront(isFront)
        }
    }

    abstract fun lensFacingIsFront(boolean: Boolean)

    abstract fun cameraCount(count: Int)

    abstract fun initEvent()

    abstract fun initAIModel()

    abstract fun cameraFPS(fps: Int)

    abstract fun cameraDetectedInfo(
        personScore: Float?,
        poseLabels: List<Pair<Int, Float>>?
    )

}