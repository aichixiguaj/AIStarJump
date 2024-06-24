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

package com.aichixiguaj.aisports.data

enum class BodyPart(val position: Int) {
    // 鼻
    NOSE(0),
    // 眼
    LEFT_EYE(1),
    RIGHT_EYE(2),
    // 耳
    LEFT_EAR(3),
    RIGHT_EAR(4),
    // 肩
    LEFT_SHOULDER(5),
    RIGHT_SHOULDER(6),
    // 手肘
    LEFT_ELBOW(7),
    RIGHT_ELBOW(8),
    // 手腕
    LEFT_WRIST(9),
    RIGHT_WRIST(10),
    // 臀部
    LEFT_HIP(11),
    RIGHT_HIP(12),
    // 膝盖
    LEFT_KNEE(13),
    RIGHT_KNEE(14),
    // 脚踝
    LEFT_ANKLE(15),
    RIGHT_ANKLE(16);
    companion object{
        private val map = values().associateBy(BodyPart::position)
        fun fromInt(position: Int): BodyPart = map.getValue(position)
    }
}
