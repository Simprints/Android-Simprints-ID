package com.simprints.feature.externalcredential.screens.scanocr.usecase

import javax.inject.Inject

internal class GhanaNhisCardOcrSelectorUseCase @Inject constructor() {

    operator fun invoke(readoutValue: String): Boolean =
        NHIS_PATTERN.matches(readoutValue)

    companion object {
        // NHIS Card membership is 8 digits long
        private val NHIS_PATTERN = Regex("^\\d{8}$")
    }

}
