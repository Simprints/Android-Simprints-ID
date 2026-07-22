package com.simprints.infra.config.store.local.models

import com.simprints.infra.config.store.models.FaydaCardConfig
import com.simprints.infra.config.store.models.GhanaIdCardConfig
import com.simprints.infra.config.store.models.MultiFactorIdConfiguration
import com.simprints.infra.config.store.models.NhisCardConfig
import com.simprints.infra.config.store.models.QrCodeConfig

internal fun MultiFactorIdConfiguration.toProto(): ProtoMultiFactorIdConfiguration = ProtoMultiFactorIdConfiguration
    .newBuilder()
    .addAllAllowedExternalCredentials(allowedExternalCredentials.map { it.toProto() })
    .also { if (ghanaIdCardConfig != null) it.setGhanaIdCardConfig(ghanaIdCardConfig.toProto()) }
    .also { if (nhisCardConfig != null) it.setNhisCardConfig(nhisCardConfig.toProto()) }
    .also { if (qrCodeConfig != null) it.setQrCodeConfig(qrCodeConfig.toProto()) }
    .also { if (faydaCardConfig != null) it.setFaydaCardConfig(faydaCardConfig.toProto()) }
    .build()

internal fun GhanaIdCardConfig.toProto(): ProtoGhanaIdCardConfig = ProtoGhanaIdCardConfig
    .newBuilder()
    .setIsCapturingAllFields(isCapturingAllFields)
    .build()

internal fun NhisCardConfig.toProto(): ProtoNhisCardConfig = ProtoNhisCardConfig
    .newBuilder()
    .setIsCapturingAllFields(isCapturingAllFields)
    .build()

internal fun QrCodeConfig.toProto(): ProtoQrCodeConfig = ProtoQrCodeConfig
    .newBuilder()
    .build()

internal fun FaydaCardConfig.toProto(): ProtoFaydaCardConfig = ProtoFaydaCardConfig
    .newBuilder()
    .setIsCapturingAllFields(isCapturingAllFields)
    .build()

internal fun ProtoMultiFactorIdConfiguration.toDomain(): MultiFactorIdConfiguration = MultiFactorIdConfiguration(
    allowedExternalCredentials = allowedExternalCredentialsList.map { it.toDomain() },
    ghanaIdCardConfig = if (hasGhanaIdCardConfig()) ghanaIdCardConfig.toDomain() else null,
    nhisCardConfig = if (hasNhisCardConfig()) nhisCardConfig.toDomain() else null,
    qrCodeConfig = if (hasQrCodeConfig()) QrCodeConfig else null,
    faydaCardConfig = if (hasFaydaCardConfig()) faydaCardConfig.toDomain() else null,
)

internal fun ProtoGhanaIdCardConfig.toDomain() = GhanaIdCardConfig(
    isCapturingAllFields = isCapturingAllFields,
)

internal fun ProtoNhisCardConfig.toDomain() = NhisCardConfig(
    isCapturingAllFields = isCapturingAllFields,
)

internal fun ProtoFaydaCardConfig.toDomain() = FaydaCardConfig(
    isCapturingAllFields = isCapturingAllFields,
)
