package com.simprints.feature.externalcredential.screens.scanocr.usecase

import com.simprints.feature.externalcredential.screens.scanocr.model.OcrScanResult
import com.simprints.feature.externalcredential.screens.scanocr.reader.OcrReader
import javax.inject.Inject

internal class GhanaIdCardOcrReaderUseCase @Inject constructor() {
    operator fun invoke(
        ocrReader: OcrReader,
        isCapturingAllFields: Boolean,
    ): OcrScanResult.GhanaIdCard? {
        val credential = ocrReader.find { matchesPattern(GHANA_ID_PATTERN) } ?: return null

        return if (isCapturingAllFields) {
            // Intentionally using short unique substrings instead of full field names.
            // OCR output is often noisy, and shorter distinctive fragments (e.g. "uance" from "Date of Issuance") reduce matching failures
            OcrScanResult.GhanaIdCard(
                credential = credential,
                surname = ocrReader.findBelow("surname"),
                firstName = ocrReader.findBelow("first"),
                nationality = ocrReader.findBelow("natio"),
                dateOfBirth = ocrReader.findBelow("birth"),
                height = ocrReader.findBelow("height"),
                documentNumber = ocrReader.findBelow("docu"),
                placeOfIssue = ocrReader.findBelow("place"),
                dateOfIssue = ocrReader.findBelow("mission"),
                dateOfExpiry = ocrReader.findBelow("expiry"),
            )
        } else {
            OcrScanResult.GhanaIdCard(credential)
        }
    }

    private fun OcrReader.findBelow(text: String) = find { isBelow { containsText(text) } }

    companion object {
        // Ghana ID card number pattern is "GHA-123456789-0"
        val GHANA_ID_PATTERN = Regex("^GHA-\\d{9}-\\d$")
    }
}
