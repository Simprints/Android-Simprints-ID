package com.simprints.infra.uibase.camera.qrscan.usecase

import android.graphics.Bitmap
import android.graphics.Rect
import com.simprints.infra.uibase.camera.qrscan.QrCodeAnalyzer
import javax.inject.Inject

class CropBitmapAreaForDetectionUseCase @Inject constructor(
    private val rotateToPortraitIfNeededUseCase: RotateToPortraitIfNeededUseCase,
    private val mapCropRectToImageSpaceUseCase: MapCropRectToImageSpaceUseCase,
    private val cropBitmapToRectUseCase: CropBitmapToRectUseCase
) {
    /**
     * Takes a [bitmap] image, and crops it to the area specified in [cropConfig]
     */
    operator fun invoke(bitmap: Bitmap, cropConfig: QrCodeAnalyzer.CropConfig): Bitmap {
        val rotatedBitmap: Bitmap = rotateToPortraitIfNeededUseCase(bitmap, cropConfig.orientation)
        val crop: Rect = mapCropRectToImageSpaceUseCase(
            cropRectInRoot = cropConfig.rect,
            rootWidth = cropConfig.rootViewWidth,
            rootHeight = cropConfig.rootViewHeight,
            imageWidth = rotatedBitmap.width,
            imageHeight = rotatedBitmap.height
        )
        val isLeftOutOfBounds = crop.left < 0
        val isTopOutOfBounds = crop.top < 0
        val isRightOutOfBounds = crop.right > rotatedBitmap.width
        val isBottomOutOfBounds = crop.bottom > rotatedBitmap.height

        // a safety check to ensure that crop area is fully inside the rotated bitmap bounds
        return if (isLeftOutOfBounds || isTopOutOfBounds || isRightOutOfBounds || isBottomOutOfBounds) {
            bitmap
        } else {
            cropBitmapToRectUseCase(rotatedBitmap, crop)
        }
    }
}
