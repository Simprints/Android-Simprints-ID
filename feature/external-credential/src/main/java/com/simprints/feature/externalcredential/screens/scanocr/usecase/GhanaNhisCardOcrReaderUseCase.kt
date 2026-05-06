package com.simprints.feature.externalcredential.screens.scanocr.usecase

import com.simprints.feature.externalcredential.screens.scanocr.model.OcrScanResult
import com.simprints.feature.externalcredential.screens.scanocr.reader.OcrReader
import javax.inject.Inject

internal class GhanaNhisCardOcrReaderUseCase @Inject constructor() {
    operator fun invoke(
        ocrReader: OcrReader,
        isCapturingAllFields: Boolean,
    ): OcrScanResult.GhanaNhisCard? {
        val credential = ocrReader.find { matchesPattern(NHIS_PATTERN) } ?: return null

        return if (isCapturingAllFields) {
            // Intentionally using short unique substrings instead of full field names.
            // OCR output is often noisy, and shorter distinctive fragments (e.g. "of issue" from "Date of Issue") reduce matching failures
            OcrScanResult.GhanaNhisCard(
                credential = credential,
                name = ocrReader.findBelow("name"),
                dateOfBirth = ocrReader.findBelow("birth"),
                sex = ocrReader.findBelow("sex"),
                dateOfIssue = ocrReader.findBelow("of issue"),
            )
        } else {
            OcrScanResult.GhanaNhisCard(credential)
        }
    }

    private fun OcrReader.findBelow(text: String) = find { isBelow { containsText(text) } }

    companion object {
        // NHIS Card membership is 8 digits long
        val NHIS_PATTERN = Regex("^\\d{8}$")
    }
}
