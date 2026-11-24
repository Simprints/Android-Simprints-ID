package com.simprints.feature.externalcredential.screens.scanocr.usecase

import android.graphics.Bitmap
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrDocumentType
import com.simprints.feature.externalcredential.screens.scanocr.model.ReadoutResult
import com.simprints.feature.externalcredential.screens.scanocr.model.ReadoutSource
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads the credential from a barcode printed on the document, using the reader for the given document type.
 */
@Singleton
internal class ReadBarcodeUseCase @Inject constructor(
    private val readBarcodesFromImage: ReadBarcodesFromImageUseCase,
    private val faydaCardOcrReaderUseCase: FaydaCardOcrReaderUseCase,
) {
    operator fun invoke(
        bitmap: Bitmap,
        documentType: OcrDocumentType,
    ): ReadoutResult? {
        val scanResult = when (documentType) {
            OcrDocumentType.FaydaCard -> faydaCardOcrReaderUseCase.fromBarcode(readBarcodesFromImage(bitmap))
            else -> null
        }
        return scanResult?.let { ReadoutResult(ReadoutSource.BARCODE, it) }
    }
}
