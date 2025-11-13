package com.simprints.infra.uibase.camera.qrscan.usecase

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Matrix
import javax.inject.Inject

class RotateToPortraitIfNeededUseCase @Inject constructor() {
    /**
     * Rotates given [bitmap] by 90 degrees if it is in landscape mode. Otherwise, returns the original [bitmap] without modification.
     * Images from video feed are usually in landscape mode, and we want to be sure that the calculation are always happening in the same
     * space.
     *
     * @param bitmap image to potentially rotate
     * @param orientation the current device orientation from [Configuration] (portrait/landscape)
     * @return a rotated bitmap if rotation was needed, otherwise the original bitmap
     */
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
