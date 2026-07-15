package com.simprints.feature.externalcredential.screens.scanocr.usecase

import com.simprints.feature.externalcredential.screens.scanocr.model.OcrScanResult
import com.simprints.feature.externalcredential.screens.scanocr.reader.OcrReader
import javax.inject.Inject

internal class FaydaCardOcrReaderUseCase @Inject constructor() {
    operator fun invoke(ocrReader: OcrReader): OcrScanResult.FaydaCard? {
        val credential = ocrReader.find {
            matchesCondition { line ->
                // Removing non-digits from the line readout. Checking if remaining digits match FAN_PATTERN
                line.filter(Char::isDigit).matches(FAN_PATTERN)
            }
        } ?: return null
        val credentialText = credential.text.filter(Char::isDigit)
        return OcrScanResult.FaydaCard(credential.copy(text = credentialText))
    }

    companion object {
        // Fayda Alias Number (FAN): exactly 16 digits after stripping all non-digit characters
        val FAN_PATTERN = Regex("^\\d{16}$")
    }
}
