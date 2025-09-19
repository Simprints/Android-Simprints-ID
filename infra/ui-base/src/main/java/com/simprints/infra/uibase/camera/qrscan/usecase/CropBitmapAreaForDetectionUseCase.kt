package com.simprints.infra.uibase.camera.qrscan.usecase

import android.graphics.Bitmap
import com.simprints.infra.uibase.camera.qrscan.QrCodeAnalyzer
import javax.inject.Inject

class CropBitmapAreaForDetectionUseCase @Inject constructor(
    private val rotateIfNeededUseCase: RotateIfNeededUseCase,
    private val mapCropRectToImageSpaceUseCase: MapCropRectToImageSpaceUseCase,
    private val cropBitmapToRectUseCase: CropBitmapToRectUseCase
) {
    operator fun invoke(bitmap: Bitmap, cropConfig: QrCodeAnalyzer.CropConfig): Bitmap {
        val rotatedBitmap = rotateIfNeededUseCase(bitmap, cropConfig.orientation)
        val crop = mapCropRectToImageSpaceUseCase(
            cropRectInRoot = cropConfig.rect,
            rootWidth = cropConfig.rootViewWidth,
            rootHeight = cropConfig.rootViewHeight,
            imageWidth = rotatedBitmap.width,
            imageHeight = rotatedBitmap.height
        )
        return if (crop.left < 0 || crop.top < 0 || crop.right > rotatedBitmap.width || crop.bottom > rotatedBitmap.height) {
            bitmap
        } else {
            cropBitmapToRectUseCase(rotatedBitmap, crop)
        }
    }
}
