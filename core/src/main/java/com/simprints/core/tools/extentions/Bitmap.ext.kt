package com.simprints.core.tools.extentions

import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream


fun Bitmap.toByteArray(): ByteArray {
    val byteArrayOutputStream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
    return byteArrayOutputStream.toByteArray()
}

fun ImageProxy.toBitmap(): Bitmap {
    val buffer = planes[0].buffer
    val pixelStride = planes[0].pixelStride
    val rowStride = planes[0].rowStride
    val rowPadding = rowStride - pixelStride * width
    val bitmap = Bitmap.createBitmap(
        width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888
    )
    bitmap.copyPixelsFromBuffer(buffer)
    return bitmap
}
