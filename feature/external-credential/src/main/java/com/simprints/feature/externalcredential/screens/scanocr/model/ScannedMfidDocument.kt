package com.simprints.feature.externalcredential.screens.scanocr.model

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.Serializable as JavaSerializable

/**
 * Result of the OCR scan for the supported credential document. Its subclasses define the currently-supported document types with their
 * respective non-credential fields.
 *
 * @param imagePath path to full-res bitmap that was used for the OCR
 * @param ocrScanResult detected blocks for the external credential
 */

@Keep
@Serializable
@SerialName("ScannedMfidDocument")
internal data class ScannedMfidDocument(
    val imagePath: String,
    val ocrScanResult: OcrScanResult,
) : JavaSerializable {
    val type: OcrDocumentType
        get() = when (ocrScanResult) {
            is OcrScanResult.GhanaIdCard -> OcrDocumentType.GhanaIdCard
            is OcrScanResult.GhanaNhisCard -> OcrDocumentType.NhisCard
        }
}
