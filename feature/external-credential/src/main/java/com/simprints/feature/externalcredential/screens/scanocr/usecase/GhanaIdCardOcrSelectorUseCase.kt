package com.simprints.feature.externalcredential.screens.scanocr.usecase

import javax.inject.Inject

internal class GhanaIdCardOcrSelectorUseCase @Inject constructor() {
    operator fun invoke(readoutValue: String): Boolean = GHANA_ID_PATTERN.matches(readoutValue)

    companion object {
        // Ghana ID card number pattern is "GHA-12345789-0"
        private val GHANA_ID_PATTERN = Regex("^GHA-\\d{9}-\\d$")
    }
}
