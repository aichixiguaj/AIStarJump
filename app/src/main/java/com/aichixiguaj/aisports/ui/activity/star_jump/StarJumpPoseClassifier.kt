/* Copyright 2021 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================
*/

package com.aichixiguaj.aisports.ui.activity.star_jump

import com.aichixiguaj.aisports.data.BodyPart
import com.aichixiguaj.aisports.data.KeyPoint
import com.aichixiguaj.aisports.data.Person
import com.aichixiguaj.aisports.data.StarJumpTypes
import com.aichixiguaj.aisports.ext.toInt
import com.aichixiguaj.aisports.ml.BasePoseClassifier
import com.aichixiguaj.aisports.util.PoseUtil
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

class StarJumpPoseClassifier : BasePoseClassifier {

    // 鼻子
    private val noseIndex = BodyPart.NOSE.position

    // 肩部
    private val leftShoulderIndex = BodyPart.LEFT_SHOULDER.position
    private val rightShoulderIndex = BodyPart.RIGHT_SHOULDER.position

    // 手腕
    private val leftWristIndex = BodyPart.LEFT_WRIST.position
    private val rightWristIndex = BodyPart.RIGHT_WRIST.position

    // 手肘
    private val leftElbowIndex = BodyPart.LEFT_ELBOW.position
    private val rightElbowIndex = BodyPart.RIGHT_ELBOW.position

    // 脚踝
    private val leftAnkleIndex = BodyPart.LEFT_ANKLE.position
    private val rightAnkleIndex = BodyPart.RIGHT_ANKLE.position

    // 臀部
    private val leftHipIndex = BodyPart.LEFT_HIP.position
    private val rightHipIndex = BodyPart.RIGHT_HIP.position

    private var poseUtil = PoseUtil()

    // 双腿 是否改变了状态
    private val legIsResetState = AtomicBoolean(false)

    // 手臂关键角度（用于判断手臂是否合并）
    private var armKeyOpenAngle = 60f
    private var armKeyCloseAngle = 30f

    companion object {

        // 手臂状态： 开、合、其它
        const val STAR_JUMP_ARM_OPEN = 1
        const val STAR_JUMP_ARM_CLOSE = 0
        const val STAR_JUMP_ARM_OTHER = -1

        fun create(): StarJumpPoseClassifier {
            return StarJumpPoseClassifier()
        }
    }

