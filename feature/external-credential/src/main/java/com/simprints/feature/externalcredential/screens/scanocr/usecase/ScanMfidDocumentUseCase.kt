package com.simprints.feature.externalcredential.screens.scanocr.usecase

import android.graphics.Bitmap
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrDocumentType
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrScanResult
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
    private val readBarcodesFromImage: ReadBarcodesFromImageUseCase,
    private val ghanaNhisCardOcrReaderUseCase: GhanaNhisCardOcrReaderUseCase,
    private val ghanaIdCardOcrReaderUseCase: GhanaIdCardOcrReaderUseCase,
    private val faydaCardOcrReaderUseCase: FaydaCardOcrReaderUseCase,
    private val credentialImageRepository: CredentialImageRepository,
    private val createAndSaveZoomedImageUseCase: CreateAndSaveZoomedImageUseCase,
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
            if (readFaydaBarcode && documentType == OcrDocumentType.FaydaCard) {
                // Fayda cards carry the FAN in a barcode, which reads faster and more reliably than the printed number
                faydaCardOcrReaderUseCase.fromBarcodes(readBarcodesFromImage(bitmap))?.let { scanResult ->
                    return save(bitmap, scanResult, isBarcode = true)
                }
            }
            val ocrText = readTextFromImage(bitmap) ?: return null
            val ocrReader = OcrReader(ocrText)
            val scanResult = when (documentType) {
                OcrDocumentType.NhisCard -> ghanaNhisCardOcrReaderUseCase(ocrReader, isCapturingAllFields)
                OcrDocumentType.GhanaIdCard -> ghanaIdCardOcrReaderUseCase(ocrReader, isCapturingAllFields)
                OcrDocumentType.FaydaCard -> faydaCardOcrReaderUseCase(ocrReader)
            }
            scanResult?.let { save(bitmap, it, isBarcode = false) }
        } catch (e: Exception) {
            Simber.e("OCR failed for $documentType", e, tag = MULTI_FACTOR_ID)
            null
        }
    }

    private suspend fun save(
        bitmap: Bitmap,
        scanResult: OcrScanResult,
        isBarcode: Boolean,
    ): ScannedMfidDocument {
        val zoomedImagePath = createAndSaveZoomedImageUseCase(scanResult.credential, bitmap, isBarcode)
        val imagePath = credentialImageRepository.saveCredentialScan(bitmap, imageType = FullDocument)
        return ScannedMfidDocument(imagePath, zoomedImagePath, scanResult)
    }
}
