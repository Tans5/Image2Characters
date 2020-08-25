package com.tans.image2characters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.math.RoundingMode
import kotlin.math.max

fun Context.getImageOrientation(uri: Uri): Int {
    val iStream = contentResolver.openInputStream(uri)
    return if (iStream != null) {
        val exif = ExifInterface(iStream)
        when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, Int.MAX_VALUE)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    } else {
        0
    }
}

fun Context.getBitmapWithMaxSize(
    maxWidth: Float,
    maxHeight: Float,
    uri: Uri,
    bitmapConfig: Bitmap.Config = Bitmap.Config.RGB_565
): Bitmap {
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeStream(contentResolver.openInputStream(uri), null, options)
    val bWidth = options.outWidth
    val bHeight = options.outHeight
    val scaleWidth = bWidth / maxWidth
    val scaleHeight = bHeight / maxHeight
    val scale = if (scaleWidth > scaleHeight) {
        max(1f, scaleWidth)
    } else {
        max(1f, scaleHeight)
    }
    options.inJustDecodeBounds = false
    options.inPreferredConfig = bitmapConfig
    options.inSampleSize = scale.toBigDecimal().setScale(0, RoundingMode.UP).toInt()

    return BitmapFactory.decodeStream(contentResolver.openInputStream(uri), null, options)
        ?: error("$uri can't create Bitmap")
}

fun Bitmap.rotation(degrees: Int): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(degrees.toFloat(), width / 2f, height / 2f)
    return transform(matrix, width, height)
}

fun Bitmap.transform(matrix: Matrix, dstWidth: Int, dstHeight: Int): Bitmap {
    return Bitmap.createBitmap(this, 0, 0, dstWidth, dstHeight, matrix, false)
}