    /**
     *  返回两个Pair
     *  0 ：动作类型 + 可信度[0.0 - 1.0]
     *  1 ：超出屏幕 + 可信度[1：超出屏幕  0：在屏幕内]
     */
    override fun classify(person: Person?): List<Pair<Int, Float>> {
        val output = mutableListOf<Pair<Int, Float>>()
        person?.keyPoints?.let {
            try {
                // 肩部
                val leftShoulderPoint = it[leftShoulderIndex]
                val rightShoulderPoint = it[rightShoulderIndex]

                // 手腕
                val leftWristPoint = it[leftWristIndex]
                val rightWristPoint = it[rightWristIndex]

                // 手肘
                val leftElbowPoint = it[leftElbowIndex]
                val rightElbowPoint = it[rightElbowIndex]

                // 脚踝
                val leftAnklePoint = it[leftAnkleIndex]
                val rightAnklePoint = it[rightAnkleIndex]

                // 臀部
                val leftHipPoint = it[leftHipIndex]
                val rightHipPoint = it[rightHipIndex]

                // 鼻子
                val nosePoint = it[noseIndex]

                // 是否超出屏幕了
                val isOutScreen = checkPersonOutsideOfScreen(
                    leftWristPoint, rightWristPoint, leftAnklePoint, rightAnklePoint
                )

                Timber.i("超出屏幕：${isOutScreen}   部位置信度： 左手${leftWristPoint.score}   右手${rightWristPoint.score}   左脚${leftAnklePoint.score}   右脚${rightAnklePoint.score}")

                // 获取手臂开合状态 + 置信度
                val armState = getArmState(
                    nosePoint = nosePoint,
                    leftShoulderPoint = leftShoulderPoint,
                    rightShoulderPoint = rightShoulderPoint,
                    leftWristPoint = leftWristPoint,
                    rightWristPoint = rightWristPoint,
                    leftElbowPoint = leftElbowPoint,
                    rightElbowPoint = rightElbowPoint,
                    leftHipPoint = leftHipPoint,
                    rightHipPoint = rightHipPoint,
                )

                val ankleIsOpen = poseUtil.checkAnkleIsOpen(
                    leftShoulderPoint, rightShoulderPoint, leftAnklePoint, rightAnklePoint
                )

                // 手臂的姿势 和 可信度
                val armStateValue = armState.first
                val armConfidence = armState.second

                if (!isOutScreen) {
                    // 返回结果 动作状态 + 可信度
                    val poseConfidence: Float
                    val poseState = if (ankleIsOpen) {
                        // 脚 开 状态
                        when (armStateValue) {
                            STAR_JUMP_ARM_OPEN -> {
                                //  脚 开 手开 ->正常开状态
                                Timber.i("脚开手开 可信度${armConfidence}")
                                poseConfidence = armConfidence
                                if (armConfidence == 1f) {
                                    // 如果腿部之前有改变
                                    if (legIsResetState.get()) {
                                        // 现在标记为没改变 需改变一次腿才为正确的
                                        legIsResetState.set(false)
                                        StarJumpTypes.OPEN
                                    } else {
                                        StarJumpTypes.OTHER
                                    }
                                } else {
                                    StarJumpTypes.OPEN
                                }
                            }

                            STAR_JUMP_ARM_CLOSE -> {
                                //  脚 开 手合 ->不协调合
                                Timber.i("脚开手合 可信度${armConfidence}")
                                poseConfidence = armConfidence
                                StarJumpTypes.ERROR_CLOSE
                            }

                            else -> {
                                // 其它状态
                                Timber.i("脚开其它状态 可信度1")
                                poseConfidence = 1f
                                StarJumpTypes.OTHER
                            }
                        }
                    } else {
                        // 如果腿不是初始状态 改为初始状态
                        if (!legIsResetState.get()) {
                            legIsResetState.set(true)
                        }

                        // 脚 合 状态
                        when (armStateValue) {
                            STAR_JUMP_ARM_OPEN -> {
                                // 脚合 手开 动作不协调
                                Timber.i("脚合手开 可信度${armConfidence}")
                                poseConfidence = armConfidence
                                StarJumpTypes.ERROR_OPEN
                            }

                            STAR_JUMP_ARM_CLOSE -> {
                                // 脚合 手合 合并动作
                                Timber.i("脚合手合 可信度${armConfidence}")
                                poseConfidence = armConfidence
                                StarJumpTypes.CLOSE
                            }

                            else -> {
                                Timber.i("脚合其它状态 可信度1")
                                poseConfidence = 1f
                                StarJumpTypes.OTHER
                            }
                        }
                    }
                    output.add(Pair(poseState.position, poseConfidence))
                } else {
                    output.add(Pair(StarJumpTypes.OUTSIDE_SCREEN.position, 1f))
                }
                output.add(
                    Pair(
                        StarJumpTypes.OUTSIDE_SCREEN.position, isOutScreen.toInt().toFloat()
                    )
                )
            } catch (e: Exception) {
                Timber.e("处理关节数据发生异常：${e.message}")
                e.printStackTrace()
            }
        }
        return output
    }

    /**
     *  手腕+脚踝可信度超过0.1就确定它在屏幕中
     */
    private fun checkPersonOutsideOfScreen(
        leftWristPoint: KeyPoint,
        rightWristPoint: KeyPoint,
        leftAnklePoint: KeyPoint,
        rightAnklePoint: KeyPoint,
    ) = leftWristPoint.score.toDouble() < 0.10 || rightWristPoint.score.toDouble() < 0.10
            || leftAnklePoint.score.toDouble() < 0.10 || rightAnklePoint.score.toDouble() < 0.10

