package com.simprints.feature.externalcredential.screens.scanocr.usecase

import android.graphics.Bitmap
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrDocumentType
import com.simprints.feature.externalcredential.screens.scanocr.model.ScannedMfidDocument
import com.simprints.infra.config.store.models.MultiFactorIdConfiguration
import com.simprints.infra.credential.store.CredentialImageRepository
import com.simprints.infra.credential.store.model.CredentialScanImageType.FullDocument
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.MULTI_FACTOR_ID
import com.simprints.infra.logging.Simber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ScanMfidDocumentUseCase @Inject constructor(
    private val readBarcode: ReadBarcodeUseCase,
    private val readOcrText: ReadOcrTextUseCase,
    private val credentialImageRepository: CredentialImageRepository,
) {
    suspend operator fun invoke(
        bitmap: Bitmap,
        documentType: OcrDocumentType,
        config: MultiFactorIdConfiguration,
        readFaydaBarcode: Boolean = false,
    ): ScannedMfidDocument? {
        val isCapturingAllFields = when (documentType) {
            OcrDocumentType.NhisCard -> config.nhisCardConfig?.isCapturingAllFields
            OcrDocumentType.GhanaIdCard -> config.ghanaIdCardConfig?.isCapturingAllFields
            OcrDocumentType.FaydaCard -> config.faydaCardConfig?.isCapturingAllFields
        } ?: false
        return try {
            val readout = when {
                // A barcode reads faster and more reliably than the printed number, so it is tried first
                readFaydaBarcode -> readBarcode(bitmap, documentType) ?: readOcrText(bitmap, documentType, isCapturingAllFields)

                else -> readOcrText(bitmap, documentType, isCapturingAllFields)
            }
            readout?.let {
                val savedImagePath = credentialImageRepository.saveCredentialScan(bitmap, imageType = FullDocument)
                ScannedMfidDocument(savedImagePath, it.scanResult, it.source)
            }
        } catch (e: Exception) {
            Simber.e("OCR failed for $documentType", e, tag = MULTI_FACTOR_ID)
            null
        }
    }
}
