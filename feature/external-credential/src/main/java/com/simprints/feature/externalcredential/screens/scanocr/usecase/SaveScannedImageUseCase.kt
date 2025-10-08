package com.simprints.feature.externalcredential.screens.scanocr.usecase

import android.content.Context
import android.graphics.Bitmap
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrDocumentType
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

internal class SaveScannedImageUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    /**
     * Saves a bitmap to the application's cache directory as a JPEG file.
     * @param bitmap the bitmap to save
     * @return absolute path to the saved file
     */
    operator fun invoke(
        bitmap: Bitmap,
        documentType: OcrDocumentType,
        imageType: ScanImageType,
    ): String {
        val documentTypeName = documentType.toString().trim().replace(" ", "")
        val fileName = "ocr_scan_${documentTypeName}_${imageType}_${System.currentTimeMillis()}.jpg"
        val file = File(context.cacheDir, fileName)

        file.outputStream().use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        }

        return file.absolutePath
    }

    enum class ScanImageType {
        FullDocument,
        ZoomedInCredential,
    }
}
