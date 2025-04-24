package com.simprints.feature.externalcredential.screens.ocr.usecase

import android.graphics.Bitmap
import androidx.exifinterface.media.ExifInterface
import javax.inject.Inject
import android.graphics.Matrix

internal class RotateBitmapUseCase @Inject constructor() {

    suspend operator fun invoke(image: Bitmap, exif: ExifInterface): Bitmap {
        val rotationDegrees = when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }

        return if (rotationDegrees != 0) {
            val matrix = Matrix()
            matrix.postRotate(rotationDegrees.toFloat())
            Bitmap.createBitmap(image, 0, 0, image.width, image.height, matrix, true)
        } else {
            image
        }
    }
}
