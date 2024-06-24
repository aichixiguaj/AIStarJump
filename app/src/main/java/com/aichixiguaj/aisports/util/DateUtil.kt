package com.aichixiguaj.aisports.util

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

/**
 *    @author   ： AiChiXiGuaJ
 *    @date      ： 2022/3/22 22:19
 *    @email    ： aichixiguaj@qq.com
 *    @desc     :    日期工具
 */
@SuppressLint("SimpleDateFormat")
class DateUtil {

    private lateinit var calendar: Calendar
    private lateinit var dateFormat: SimpleDateFormat
    private lateinit var dateFormat3: SimpleDateFormat
    private lateinit var dateFormat4: SimpleDateFormat
    private val dateType = "yyyy-MM-dd HH:mm:ss"
    private val dateType2 = "yyyy年MM月dd日HH点mm分"
    private val dateType3 = "yyyy-MM-dd"
    private val dateType4 = "HH:mm:ss"

    companion object {

        @Volatile
        private var instance: DateUtil? = null

        @JvmStatic
        @Synchronized
        fun getInstance(): DateUtil {
            if (instance == null) {
                instance = DateUtil()
            }
            return instance!!
        }
    }

    /**
     * 初始化
     */
    private fun init() {
        if (!::calendar.isInitialized) {
            calendar = Calendar.getInstance()
        }
        if (!::dateFormat.isInitialized) {
            dateFormat = SimpleDateFormat(dateType)
        }
    }

    /**
     *  时间戳转字符串时间
     */
    fun timestamp2String(timestamp: Long): String {
        init()
        return dateFormat.format(timestamp)
    }

    /**
     *  获取今天的日期
     */
    fun getCurrentDay(): String {
        if (!::dateFormat3.isInitialized) {
            dateFormat3 = SimpleDateFormat(dateType3)
        }
        return dateFormat3.format(System.currentTimeMillis())
    }

    fun timeMillisToTime(time: Long): String {
        if (!::dateFormat4.isInitialized) {
            dateFormat4 = SimpleDateFormat(dateType4)
        }
        return dateFormat4.format(time)
    }

    /**
     *  获取当前时间
     */
    fun getCurrentDate(): String {
        return SimpleDateFormat(dateType2).format(System.currentTimeMillis())
    }
}