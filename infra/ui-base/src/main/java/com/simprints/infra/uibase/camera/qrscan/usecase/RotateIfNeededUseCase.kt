package com.simprints.infra.uibase.camera.qrscan.usecase

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Matrix
import javax.inject.Inject

class RotateIfNeededUseCase @Inject constructor() {
    operator fun invoke(bitmap: Bitmap, orientation: Int): Bitmap {
        val isLandscape = bitmap.width > bitmap.height
        val isPortraitOrientation = orientation == Configuration.ORIENTATION_PORTRAIT

        return if (isLandscape && isPortraitOrientation) {
            val matrix = Matrix().apply { postRotate(90f) }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else {
            bitmap
        }
    }
}
