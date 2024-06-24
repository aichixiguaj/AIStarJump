package com.aichixiguaj.aisports.ml

import com.aichixiguaj.aisports.data.Person

interface BasePoseClassifier {

    fun classify(person: Person?): List<Pair<Int, Float>>

    /**
     *  作用：防止误识别  可能脚的关节点在比手还高这种需要过滤
     *  如果不做过滤直接返回true即可
     *  如果做过滤需要判别各种姿态的相对坐标
     */
    fun poseIsCorrect(list: List<Person>): Boolean

    fun close()
}