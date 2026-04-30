package com.simprints.feature.externalcredential.screens.scanocr.usecase

import com.simprints.feature.externalcredential.screens.scanocr.reader.OcrLine
import com.simprints.feature.externalcredential.screens.scanocr.reader.OcrReader
import javax.inject.Inject

internal class GhanaNhisCardOcrSelectorUseCase @Inject constructor() {
    operator fun invoke(ocrReader: OcrReader): OcrLine? = ocrReader.find { matchesPattern(NHIS_PATTERN) }

    companion object {
        // NHIS Card membership is 8 digits long
        private val NHIS_PATTERN = Regex("^\\d{8}$")
    }
}
