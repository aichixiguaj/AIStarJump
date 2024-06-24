package com.aichixiguaj.aisports.data

import android.util.Size
import java.util.Comparator

/**
 *    @author   ： AiChiXiGuaJ
 *    @date      ： 2023/7/17 17:06
 *    @email    ： aichixiguaj@qq.com
 *    @desc     :
 */
class CompareSizesByArea : Comparator<Size> {
    override fun compare(lhs: Size, rhs: Size): Int {
        return java.lang.Long.signum(lhs.width.toLong() * lhs.height - rhs.width.toLong() * rhs.height)
    }
}