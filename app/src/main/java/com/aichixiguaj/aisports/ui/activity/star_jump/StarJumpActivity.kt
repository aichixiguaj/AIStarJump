package com.aichixiguaj.aisports.ui.activity.star_jump

import android.hardware.camera2.CameraCharacteristics
import android.widget.FrameLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.aichixiguaj.aisports.R
import com.aichixiguaj.aisports.base.CameraActivity
import com.aichixiguaj.aisports.data.Device
import com.aichixiguaj.aisports.data.StarJumpTypes
import com.aichixiguaj.aisports.databinding.LayoutStarJumpControllerBinding
import com.aichixiguaj.aisports.ext.gone
import com.aichixiguaj.aisports.ext.inflateView
import com.aichixiguaj.aisports.ext.setVisibleOrGone
import com.aichixiguaj.aisports.ext.showShortToast
import com.aichixiguaj.aisports.ext.visible
import com.aichixiguaj.aisports.ml.MoveNet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Exception
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 *    @author   ： AiChiXiGuaJ
 *    @date      ： 2023/7/17 9:48
 *    @email    ： aichixiguaj@qq.com
 *    @desc     :    开合跳
 */
class StarJumpActivity : CameraActivity() {

    // 设备解析方式
    private var device: Device = Device.CPU

    // 是否计分(开、不协调开计分)   退出(合、不协调和退出)
    private val isEnter = AtomicBoolean(false)

    // 计数 有效数和不协调数
    private var count = AtomicInteger(0)
    private var errorCount = AtomicInteger(0)

    // 是否超出屏幕
    private var isOutsideScreen = AtomicBoolean(true)

    private lateinit var childBinding: LayoutStarJumpControllerBinding

    override fun initEvent() {
        firstLensFacing = CameraCharacteristics.LENS_FACING_BACK

        childBinding.apply {
            switchCameraIV.setOnClickListener {
                it?.isEnabled = false
                switchCamera() { _, _ ->
                    lifecycleScope.launch {
                        delay(1000)
                        it?.isEnabled = true
                    }
                }
            }
        }
    }

    override fun initAIModel() {
        cameraSource?.apply {
            // 加载分类器
            setClassifier(StarJumpPoseClassifier.create())
            // 是否显示人体的关键点(圆点标识)
            showPersonCircle(true)
            // 关节点检测
            val poseDetector = MoveNet.create(WeakReference(this@StarJumpActivity), device)
            setDetector(poseDetector)
        }
    }

    override fun cameraDetectedInfo(personScore: Float?, poseLabels: List<Pair<Int, Float>>?) {
        poseLabels?.let {
            try {
                handleScore(it[0])
                checkIsOutSideScreen(it[1])
                val showMsg = "有效个数：${count.get()}\n不协调个数：${errorCount.get()}"
                mBinding.showPersonInfo.text = showMsg
            } catch (e: Exception) {
                showShortToast("解析动作出错：${e.message}")
                e.printStackTrace()
            }
        }
    }

    override fun initView(container: FrameLayout) = inflateView<LayoutStarJumpControllerBinding>(
        R.layout.layout_star_jump_controller,
        container
    ).apply {
        childBinding = this
    }.root

    /**
     *  检查是否超出屏幕
     */
    private fun checkIsOutSideScreen(pair: Pair<Int, Float>) {
        try {
            isOutsideScreen.set(pair.second == 1f)
        } catch (e: Exception) {
            showShortToast("处理超出屏幕异常：${e.message}")
            e.printStackTrace()
        }
    }


    /**
     *  处理成绩
     */
    private fun handleScore(pair: Pair<Int, Float>) {
        val confidence = pair.second

        // 如果超过了屏幕且没显示
        if (pair.first == 99) {
            if (mBinding.outScreen.isGone) {
                mBinding.outScreen.visible()
            }
        } else if (mBinding.outScreen.isVisible) {
            mBinding.outScreen.gone()
        }

        when (pair.first) {
            StarJumpTypes.OPEN.position -> addCount(false, confidence)
            StarJumpTypes.ERROR_OPEN.position -> addCount(true, confidence)
            StarJumpTypes.CLOSE.position -> existOpenState()
            StarJumpTypes.ERROR_CLOSE.position -> existOpenState()
            StarJumpTypes.OTHER.position -> existOpenState()
        }
    }

    /**
     *  处理退出状态
     */
    private fun existOpenState() {
        if (isEnter.get()) {
            isEnter.set(false)
        }
    }

    /**
     *  添加个数
     */
    private fun addCount(isErrorCount: Boolean, confidence: Float) {
        // 如果是合的状态
        if (confidence == 1f) {
            if (!isEnter.get()) {
                // 修改为开并
                isEnter.set(true)
                if (isErrorCount) {
                    errorCount.set(errorCount.get() + 1)
                } else {
                    count.set(count.get() + 1)
                }
            }
        }
    }

    override fun cameraFPS(fps: Int) {
    }

    override fun lensFacingIsFront(boolean: Boolean) {
    }

    override fun cameraCount(count: Int) {
        childBinding.switchCameraIV.setVisibleOrGone(count > 1)
    }

}