package com.simprints.feature.externalcredential.screens.scanocr.usecase

import android.view.View
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrCropConfig
import javax.inject.Inject

internal class BuildOcrCropConfigUseCase @Inject constructor(
    private val getBoundsRelativeToParentUseCase: GetBoundsRelativeToParentUseCase
) {

    operator fun invoke(rotationDegrees: Int, cameraPreview: View, documentScannerArea: View): OcrCropConfig {
        val cutoutRect = getBoundsRelativeToParentUseCase(parent = cameraPreview, child = documentScannerArea)
        return OcrCropConfig(
            rotationDegrees = rotationDegrees,
            cutoutRect = cutoutRect,
            previewViewWidth = cameraPreview.width,
            previewViewHeight = cameraPreview.height
        )
    }
}
