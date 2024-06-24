package com.aichixiguaj.aisports.util

import com.google.gson.Gson

/**
 *    @author   ： AiChiXiGuaJ
 *    @date      ： 2023/2/28 15:45
 *    @email    ： aichixiguaj@qq.com
 *    @desc     :
 */
object UtilObject {

    lateinit var cacheRepository: CacheRepository
        private set

    lateinit var dateUtil: DateUtil
        private set

    lateinit var gson: Gson
        private set

    fun init() {
        cacheRepository = CacheRepository()
        gson = Gson()
        dateUtil = DateUtil()
    }

}