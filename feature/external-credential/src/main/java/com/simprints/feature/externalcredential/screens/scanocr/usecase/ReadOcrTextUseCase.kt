package com.simprints.feature.externalcredential.screens.scanocr.usecase

import android.graphics.Bitmap
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrDocumentType
import com.simprints.feature.externalcredential.screens.scanocr.model.ReadoutResult
import com.simprints.feature.externalcredential.screens.scanocr.model.ReadoutSource
import com.simprints.feature.externalcredential.screens.scanocr.reader.OcrReader
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads the credential from the text printed on the document, using the reader for the given document type.
 */
@Singleton
internal class ReadOcrTextUseCase @Inject constructor(
    private val readTextFromImage: ReadTextFromImageUseCase,
    private val ghanaNhisCardOcrReaderUseCase: GhanaNhisCardOcrReaderUseCase,
    private val ghanaIdCardOcrReaderUseCase: GhanaIdCardOcrReaderUseCase,
    private val faydaCardOcrReaderUseCase: FaydaCardOcrReaderUseCase,
) {
    operator fun invoke(
        bitmap: Bitmap,
        documentType: OcrDocumentType,
        isCapturingAllFields: Boolean,
    ): ReadoutResult? {
        val ocrReader = OcrReader(readTextFromImage(bitmap) ?: return null)
        val scanResult = when (documentType) {
            OcrDocumentType.NhisCard -> ghanaNhisCardOcrReaderUseCase(ocrReader, isCapturingAllFields)
            OcrDocumentType.GhanaIdCard -> ghanaIdCardOcrReaderUseCase(ocrReader, isCapturingAllFields)
            OcrDocumentType.FaydaCard -> faydaCardOcrReaderUseCase.fromText(ocrReader)
        }
        return scanResult?.let { ReadoutResult(ReadoutSource.TEXT_OCR, it) }
    }
}
