package com.simprints.feature.externalcredential.screens.scanocr.usecase

import com.simprints.feature.externalcredential.screens.scanocr.reader.OcrLine
import com.simprints.feature.externalcredential.screens.scanocr.reader.OcrReader
import javax.inject.Inject

internal class GhanaIdCardOcrSelectorUseCase @Inject constructor() {
    operator fun invoke(ocrReader: OcrReader): OcrLine? = ocrReader.find { matchesPattern(GHANA_ID_PATTERN) }

    companion object {
        // Ghana ID card number pattern is "GHA-12345789-0"
        private val GHANA_ID_PATTERN = Regex("^GHA-\\d{9}-\\d$")
    }
}
