package com.simprints.feature.consent.screens.consent.tempocr

import android.graphics.Bitmap
import androidx.camera.core.ImageAnalysis
import javax.inject.Inject

class BuildAnalyzerUseCase @Inject constructor(
) {
    private var frameCounter = 0
    operator fun invoke(everyNthFrame: Int, onBitmapReady: (Bitmap, Int) -> Unit): ImageAnalysis.Analyzer {
        return ImageAnalysis.Analyzer { imageProxy ->
            frameCounter++
            if (frameCounter % everyNthFrame == 0) {
                onBitmapReady(imageProxy.toBitmap(), imageProxy.imageInfo.rotationDegrees)
            }
            imageProxy.close()
        }
    }
}
