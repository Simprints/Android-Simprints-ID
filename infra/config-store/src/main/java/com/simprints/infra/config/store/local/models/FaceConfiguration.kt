package com.simprints.infra.config.store.local.models

import com.simprints.infra.config.store.exceptions.InvalidProtobufEnumException
import com.simprints.infra.config.store.models.AgeGroup
import com.simprints.infra.config.store.models.FaceConfiguration

internal fun FaceConfiguration.toProto(): ProtoFaceConfiguration = ProtoFaceConfiguration
    .newBuilder()
    .addAllAllowedSdks(allowedSDKs.map { it.toProto() })
    .also { if (rankOne != null) it.rankOne = rankOne.toProto() }
    .also { if (simFace != null) it.simFace = simFace.toProto() }
    .build()

internal fun FaceConfiguration.BioSdk.toProto() = when (this) {
    FaceConfiguration.BioSdk.RANK_ONE -> ProtoFaceConfiguration.ProtoBioSdk.RANK_ONE
    FaceConfiguration.BioSdk.SIM_FACE -> ProtoFaceConfiguration.ProtoBioSdk.SIM_FACE
}

internal fun FaceConfiguration.FaceSdkConfiguration.toProto() = ProtoFaceConfiguration.ProtoFaceSdkConfiguration
    .newBuilder()
    .setNbOfImagesToCapture(nbOfImagesToCapture)
    .setQualityThresholdPrecise(qualityThreshold)
    .setImageSavingStrategy(imageSavingStrategy.toProto())
    .setDecisionPolicy(decisionPolicy.toProto())
    .setVersion(version)
    .setAllowedAgeRange(allowedAgeRange.toProto())
    .also {
        if (verificationMatchThreshold != null) it.verificationMatchThreshold = verificationMatchThreshold
    }.build()

internal fun FaceConfiguration.ImageSavingStrategy.toProto(): ProtoFaceConfiguration.ImageSavingStrategy = when (this) {
    FaceConfiguration.ImageSavingStrategy.NEVER -> ProtoFaceConfiguration.ImageSavingStrategy.NEVER
    FaceConfiguration.ImageSavingStrategy.ONLY_USED_IN_REFERENCE -> ProtoFaceConfiguration.ImageSavingStrategy.ONLY_USED_IN_REFERENCE
    FaceConfiguration.ImageSavingStrategy.ONLY_GOOD_SCAN -> ProtoFaceConfiguration.ImageSavingStrategy.ONLY_GOOD_SCAN
}

internal fun ProtoFaceConfiguration.toDomain(): FaceConfiguration = FaceConfiguration(
    allowedSDKs = allowedSdksList.map { it.toDomain() },
    rankOne = if (hasRankOne()) rankOne.toDomain() else null,
    simFace = if (hasSimFace()) simFace.toDomain() else null,
)

@Suppress("SameReturnValue")
internal fun ProtoFaceConfiguration.ProtoBioSdk.toDomain() = when (this) {
    ProtoFaceConfiguration.ProtoBioSdk.RANK_ONE -> FaceConfiguration.BioSdk.RANK_ONE
    ProtoFaceConfiguration.ProtoBioSdk.SIM_FACE -> FaceConfiguration.BioSdk.SIM_FACE
    ProtoFaceConfiguration.ProtoBioSdk.UNRECOGNIZED -> FaceConfiguration.BioSdk.RANK_ONE
}

internal fun ProtoFaceConfiguration.ProtoFaceSdkConfiguration.toDomain() = FaceConfiguration.FaceSdkConfiguration(
    nbOfImagesToCapture = nbOfImagesToCapture,
    qualityThreshold = qualityThresholdPrecise,
    imageSavingStrategy = imageSavingStrategy.toDomain(),
    decisionPolicy = decisionPolicy.toDomain(),
    version = version,
    allowedAgeRange = if (hasAllowedAgeRange()) allowedAgeRange.toDomain() else AgeGroup(0, null),
    verificationMatchThreshold = if (hasVerificationMatchThreshold()) verificationMatchThreshold else null,
)

internal fun ProtoFaceConfiguration.ImageSavingStrategy.toDomain(): FaceConfiguration.ImageSavingStrategy = when (this) {
    ProtoFaceConfiguration.ImageSavingStrategy.NEVER -> FaceConfiguration.ImageSavingStrategy.NEVER
    ProtoFaceConfiguration.ImageSavingStrategy.ONLY_USED_IN_REFERENCE -> FaceConfiguration.ImageSavingStrategy.ONLY_USED_IN_REFERENCE
    ProtoFaceConfiguration.ImageSavingStrategy.ONLY_GOOD_SCAN -> FaceConfiguration.ImageSavingStrategy.ONLY_GOOD_SCAN
    ProtoFaceConfiguration.ImageSavingStrategy.UNRECOGNIZED -> throw InvalidProtobufEnumException(
        "invalid image saving strategy $name",
    )
}
