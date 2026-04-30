package com.simprints.feature.externalcredential.screens.scanocr.usecase

import android.graphics.Bitmap
import com.simprints.feature.externalcredential.screens.scanocr.model.DetectedOcrBlock
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrDocumentType
import com.simprints.feature.externalcredential.screens.scanocr.reader.OcrReader
import com.simprints.infra.credential.store.CredentialImageRepository
import com.simprints.infra.credential.store.model.CredentialScanImageType.FullDocument
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.MULTI_FACTOR_ID
import com.simprints.infra.logging.Simber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class GetCredentialCoordinatesUseCase @Inject constructor(
    private val readTextFromImage: ReadTextFromImageUseCase,
    private val ghanaNhisCardOcrSelectorUseCase: GhanaNhisCardOcrSelectorUseCase,
    private val ghanaIdCardOcrSelectorUseCase: GhanaIdCardOcrSelectorUseCase,
    private val credentialImageRepository: CredentialImageRepository,
) {
    suspend operator fun invoke(
        bitmap: Bitmap,
        documentType: OcrDocumentType,
    ): DetectedOcrBlock? {
        return try {
            val ocrText = readTextFromImage(bitmap) ?: return null
            val ocrReader = OcrReader(ocrText)
            val credentialOcrLine = when (documentType) {
                OcrDocumentType.NhisCard -> ghanaNhisCardOcrSelectorUseCase(ocrReader)
                OcrDocumentType.GhanaIdCard -> ghanaIdCardOcrSelectorUseCase(ocrReader)
            }
            if (credentialOcrLine != null) {
                val savedImagePath = credentialImageRepository.saveCredentialScan(bitmap, imageType = FullDocument)
                DetectedOcrBlock(
                    imagePath = savedImagePath,
                    documentType = documentType,
                    blockBoundingBox = credentialOcrLine.blockBoundingBox,
                    lineBoundingBox = credentialOcrLine.boundingBox,
                    readoutValue = credentialOcrLine.text,
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Simber.e("OCR failed for $documentType", e, tag = MULTI_FACTOR_ID)
            null
        }
    }
}
