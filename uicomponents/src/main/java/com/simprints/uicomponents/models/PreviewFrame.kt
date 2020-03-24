package com.simprints.uicomponents.models

import android.graphics.*
import java.io.ByteArrayOutputStream

class PreviewFrame(
    val width: Int,
    val height: Int,
    val format: Int,
    val mirrored: Boolean,
    val bytes: ByteArray
) {
    fun toBitmap(croppingRegion: Rect = Rect(0, 0, width, height)): Bitmap {
        val yuvImage = YuvImage(bytes, ImageFormat.NV21, width, height, null)
        return ByteArrayOutputStream()
            .also { yuvImage.compressToJpeg(croppingRegion, 100, it) }
            .toByteArray()
            .let { BitmapFactory.decodeByteArray(it, 0, it.size) }
    }
}
