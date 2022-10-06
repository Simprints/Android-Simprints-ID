package com.simprints.infra.config.local.models

import com.simprints.infra.config.domain.models.FingerprintConfiguration
import com.simprints.infra.config.exceptions.InvalidProtobufEnumException

internal fun FingerprintConfiguration.toProto(): ProtoFingerprintConfiguration =
    ProtoFingerprintConfiguration.newBuilder()
        .addAllFingersToCapture(fingersToCapture.map { it.toProto() })
        .setQualityThreshold(qualityThreshold)
        .setDecisionPolicy(decisionPolicy.toProto())
        .addAllAllowedVeroGenerations(allowedVeroGenerations.map { it.toProto() })
        .setComparisonStrategyForVerification(comparisonStrategyForVerification.toProto())
        .setDisplayHandIcons(displayHandIcons)
        .also { if (vero2 != null) it.vero2 = vero2.toProto() }
        .build()

internal fun FingerprintConfiguration.VeroGeneration.toProto(): ProtoFingerprintConfiguration.VeroGeneration =
    when (this) {
        FingerprintConfiguration.VeroGeneration.VERO_1 -> ProtoFingerprintConfiguration.VeroGeneration.VERO_1
        FingerprintConfiguration.VeroGeneration.VERO_2 -> ProtoFingerprintConfiguration.VeroGeneration.VERO_2
    }

internal fun FingerprintConfiguration.FingerComparisonStrategy.toProto(): ProtoFingerprintConfiguration.FingerComparisonStrategy =
    when (this) {
        FingerprintConfiguration.FingerComparisonStrategy.SAME_FINGER -> ProtoFingerprintConfiguration.FingerComparisonStrategy.SAME_FINGER
        FingerprintConfiguration.FingerComparisonStrategy.CROSS_FINGER_USING_MEAN_OF_MAX -> ProtoFingerprintConfiguration.FingerComparisonStrategy.CROSS_FINGER_USING_MEAN_OF_MAX
    }

internal fun ProtoFingerprintConfiguration.toDomain(): FingerprintConfiguration =
    FingerprintConfiguration(
        fingersToCaptureList.map { it.toDomain() },
        qualityThreshold,
        decisionPolicy.toDomain(),
        allowedVeroGenerationsList.map { it.toDomain() },
        comparisonStrategyForVerification.toDomain(),
        displayHandIcons,
        if (hasVero2()) vero2.toDomain() else null,
    )

internal fun ProtoFingerprintConfiguration.VeroGeneration.toDomain(): FingerprintConfiguration.VeroGeneration =
    when (this) {
        ProtoFingerprintConfiguration.VeroGeneration.VERO_1 -> FingerprintConfiguration.VeroGeneration.VERO_1
        ProtoFingerprintConfiguration.VeroGeneration.VERO_2 -> FingerprintConfiguration.VeroGeneration.VERO_2
        ProtoFingerprintConfiguration.VeroGeneration.UNRECOGNIZED -> throw InvalidProtobufEnumException(
            "invalid VeroGeneration $name"
        )
    }

internal fun ProtoFingerprintConfiguration.FingerComparisonStrategy.toDomain(): FingerprintConfiguration.FingerComparisonStrategy =
    when (this) {
        ProtoFingerprintConfiguration.FingerComparisonStrategy.SAME_FINGER -> FingerprintConfiguration.FingerComparisonStrategy.SAME_FINGER
        ProtoFingerprintConfiguration.FingerComparisonStrategy.CROSS_FINGER_USING_MEAN_OF_MAX -> FingerprintConfiguration.FingerComparisonStrategy.CROSS_FINGER_USING_MEAN_OF_MAX
        ProtoFingerprintConfiguration.FingerComparisonStrategy.UNRECOGNIZED -> throw InvalidProtobufEnumException(
            "invalid FingerComparisonStrategy $name"
        )
    }
