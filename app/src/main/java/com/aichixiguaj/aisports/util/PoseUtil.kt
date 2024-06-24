package com.aichixiguaj.aisports.util

import com.aichixiguaj.aisports.data.KeyPoint
import com.aichixiguaj.aisports.ext.toInt
import timber.log.Timber
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.max

/**
 *    @author   ： AiChiXiGuaJ
 *    @date      ： 2023/7/19 13:57
 *    @email    ： aichixiguaj@qq.com
 *    @desc     :   姿态工具
 *
 *    后置摄像头显示逻辑：
 *         左肩显示在右边屏幕  符合看别人的逻辑
 *    前置摄像头显示逻辑
 *         返回的数据是左肩 但实际是右肩  做了镜像反转  不做反转模型处理关节点乱跳
 *    所以要特别注意 X 轴的计算
 */
@Suppress("MemberVisibilityCanBePrivate")
class PoseUtil {

    /**
     *  检查脚腕距离是否大于肩宽
     *  @return true 脚间距大于肩宽  false 脚间距小于或等于肩宽
     */
    fun checkAnkleIsOpen(
        leftShoulderPoint: KeyPoint,
        rightShoulderPoint: KeyPoint,
        leftAnklePoint: KeyPoint,
        rightAnklePoint: KeyPoint
    ): Boolean {
        val shoulderDistance = getPointXDistance(leftShoulderPoint, rightShoulderPoint)
        val ankleDistance = getPointXDistance(leftAnklePoint, rightAnklePoint)
        return ankleDistance > shoulderDistance
    }

    /**
     *  检查 目标点(目标)是否在 某点(两侧)之上
     *  @return 0：两侧都低于   1：一侧高于   2：两侧都高于
     */
    fun checkTargetPointOnOtherPoint(
        leftTargetPoint: KeyPoint,
        rightTargetPoint: KeyPoint,
        leftOtherPoint: KeyPoint,
        rightOtherPoint: KeyPoint,
    ): Int {
        val leftOnState = checkTargetOnOtherPointOneSide(leftTargetPoint, leftOtherPoint)
        val rightOnState = checkTargetOnOtherPointOneSide(rightTargetPoint, rightOtherPoint)
        return leftOnState + rightOnState
    }

    /**
     *  检查目标点高于其他点之上
     *  @return 0 低于  1高于
     */
    fun checkTargetOnOtherPointOneSide(
        targetPoint: KeyPoint,
        otherPoint: KeyPoint,
    ): Int {
        val targetY = targetPoint.coordinate.y
        val otherY = otherPoint.coordinate.y
        return (targetY < otherY).toInt()
    }

    /**
     *  获取点与点的横向间距
     */
    fun getPointXDistance(
        leftPoint: KeyPoint,
        rightPoint: KeyPoint
    ): Float {
        val leftX = leftPoint.coordinate.x
        val rightX = rightPoint.coordinate.x
        return abs(rightX - leftX)
    }

    /**
     *   计算三个点接近某个角度浮动制
     *
     *  StartPoint   开始点
     *  anglePoint 角度点
     *  endPoint   结束点
     *  keyAngle 接近角度点
     *
     *  注意：如果是前置摄像头
     *  左边的数据其实是右边的 比如程序发送的左肩其实是右肩数据
     *  因为前置摄像头做了镜像反转  不做反转骨骼点会识别不准不知道为啥
     *
     *  @return 00-1.0 越大越接近
     */
    fun getThreePointAngle(
        // 开始点
        leftStartPoint: KeyPoint,
        rightStartPoint: KeyPoint,
        // 角度点
        leftAnglePoint: KeyPoint,
        rightAnglePoint: KeyPoint,
        // 结束点
        leftEndPoint: KeyPoint,
        rightEndPoint: KeyPoint,
        // 接近角度点
        keyAngle: Float,
    ): Float {
        val confidenceLeft =
            getAngleApproachingTheValue(leftStartPoint, leftAnglePoint, leftEndPoint, keyAngle)
        val confidenceRight =
            getAngleApproachingTheValue(rightStartPoint, rightAnglePoint, rightEndPoint, keyAngle)
        return (confidenceLeft + confidenceRight) / 2
    }

    fun getThreePointAngle(
        // 开始点
        leftStartPoint: KeyPoint,
        rightStartPoint: KeyPoint,
        // 角度点
        leftAnglePoint: KeyPoint,
        rightAnglePoint: KeyPoint,
        // 结束点
        leftEndPoint: KeyPoint,
        rightEndPoint: KeyPoint,
        // 接近角度点
        keyAngle: Float,
        // 计算名称
        name: String,
    ): Float {
        val confidenceLeft =
            getAngleApproachingTheValue(leftStartPoint, leftAnglePoint, leftEndPoint, keyAngle)
        val confidenceRight =
            getAngleApproachingTheValue(rightStartPoint, rightAnglePoint, rightEndPoint, keyAngle)
        Timber.i("$name 左：${confidenceLeft}   右：${confidenceRight}")
        return (confidenceLeft + confidenceRight) / 2
    }

    /**
     *  获取三个点的角度 相对 一个角度的接近比例
     *  @return 0.0-1.0
     */
    fun getAngleApproachingTheValue(
        // 开始点
        startPoint: KeyPoint,
        // 角度点
        anglePoint: KeyPoint,
        // 结束点
        endPoint: KeyPoint,
        // 接近角度点
        keyAngle: Float,
    ): Float {
        // 获取到三个点的角度
        val angle = getAngle(startPoint, anglePoint, endPoint)
        // 如果偏移值越小那么就代表手臂接近头部
        val angleOffset = angle - keyAngle
        // 返回比例值
        return 1 - max(angleOffset, 0f) / 90
    }

    /**
     *  获取三个点的角度
     */
    fun getAngle(firstPoint: KeyPoint, midPoint: KeyPoint, lastPoint: KeyPoint): Float {

        val lastAndMidPointAngle = atan2(
            lastPoint.coordinate.y.toDouble() - midPoint.coordinate.y,
            lastPoint.coordinate.x.toDouble() - midPoint.coordinate.x
        )

        val firstAndMidPointAngle = atan2(
            firstPoint.coordinate.y - midPoint.coordinate.y,
            firstPoint.coordinate.x - midPoint.coordinate.x
        )

        var result = abs(Math.toDegrees(lastAndMidPointAngle - firstAndMidPointAngle))
        if (result > 180) {
            result = 360.0 - result
        }
        return result.toFloat()
    }

}