package com.simprints.feature.externalcredential.screens.scanocr.usecase

import android.graphics.Bitmap
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrDocumentType
import com.simprints.feature.externalcredential.screens.scanocr.model.ScannedMfidDocument
import com.simprints.feature.externalcredential.screens.scanocr.reader.OcrReader
import com.simprints.infra.config.store.models.MultiFactorIdConfiguration
import com.simprints.infra.credential.store.CredentialImageRepository
import com.simprints.infra.credential.store.model.CredentialScanImageType.FullDocument
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.MULTI_FACTOR_ID
import com.simprints.infra.logging.Simber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ScanMfidDocumentUseCase @Inject constructor(
    private val readTextFromImage: ReadTextFromImageUseCase,
    private val ghanaNhisCardOcrReaderUseCase: GhanaNhisCardOcrReaderUseCase,
    private val ghanaIdCardOcrReaderUseCase: GhanaIdCardOcrReaderUseCase,
    private val credentialImageRepository: CredentialImageRepository,
) {
    suspend operator fun invoke(
        bitmap: Bitmap,
        documentType: OcrDocumentType,
        config: MultiFactorIdConfiguration,
    ): ScannedMfidDocument? {
        val isCapturingAllFields = when (documentType) {
            OcrDocumentType.NhisCard -> config.nhisCardConfig?.isCapturingAllFields
            OcrDocumentType.GhanaIdCard -> config.ghanaIdCardConfig?.isCapturingAllFields
        } ?: false
        return try {
            val ocrText = readTextFromImage(bitmap) ?: return null
            val ocrReader = OcrReader(ocrText)
            val scanResult = when (documentType) {
                OcrDocumentType.NhisCard -> ghanaNhisCardOcrReaderUseCase(ocrReader, isCapturingAllFields)
                OcrDocumentType.GhanaIdCard -> ghanaIdCardOcrReaderUseCase(ocrReader, isCapturingAllFields)
            }
            if (scanResult != null) {
                val savedImagePath = credentialImageRepository.saveCredentialScan(bitmap, imageType = FullDocument)
                ScannedMfidDocument(savedImagePath, scanResult)
            } else {
                null
            }
        } catch (e: Exception) {
            Simber.e("OCR failed for $documentType", e, tag = MULTI_FACTOR_ID)
            null
        }
    }
}
