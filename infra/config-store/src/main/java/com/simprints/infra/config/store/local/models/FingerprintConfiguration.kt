package com.simprints.infra.config.store.local.models

import com.simprints.infra.config.store.exceptions.InvalidProtobufEnumException
import com.simprints.infra.config.store.local.models.ProtoFingerprintConfiguration.ProtoMaxCaptureAttempts
import com.simprints.infra.config.store.models.AgeGroup
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.config.store.models.MaxCaptureAttempts

internal fun FingerprintConfiguration.toProto(): ProtoFingerprintConfiguration = ProtoFingerprintConfiguration
    .newBuilder()
    .addAllAllowedScanners(allowedScanners.map { it.toProto() })
    .addAllAllowedSdks(allowedSDKs.map { it.toProto() })
    .setDisplayHandIcons(displayHandIcons)
    .also {
        if (secugenSimMatcher != null) it.secugenSimMatcher = secugenSimMatcher.toProto()
        if (nec != null) it.nec = nec.toProto()
    }.build()

internal fun FingerprintConfiguration.FingerprintSdkConfiguration.toProto() = ProtoFingerprintConfiguration.ProtoFingerprintSdkConfiguration
    .newBuilder()
    .addAllFingersToCapture(fingersToCapture.map { it.toProto() })
    .setDecisionPolicy(decisionPolicy.toProto())
    .setComparisonStrategyForVerification(comparisonStrategyForVerification.toProto())
    .setAllowedAgeRange(allowedAgeRange.toProto())
    .also {
        if (vero1 != null) it.vero1 = vero1.toProto()
        if (vero2 != null) it.vero2 = vero2.toProto()
        if (verificationMatchThreshold != null) it.verificationMatchThreshold = verificationMatchThreshold
        if (maxCaptureAttempts != null) it.maxCaptureAttempts = maxCaptureAttempts.toProto()
    }.build()

internal fun FingerprintConfiguration.VeroGeneration.toProto() = when (this) {
    FingerprintConfiguration.VeroGeneration.VERO_1 -> ProtoFingerprintConfiguration.VeroGeneration.VERO_1
    FingerprintConfiguration.VeroGeneration.VERO_2 -> ProtoFingerprintConfiguration.VeroGeneration.VERO_2
}

internal fun FingerprintConfiguration.BioSdk.toProto() = when (this) {
    FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER -> ProtoFingerprintConfiguration.ProtoBioSdk.SECUGEN_SIM_MATCHER
    FingerprintConfiguration.BioSdk.NEC -> ProtoFingerprintConfiguration.ProtoBioSdk.NEC
}

internal fun FingerprintConfiguration.FingerComparisonStrategy.toProto() = when (this) {
    FingerprintConfiguration.FingerComparisonStrategy.SAME_FINGER -> ProtoFingerprintConfiguration.FingerComparisonStrategy.SAME_FINGER
    FingerprintConfiguration.FingerComparisonStrategy.CROSS_FINGER_USING_MEAN_OF_MAX -> ProtoFingerprintConfiguration.FingerComparisonStrategy.CROSS_FINGER_USING_MEAN_OF_MAX
}

internal fun MaxCaptureAttempts.toProto() = ProtoMaxCaptureAttempts.newBuilder().setNoFingerDetected(noFingerDetected).build()

internal fun ProtoFingerprintConfiguration.toDomain() = // if has nec or sim matcher then it's a new config
    if (hasNec() || hasSecugenSimMatcher()) {
        toDomainNew()
    } else {
        toDomainOld()
    }

internal fun ProtoFingerprintConfiguration.toDomainOld() = FingerprintConfiguration(
    allowedScanners = allowedVeroGenerationsList.map { it.toDomain() },
    allowedSDKs = listOf(FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER),
    secugenSimMatcher = FingerprintConfiguration.FingerprintSdkConfiguration(
        fingersToCapture = fingersToCaptureList.map { it.toDomain() },
        decisionPolicy = decisionPolicy.toDomain(),
        comparisonStrategyForVerification = comparisonStrategyForVerification.toDomain(),
        vero1 = vero1.toDomain(),
        vero2 = vero2.toDomain(),
        maxCaptureAttempts = null,
        version = "",
    ),
    nec = null,
    displayHandIcons = displayHandIcons,
)

