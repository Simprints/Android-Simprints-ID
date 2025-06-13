package com.simprints.feature.externalcredential.screens.select

import com.simprints.feature.externalcredential.model.ExternalCredentialResult

data class ExternalCredentialPreviewLayoutConfig(
    val userMessages: MutableMap<ExternalCredentialResult, String>,
)
