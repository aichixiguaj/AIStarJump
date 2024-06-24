package com.aichixiguaj.aisports.ui.activity.high_knees

import android.view.View
import android.widget.FrameLayout
import com.aichixiguaj.aisports.base.CameraActivity

/**
 *    @author   ： AiChiXiGuaJ
 *    @date      ： 2023/7/24 10:11
 *    @email    ： aichixiguaj@qq.com
 *    @desc     :   高抬腿
 */
class HighKneesActivity:CameraActivity() {

    override fun lensFacingIsFront(boolean: Boolean) {
    }

    override fun cameraCount(count: Int) {
    }

    override fun initEvent() {
    }

    override fun initAIModel() {
    }

    override fun cameraFPS(fps: Int) {
    }

    override fun cameraDetectedInfo(personScore: Float?, poseLabels: List<Pair<Int, Float>>?) {

    }

    override fun initView(container: FrameLayout): View? {
        return null
    }

}