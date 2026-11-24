package com.simprints.feature.externalcredential.screens.scanocr.model

/**
 * Tracks the documents read during one scanning session and decides when the session has enough of them.
 *
 * Different credential types can be read in different ways. For example, a FAN value on the Fayda card can come from the OCR
 * or from the barcode, and each way needs a different number of successful reads.
 *
 * This class keeps those per-type rules in one place so the caller only has to add readouts and ask whether the scan is complete.
 */
internal class MfidCaptureState(
    private val documentType: OcrDocumentType,
    private val config: OcrConfig,
) {
    var documents: List<ScannedMfidDocument> = emptyList()
        private set

    private val ocrScans get() = documents.count { it.source == ReadoutSource.TEXT_OCR }
    private val barcodeScans get() = documents.count { it.source == ReadoutSource.BARCODE }

    val isComplete: Boolean
        get() = when (documentType) {
            OcrDocumentType.FaydaCard ->
                (config.readFaydaBarcode && barcodeScans >= BARCODE_SCANS_REQUIRED) || ocrScans >= config.capturesRequired
            OcrDocumentType.NhisCard,
            OcrDocumentType.GhanaIdCard,
                -> ocrScans >= config.capturesRequired
        }

    /**
     * Longest wait a user might face before completion; drives the progress indicator.
     */
    val maxScansRequired: Int
        get() = config.capturesRequired

    fun add(document: ScannedMfidDocument) {
        if (!isComplete) documents += document
    }

    companion object {
        // Barcode standard has its own check, no need for error correction - 1 scan is enough
        private const val BARCODE_SCANS_REQUIRED = 1
    }
}
