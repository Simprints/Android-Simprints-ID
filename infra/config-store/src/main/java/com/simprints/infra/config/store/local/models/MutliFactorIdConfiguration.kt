package com.simprints.infra.config.store.local.models

import com.simprints.infra.config.store.models.MultiFactorIdConfiguration

internal fun MultiFactorIdConfiguration.toProto(): ProtoMultiFactorIdConfiguration = ProtoMultiFactorIdConfiguration
    .newBuilder()
    .addAllAllowedExternalCredentials(allowedExternalCredentials.map { it.toProto() })
    .build()

internal fun ProtoMultiFactorIdConfiguration.toDomain(): MultiFactorIdConfiguration = MultiFactorIdConfiguration(
    allowedExternalCredentials = allowedExternalCredentialsList.map { it.toDomain() }
)
