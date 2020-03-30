package com.simprints.core.tools.extentions

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer


fun Bitmap.toByteArray(): ByteArray {
    val byteArrayOutputStream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
    return byteArrayOutputStream.toByteArray()
}
