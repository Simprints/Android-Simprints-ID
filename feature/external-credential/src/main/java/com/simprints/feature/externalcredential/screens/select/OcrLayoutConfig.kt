package com.simprints.feature.externalcredential.screens.select

import com.simprints.feature.externalcredential.model.ExternalCredentialResult

data class OcrLayoutConfig(
    val userMessages: MutableMap<ExternalCredentialResult, String>,
)
