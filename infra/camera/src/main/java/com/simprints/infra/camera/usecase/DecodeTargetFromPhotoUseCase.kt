package com.simprints.infra.camera.usecase

import android.graphics.Bitmap
import android.graphics.BitmapRegionDecoder
import android.graphics.Matrix
import android.graphics.Rect
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import javax.inject.Inject

/**
 * Turns a still photo into an upright bitmap of just the scanning window, keeping the photo's full detail.
 *
 * Only the window is decoded from the photo, so the rest of the photo never becomes a bitmap for memory optimizations.
 */
internal class DecodeTargetFromPhotoUseCase @Inject constructor(
    private val getTargetRegionInPhotoUseCase: GetTargetRegionInPhotoUseCase,
) {
    @ExcludedFromGeneratedTestCoverageReports(reason = "Android bitmap decoding")
    operator fun invoke(
        jpeg: ByteArray,
        rotationDegrees: Int,
        previewRect: Rect,
        targetRect: Rect,
    ): Bitmap {
        @Suppress("DEPRECATION") // the non-deprecated overload needs API 31
        val decoder = BitmapRegionDecoder.newInstance(jpeg, 0, jpeg.size, false)
        val region = getTargetRegionInPhotoUseCase(decoder.width, decoder.height, rotationDegrees, previewRect, targetRect)
        val window = decoder.decodeRegion(region, null)
        decoder.recycle()
        if (rotationDegrees == 0) return window
        val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
        return Bitmap.createBitmap(window, 0, 0, window.width, window.height, matrix, true).also { window.recycle() }
    }
}
