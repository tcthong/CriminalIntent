package com.example.criminalintent

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.util.DisplayMetrics
import kotlin.math.ceil
import kotlin.math.max

fun getScaledBitmap(path: String, activity: Activity): Bitmap {
    val size = Point()
    activity.windowManager.defaultDisplay.getRealSize(size)
    return getScaledBitmap(path, size.x, size.y)
}

fun getScaledBitmap(path: String, destWidth: Int, destHeight: Int): Bitmap {
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(path, options)

    var inSampleSize = 1
    val originalWidth = options.outWidth.toFloat()
    val originalHeight = options.outHeight.toFloat()

    if (originalHeight > destHeight || originalWidth > destWidth) {
        val scaledHeight = originalHeight / destHeight
        val scaledWidth = originalWidth / destWidth

        inSampleSize = ceil(max(scaledHeight, scaledWidth)).toInt()
    }

    options.inSampleSize = inSampleSize
    options.inJustDecodeBounds = false

    return BitmapFactory.decodeFile(path, options)
}