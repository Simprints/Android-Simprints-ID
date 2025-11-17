package com.simprints.feature.externalcredential.screens.scanocr.usecase

import javax.inject.Inject

internal class FaydaCardOcrSelectorUseCase @Inject constructor() {
    operator fun invoke(readoutValue: String): Boolean = FAYDA_ID_PATTERN.matches(readoutValue)

    companion object {
        // Ethiopian Fayda ID card number pattern is "FCN" followed by 16 digits
        private val FAYDA_ID_PATTERN = Regex("^FCN\\d{16}$")
    }
}