internal fun ProtoFingerprintConfiguration.toDomainNew() = FingerprintConfiguration(
    allowedScanners = allowedScannersList.map { it.toDomain() },
    allowedSDKs = allowedSdksList.map { it.toDomain() },
    displayHandIcons,
    if (hasSecugenSimMatcher()) secugenSimMatcher.toDomain() else null,
    if (hasNec()) nec.toDomain() else null,
)

internal fun ProtoFingerprintConfiguration.ProtoBioSdk.toDomain() = when (this) {
    ProtoFingerprintConfiguration.ProtoBioSdk.SECUGEN_SIM_MATCHER -> FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER
    ProtoFingerprintConfiguration.ProtoBioSdk.NEC -> FingerprintConfiguration.BioSdk.NEC
    ProtoFingerprintConfiguration.ProtoBioSdk.UNRECOGNIZED -> FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER
}

internal fun ProtoFingerprintConfiguration.ProtoFingerprintSdkConfiguration.toDomain() =
    FingerprintConfiguration.FingerprintSdkConfiguration(
        fingersToCapture = fingersToCaptureList.map { it.toDomain() },
        decisionPolicy = decisionPolicy.toDomain(),
        comparisonStrategyForVerification = comparisonStrategyForVerification.toDomain(),
        vero1 = if (hasVero1()) vero1.toDomain() else null,
        vero2 = if (hasVero2()) vero2.toDomain() else null,
        allowedAgeRange = if (hasAllowedAgeRange()) allowedAgeRange.toDomain() else AgeGroup(0, null),
        verificationMatchThreshold = if (hasVerificationMatchThreshold()) verificationMatchThreshold else null,
        maxCaptureAttempts = maxCaptureAttempts.toDomain(),
        version = version,
    )

internal fun ProtoMaxCaptureAttempts.toDomain() = MaxCaptureAttempts(
    noFingerDetected = noFingerDetected,
)

internal fun ProtoFingerprintConfiguration.VeroGeneration.toDomain() = when (this) {
    ProtoFingerprintConfiguration.VeroGeneration.VERO_1 -> FingerprintConfiguration.VeroGeneration.VERO_1
    ProtoFingerprintConfiguration.VeroGeneration.VERO_2 -> FingerprintConfiguration.VeroGeneration.VERO_2
    ProtoFingerprintConfiguration.VeroGeneration.UNRECOGNIZED -> throw InvalidProtobufEnumException(
        "invalid VeroGeneration $name",
    )
}

internal fun ProtoFingerprintConfiguration.FingerComparisonStrategy.toDomain() = when (this) {
    ProtoFingerprintConfiguration.FingerComparisonStrategy.SAME_FINGER -> FingerprintConfiguration.FingerComparisonStrategy.SAME_FINGER
    ProtoFingerprintConfiguration.FingerComparisonStrategy.CROSS_FINGER_USING_MEAN_OF_MAX -> FingerprintConfiguration.FingerComparisonStrategy.CROSS_FINGER_USING_MEAN_OF_MAX
    ProtoFingerprintConfiguration.FingerComparisonStrategy.UNRECOGNIZED -> throw InvalidProtobufEnumException(
        "invalid FingerComparisonStrategy $name",
    )
}

internal fun ProtoAllowedAgeRange.toDomain() = AgeGroup(startInclusive, if (hasEndExclusive()) endExclusive else null)

internal fun AgeGroup.toProto() = ProtoAllowedAgeRange
    .newBuilder()
    .also {
        it.setStartInclusive(startInclusive)
        if (endExclusive != null) it.setEndExclusive(endExclusive)
    }.build()
