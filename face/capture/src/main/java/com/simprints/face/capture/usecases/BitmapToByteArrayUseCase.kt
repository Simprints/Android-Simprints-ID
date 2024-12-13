package com.simprints.face.capture.usecases

import android.content.Context
import android.graphics.Bitmap
import com.simprints.core.tools.extentions.dpToPx
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import kotlin.math.max

internal class BitmapToByteArrayUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        const val IMAGE_QUALITY = 100
        const val IMAGE_MAXIMUM_SIZE = 240f
    }

    operator fun invoke(bitmap: Bitmap): ByteArray {
        val resizedBitmap = resizeBitmap(bitmap)

        val byteArrayOutputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, byteArrayOutputStream)

        bitmap.recycle()
        resizedBitmap.recycle()

        return byteArrayOutputStream.toByteArray()
    }

    private fun resizeBitmap(bitmap: Bitmap): Bitmap {
        val maximumSize = IMAGE_MAXIMUM_SIZE.dpToPx(context)
        if (bitmap.width <= maximumSize && bitmap.height <= maximumSize) return bitmap
        // Calculate the scale factor
        val scaleFactor = max(bitmap.width / maximumSize, bitmap.height / maximumSize)
        // Calculate the new dimensions
        val newWidth = (bitmap.width / scaleFactor).toInt()
        val newHeight = (bitmap.height / scaleFactor).toInt()

        // Resize the bitmap
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
}
