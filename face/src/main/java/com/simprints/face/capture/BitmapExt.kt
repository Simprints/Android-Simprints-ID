package com.simprints.face.capture

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.PixelFormat
import android.graphics.Rect
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream


fun Bitmap.toJPGCompressedByteArray(): ByteArray {
    val byteArrayOutputStream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
    return byteArrayOutputStream.toByteArray()
}

/**
 * Convert ImageProxy image To bitmap then crop and rotate it.
 * Image format should be RGBA_8888
 *
 * @param cropRect
 * @param rotationMatrix
 * @return
 */

fun ImageProxy.toBitmap(cropRect: Rect ): Bitmap {
    require(format == PixelFormat.RGBA_8888){
        "$format is not supported. RGBA_8888 is the only supported image format"
    }
    val buffer = planes[0].buffer
    val pixelStride = planes[0].pixelStride
    val rowStride = planes[0].rowStride
    val rowPadding = rowStride - pixelStride * width
    val bitmap = Bitmap.createBitmap(
        width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888
    )
    bitmap.copyPixelsFromBuffer(buffer)
    val rotationMatrix = Matrix()
    rotationMatrix.postRotate(imageInfo.rotationDegrees.toFloat())
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
