package com.simprints.feature.externalcredential.screens.scanocr

import com.simprints.feature.externalcredential.screens.scanocr.model.OcrDocumentType


internal sealed class ScanOcrState {
    abstract val ocrDocumentType: OcrDocumentType

    data class ReadyToScan(
        override val ocrDocumentType: OcrDocumentType,
    ) : ScanOcrState()

    data class NoCameraPermission(
        override val ocrDocumentType: OcrDocumentType,
        val shouldOpenPhoneSettings: Boolean
    ) : ScanOcrState()

    data class InProgress(
        override val ocrDocumentType: OcrDocumentType,
        val successfulCaptures: Int
    ) : ScanOcrState()
}