    /**
     *  获取手臂  开合状态+置信度
     */
    private fun getArmState(
        nosePoint: KeyPoint,
        leftShoulderPoint: KeyPoint,
        rightShoulderPoint: KeyPoint,
        leftWristPoint: KeyPoint,
        rightWristPoint: KeyPoint,
        leftElbowPoint: KeyPoint,
        rightElbowPoint: KeyPoint,
        leftHipPoint: KeyPoint,
        rightHipPoint: KeyPoint,
    ): Pair<Int, Float> {
        // 判断手腕是否高于肩部
        val wristOnShoulderScore = poseUtil.checkTargetPointOnOtherPoint(
            leftWristPoint, rightWristPoint, leftShoulderPoint, rightShoulderPoint
        )
        // 双手腕高于肩部
        val wristIsOnShoulder = wristOnShoulderScore == 2

        // 判断手腕是否高过手肘
        val wristOnElbowScore = poseUtil.checkTargetPointOnOtherPoint(
            leftWristPoint, rightWristPoint, leftElbowPoint, rightElbowPoint
        )
        // 双手手腕是否高于手肘
        val wristIsOnElbow = wristOnElbowScore == 2

        // 手肘是否在肩部之上
        val elbowOnShoulderScore = poseUtil.checkTargetPointOnOtherPoint(
            leftElbowPoint, rightElbowPoint,
            leftShoulderPoint, rightShoulderPoint,
        )
        // 双手肘是否高于双肩
        val elbowIsOnShoulder = elbowOnShoulderScore == 2

        showPointPositionInfo("手腕", "肩部", wristOnShoulderScore)
        showPointPositionInfo("手腕", "手肘", wristOnElbowScore)
        showPointPositionInfo("手肘", "肩部", elbowOnShoulderScore)

        // 返回的结果
        val resultArmState: Int
        var resultArmPoseConfidence = 0f

        // 如果  双手腕在肩部之上  且  双手腕在手肘之上
        if (wristIsOnShoulder && elbowIsOnShoulder) {
            // 手腕在手肘之上
            if (wristIsOnElbow) {
                Timber.i("状态-：  双手举过头顶")
                resultArmPoseConfidence = poseUtil.getThreePointAngle(
                    nosePoint,
                    nosePoint,
                    leftShoulderPoint,
                    rightShoulderPoint,
                    leftWristPoint,
                    rightWristPoint,
                    armKeyOpenAngle,
                    "鼻-肩部-手腕"
                )
                resultArmState = STAR_JUMP_ARM_OPEN
            } else {
                // 未知状态
                resultArmState = STAR_JUMP_ARM_OTHER
                Timber.w("状态-：手腕在手肘之下")
            }
        } else {
            if (wristOnShoulderScore == 0 && elbowOnShoulderScore == 0) {
                // 手臂在肩部以下
                Timber.i("状态-：手臂低于肩部")
                resultArmState = STAR_JUMP_ARM_CLOSE
                resultArmPoseConfidence = poseUtil.getThreePointAngle(
                    leftHipPoint,
                    rightHipPoint,
                    leftShoulderPoint,
                    rightShoulderPoint,
                    leftWristPoint,
                    rightWristPoint,
                    armKeyCloseAngle,
                    "臀部-肩部-手腕"
                )
            } else {
                resultArmState = STAR_JUMP_ARM_OTHER
                Timber.i("状态-：其它")
            }
        }
        return Pair(resultArmState, resultArmPoseConfidence)
    }

    /**
     *  获取目标点(两侧)是否高于其它点(两侧)显示
     */
    private fun showPointPositionInfo(
        targetPointName: String, otherName: String, score: Int
    ) {
        val detailInfo = when (score) {
            2 -> "双侧 ${targetPointName}-> 高于 -${otherName}"
            1 -> "单侧 ${targetPointName}-> 高于 -${otherName}"
            0 -> "双侧 ${targetPointName}-> 低于 -${otherName}"
            else -> "${targetPointName}未知状态"
        }
        val showMsg = "${targetPointName}-${otherName}:   $detailInfo"
        when (score) {
            2 -> Timber.i(showMsg)
            1 -> Timber.w(showMsg)
            else -> Timber.d(showMsg)
        }
    }

    override fun poseIsCorrect(list: List<Person>): Boolean {
        // 开合跳的正常姿态应该是站立 如果需要过滤掉误识别的姿态
        // 需要在这里验证这个姿态是否是站立的
        return true
    }

    override fun close() {}
}
