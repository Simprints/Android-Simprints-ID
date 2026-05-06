package com.simprints.feature.externalcredential.screens.scanocr.usecase

import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrDocumentType
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrScanResult
import com.simprints.feature.externalcredential.screens.scanocr.model.ScannedMfidDocument
import com.simprints.feature.externalcredential.screens.scanocr.reader.OcrLine
import com.simprints.feature.externalcredential.screens.search.model.MfidDocument
import javax.inject.Inject

internal class BuildMfidDocumentUseCase @Inject constructor(
    private val getBestReadoutBasedOnConfidenceUseCase: GetBestReadoutBasedOnConfidenceUseCase,
) {
    operator fun invoke(
        scannedDocuments: List<ScannedMfidDocument>,
        documentType: OcrDocumentType,
    ): MfidDocument = when (documentType) {
        OcrDocumentType.NhisCard -> buildNhisCard(scannedDocuments)
        OcrDocumentType.GhanaIdCard -> buildGhanaIdCard(scannedDocuments)
    }

    /**
     * Builds a single NHIS card readout object from multiple OCR scan attempts.
     * Each field is is reconstructed by selecting the most likely readout from all available scan results using OCR confidence aggregation.
     *
     * @param scannedDocuments OCR scan attempts for the same NHIS card
     * @return merged NHIS document with the most likely field values
     */
    private fun buildNhisCard(scannedDocuments: List<ScannedMfidDocument>): MfidDocument.GhanaNhisCard {
        val credential = scannedDocuments
            .map { it.ocrScanResult.credential.text }
            .let { credentials -> getBestReadoutBasedOnConfidenceUseCase(credentials, targetLength = CREDENTIAL_LENGTH_GHANA_NHIS_CARD) }
            .asTokenizableRaw()
        val results = scannedDocuments.map { it.ocrScanResult }.filterIsInstance<OcrScanResult.GhanaNhisCard>()
        return MfidDocument.GhanaNhisCard(
            credential = credential,
            name = results.bestReadoutOrNull { it.name },
            dateOfBirth = results.bestReadoutOrNull { it.dateOfBirth },
            sex = results.bestReadoutOrNull { it.sex },
            dateOfIssue = results.bestReadoutOrNull { it.dateOfIssue },
        )
    }

    /**
     * Builds a single Ghana ID card readout object from multiple OCR scan attempts.
     * Each field is is reconstructed by selecting the most likely readout from all available scan results using OCR confidence aggregation.
     *
     * @param scannedDocuments OCR scan attempts for the same Ghana ID card
     * @return merged Ghana ID document with the most likely field values
     */
    private fun buildGhanaIdCard(scannedDocuments: List<ScannedMfidDocument>): MfidDocument.GhanaIdCard {
        val credential = scannedDocuments
            .map { it.ocrScanResult.credential.text }
            .let { credentials -> getBestReadoutBasedOnConfidenceUseCase(credentials, targetLength = CREDENTIAL_LENGTH_GHANA_ID) }
            .asTokenizableRaw()
        val results = scannedDocuments.map { it.ocrScanResult }.filterIsInstance<OcrScanResult.GhanaIdCard>()
        return MfidDocument.GhanaIdCard(
            credential = credential,
            surname = results.bestReadoutOrNull { it.surname },
            firstName = results.bestReadoutOrNull { it.firstName },
            nationality = results.bestReadoutOrNull { it.nationality },
            dateOfBirth = results.bestReadoutOrNull { it.dateOfBirth },
            height = results.bestReadoutOrNull { it.height },
            documentNumber = results.bestReadoutOrNull { it.documentNumber },
            placeOfIssue = results.bestReadoutOrNull { it.placeOfIssue },
            dateOfIssue = results.bestReadoutOrNull { it.dateOfIssue },
            dateOfExpiry = results.bestReadoutOrNull { it.dateOfExpiry },
        )
    }

    private fun <T> List<T>.bestReadoutOrNull(ocrLine: (T) -> OcrLine?) = mapNotNull(ocrLine)
        .takeUnless(List<OcrLine>::isEmpty)
        ?.let { getBestReadoutBasedOnConfidenceUseCase(it.map(OcrLine::text)) }
        ?.asTokenizableRaw()

    companion object {
        // NHIS membership number contains 8 digits: 12345678
        private const val CREDENTIAL_LENGTH_GHANA_NHIS_CARD = 8

        // Ghana ID field contains 15 chars: GHA-123456789-0
        private const val CREDENTIAL_LENGTH_GHANA_ID = 15
    }
}
