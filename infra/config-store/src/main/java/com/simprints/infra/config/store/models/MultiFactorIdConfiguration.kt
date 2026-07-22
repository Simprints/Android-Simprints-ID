package com.simprints.infra.config.store.models

import com.simprints.core.domain.externalcredential.ExternalCredentialType

data class MultiFactorIdConfiguration(
    val allowedExternalCredentials: List<ExternalCredentialType>,
    val ghanaIdCardConfig: GhanaIdCardConfig?,
    val nhisCardConfig: NhisCardConfig?,
    val qrCodeConfig: QrCodeConfig?,
    val faydaCardConfig: FaydaCardConfig?,
)

data class GhanaIdCardConfig(
    val isCapturingAllFields: Boolean,
)

data class NhisCardConfig(
    val isCapturingAllFields: Boolean,
)

object QrCodeConfig

data class FaydaCardConfig(
    val isCapturingAllFields: Boolean,
)
