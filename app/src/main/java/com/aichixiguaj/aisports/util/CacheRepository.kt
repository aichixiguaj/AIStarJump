package com.aichixiguaj.aisports.util

import com.aichixiguaj.aisports.const_value.ConstValue

/**
 *    @author   ： AiChiXiGuaJ
 *    @date      ： 2022/11/1 14:44
 *    @email    ： aichixiguaj@qq.com
 *    @desc     :   缓存仓库
 */
@Suppress("unused")
class CacheRepository : BaseCacheRepository() {

    fun saveScreenWidthHeight(width: Int, height: Int) {
        setIntValue(ConstValue.KEY_SCREEN_WIDTH, width)
        setIntValue(ConstValue.KEY_SCREEN_HEIGHT, height)
    }

    fun getScreenWidth() = getIntValue(ConstValue.KEY_SCREEN_WIDTH, 0)
    fun getScreenHeight() = getIntValue(ConstValue.KEY_SCREEN_HEIGHT, 0)

    fun saveObject(key: String, data: Any) {
        val toJson = UtilObject.gson.toJson(data)
        setStringValue(key, toJson)
    }

    inline fun <reified T> getObject(key: String): T? {
        val getStr = getStringValue(key, "")
        var resultData: T? = null
        try {
            resultData = UtilObject.gson.fromJson(getStr, T::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return resultData
    }

}