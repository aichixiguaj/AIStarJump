package com.aichixiguaj.aisports.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import java.nio.ByteBuffer


class BitmapUtil {

    // Convert drawable to ByteBuffer
    fun convertDrawableToByteBuffer(
        context: Context,
        drawableId: Int
    ): ByteBuffer {
        // Get the drawable from resources
        val drawable = AppCompatResources.getDrawable(context,drawableId)

        // Convert drawable to bitmap
        val bitmap = convertDrawableToBitmap(drawable)

        // Convert bitmap to ByteBuffer
        return convertBitmapToByteBuffer(bitmap)
    }

    // Convert drawable to bitmap
    fun convertDrawableToBitmap(drawable: Drawable?): Bitmap {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        val bitmap = Bitmap.createBitmap(
            drawable!!.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    // Convert bitmap to ByteBuffer
    fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocate(bitmap.rowBytes * bitmap.height)
        bitmap.copyPixelsToBuffer(byteBuffer)
        byteBuffer.rewind() // Rewind the buffer to zero
        return byteBuffer
    }

}