package com.aichixiguaj.aisports.data

/**
 *    @author   ： AiChiXiGuaJ
 *    @date      ： 2023/7/19 9:38
 *    @email    ： aichixiguaj@qq.com
 *    @desc     :    开合跳姿势类型
 */
enum class StarJumpTypes(val position: Int) {
    // 开->脚开+头顶手合
    OPEN(0),

    // 合->脚合+腰部手合
    CLOSE(1),

    // 不协调开->脚合+头顶手合
    ERROR_OPEN(2),

    // 不协调合->脚开+腰部手合
    ERROR_CLOSE(3),

    // 其它
    OTHER(5),

    // 超出屏幕
    OUTSIDE_SCREEN(99);

    companion object {
        private val map = values().associateBy(StarJumpTypes::position)
        fun fromInt(position: Int): StarJumpTypes = map.getValue(position)
    }
}