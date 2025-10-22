package com.simprints.infra.config.store.models

import com.simprints.core.domain.externalcredential.ExternalCredentialType

data class MultiFactorIdConfiguration(
    val allowedExternalCredentials: List<ExternalCredentialType>
)
