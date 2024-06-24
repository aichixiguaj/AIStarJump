package com.aichixiguaj.aisports.util

import com.tencent.mmkv.MMKV

/**
 *    @author   ： AiChiXiGuaJ
 *    @date      ： 2022/3/8 11:27
 *    @email    ： aichixiguaj@qq.com
 *    @desc     :    缓存仓库
 */
@Suppress("unused")
open class BaseCacheRepository {

    // MM KV缓存工具
    private val kvClient = MMKV.defaultMMKV()

    /**
     *  保存布尔值
     */
     fun setBooleanValue(key: String, value: Boolean) = kvClient.encode(key, value)
     fun getBooleanValue(key: String) = kvClient.decodeBool(key)
     fun getBooleanValue(key: String, defaultState: Boolean) =
        kvClient.decodeBool(key, defaultState)

    /**
     *  保存字符串
     */
     fun setStringValue(key: String, value: String) = kvClient.encode(key, value)
     fun getStringValue(key: String) = kvClient.decodeString(key)
     fun getStringValue(key: String, defaultValue: String) =
        kvClient.decodeString(key, defaultValue)

    /**
     *  保存整型
     */
     fun setIntValue(key: String, value: Int) = kvClient.encode(key, value)
     fun getIntValue(key: String) = kvClient.decodeInt(key)
     fun getIntValue(key: String, defaultValue: Int) =
        kvClient.decodeInt(key, defaultValue)


    fun setLongValue(key: String, value: Long) = kvClient.encode(key, value)
    fun getLongValue(key: String) = kvClient.decodeLong(key)
    fun getLongValue(key: String, defaultValue: Long) = kvClient.decodeLong(key, defaultValue)

}