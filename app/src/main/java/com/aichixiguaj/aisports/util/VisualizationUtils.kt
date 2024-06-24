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

package com.aichixiguaj.aisports.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.aichixiguaj.aisports.data.BodyPart
import com.aichixiguaj.aisports.data.Person
import kotlin.math.max

object VisualizationUtils {
    /** Radius of circle used to draw keypoints.  */
    private const val CIRCLE_RADIUS = 6f

    /** Width of line used to connected two keypoints.  */
    private const val LINE_WIDTH = 4f

    /** The text size of the person id that will be displayed when the tracker is available.  */
    private const val PERSON_ID_TEXT_SIZE = 30f

    /** Distance from person id to the nose keypoint.  */
    private const val PERSON_ID_MARGIN = 6f

    /** Pair of keypoints to draw lines between.  */
    private val bodyJoints = listOf(
        Pair(BodyPart.NOSE, BodyPart.LEFT_EYE),
        Pair(BodyPart.NOSE, BodyPart.RIGHT_EYE),
        Pair(BodyPart.LEFT_EYE, BodyPart.LEFT_EAR),
        Pair(BodyPart.RIGHT_EYE, BodyPart.RIGHT_EAR),
        Pair(BodyPart.NOSE, BodyPart.LEFT_SHOULDER),
        Pair(BodyPart.NOSE, BodyPart.RIGHT_SHOULDER),
        Pair(BodyPart.LEFT_SHOULDER, BodyPart.LEFT_ELBOW),
        Pair(BodyPart.LEFT_ELBOW, BodyPart.LEFT_WRIST),
        Pair(BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_ELBOW),
        Pair(BodyPart.RIGHT_ELBOW, BodyPart.RIGHT_WRIST),
        Pair(BodyPart.LEFT_SHOULDER, BodyPart.RIGHT_SHOULDER),
        Pair(BodyPart.LEFT_SHOULDER, BodyPart.LEFT_HIP),
        Pair(BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_HIP),
        Pair(BodyPart.LEFT_HIP, BodyPart.RIGHT_HIP),
        Pair(BodyPart.LEFT_HIP, BodyPart.LEFT_KNEE),
        Pair(BodyPart.LEFT_KNEE, BodyPart.LEFT_ANKLE),
        Pair(BodyPart.RIGHT_HIP, BodyPart.RIGHT_KNEE),
        Pair(BodyPart.RIGHT_KNEE, BodyPart.RIGHT_ANKLE)
    )

    private val paintCircle = Paint().apply {
        strokeWidth = CIRCLE_RADIUS
        color = Color.parseColor("#7F06DFDF")
        style = Paint.Style.FILL
    }
    private val paintLine = Paint().apply {
        strokeWidth = LINE_WIDTH
        color = Color.parseColor("#7F00FFFF")
        style = Paint.Style.STROKE
    }

    private val paintText = Paint().apply {
        textSize = PERSON_ID_TEXT_SIZE
        color = Color.BLUE
        textAlign = Paint.Align.LEFT
    }


    // Draw line and point indicate body pose
    fun drawBodyKeypoints(
        input: Bitmap,
        persons: List<Person>,
        isTrackerEnabled: Boolean = false,
        showPersonCircles: Boolean = false,
    ): Bitmap {
        val output = input.copy(Bitmap.Config.ARGB_8888, true)
        val originalSizeCanvas = Canvas(output)
        persons.forEach { person ->
            // draw person id if tracker is enable
            if (isTrackerEnabled) {
                person.boundingBox?.let {
                    val personIdX = max(0f, it.left)
                    val personIdY = max(0f, it.top)
                    originalSizeCanvas.drawText(
                        person.id.toString(),
                        personIdX,
                        personIdY - PERSON_ID_MARGIN,
                        paintText
                    )
                    originalSizeCanvas.drawRect(it, paintLine)
                }
            }
            bodyJoints.forEach {
                if (it.first == BodyPart.LEFT_EYE || it.first == BodyPart.RIGHT_EYE || it.first == BodyPart.NOSE) {
                    // 不绘制
                } else {
                    val keyPointA = person.keyPoints[it.first.position]
                    val keyPointB = person.keyPoints[it.second.position]

                    val pointA = keyPointA.coordinate
                    val pointB = keyPointB.coordinate

                    originalSizeCanvas.drawLine(
                        pointA.x, pointA.y,
                        pointB.x, pointB.y,
                        paintLine
                    )
                }
            }

            if (showPersonCircles) {
                person.keyPoints.forEach { point ->
                    val position = point.bodyPart.position
                    if (position == BodyPart.LEFT_EYE.position
                        || position == BodyPart.RIGHT_EYE.position
                        || position == BodyPart.LEFT_EAR.position
                        || position == BodyPart.RIGHT_EAR.position
                    ) {
                        // 不绘制
                    } else {
                        // TODO 删除
//                        if (position == BodyPart.LEFT_SHOULDER.position) {
//                            paintCircle.color = Color.RED
//                        } else {
//                            paintCircle.color = Color.TRANSPARENT
//                        }

                        originalSizeCanvas.drawCircle(
                            point.coordinate.x,
                            point.coordinate.y,
                            CIRCLE_RADIUS,
                            paintCircle
                        )
                    }
                }
            }
        }
        return output
    }
}
