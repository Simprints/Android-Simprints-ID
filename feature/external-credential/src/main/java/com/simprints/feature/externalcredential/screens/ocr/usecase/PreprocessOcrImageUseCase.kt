package com.simprints.feature.externalcredential.screens.ocr.usecase

import android.graphics.Bitmap
import androidx.exifinterface.media.ExifInterface
import com.simprints.feature.externalcredential.screens.ocr.model.OcrPreprocessData
import javax.inject.Inject

internal class PreprocessOcrImageUseCase @Inject constructor(
    private val rotateBitmapUseCase: RotateBitmapUseCase,
    private val cropCardAreaUseCase: CropCardAreaUseCase,
) {

    suspend operator fun invoke(
        image: Bitmap,
        exif: ExifInterface,
        ocrPreprocessData: OcrPreprocessData,
    ): Bitmap {
        val rotated = rotateBitmapUseCase(image, exif)
        return cropCardAreaUseCase(
            image = rotated,
            previewViewHeightPx = ocrPreprocessData.previewViewHeightPx,
            previewViewWidthPx = ocrPreprocessData.previewViewWidthPx,
            cutoutRect = ocrPreprocessData.cutoutRect
        )
    }
}
