package com.simprints.face.capture

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.PixelFormat
import android.graphics.Rect
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream


fun Bitmap.toByteArray(): ByteArray {
    val byteArrayOutputStream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
    return byteArrayOutputStream.toByteArray()
}

fun ImageProxy.toBitmap(cropRect: Rect, rotationMatrix: Matrix): Bitmap {
    require(format == PixelFormat.RGBA_8888)
    val buffer = planes[0].buffer
    val pixelStride = planes[0].pixelStride
    val rowStride = planes[0].rowStride
    val rowPadding = rowStride - pixelStride * width
    val bitmap = Bitmap.createBitmap(
        width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888
    )
    bitmap.copyPixelsFromBuffer(buffer)
    val croppedRotatedBitmap = Bitmap.createBitmap(
        bitmap,
        cropRect.left,
        cropRect.top,
        cropRect.width(),
        cropRect.height(),
        rotationMatrix,
        true
    )
    bitmap.recycle()
    return croppedRotatedBitmap
}
/*
return with(cropRect) {
            Bitmap.createBitmap(
                image.toBitmap(),
                left,
                top,
                width(),
                height(),
                rotationMatrix,
                true
            )
        }
 */
