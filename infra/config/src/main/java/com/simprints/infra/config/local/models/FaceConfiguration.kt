package com.simprints.infra.config.local.models

import com.simprints.infra.config.domain.models.FaceConfiguration
import com.simprints.infra.config.exceptions.InvalidProtobufEnumException

internal fun FaceConfiguration.toProto(): ProtoFaceConfiguration =
    ProtoFaceConfiguration.newBuilder()
        .setNbOfImagesToCapture(nbOfImagesToCapture)
        .setQualityThreshold(qualityThreshold)
        .setImageSavingStrategy(imageSavingStrategy.toProto())
        .setDecisionPolicy(decisionPolicy.toProto())
        .build()

internal fun FaceConfiguration.ImageSavingStrategy.toProto(): ProtoFaceConfiguration.ImageSavingStrategy =
    when (this) {
        FaceConfiguration.ImageSavingStrategy.NEVER -> ProtoFaceConfiguration.ImageSavingStrategy.NEVER
        FaceConfiguration.ImageSavingStrategy.ONLY_GOOD_SCAN -> ProtoFaceConfiguration.ImageSavingStrategy.ONLY_GOOD_SCAN
    }

internal fun ProtoFaceConfiguration.toDomain(): FaceConfiguration =
    FaceConfiguration(
        nbOfImagesToCapture,
        qualityThreshold,
        imageSavingStrategy.toDomain(),
        decisionPolicy.toDomain(),
    )

internal fun ProtoFaceConfiguration.ImageSavingStrategy.toDomain(): FaceConfiguration.ImageSavingStrategy =
    when (this) {
        ProtoFaceConfiguration.ImageSavingStrategy.NEVER -> FaceConfiguration.ImageSavingStrategy.NEVER
        ProtoFaceConfiguration.ImageSavingStrategy.ONLY_GOOD_SCAN -> FaceConfiguration.ImageSavingStrategy.ONLY_GOOD_SCAN
        ProtoFaceConfiguration.ImageSavingStrategy.UNRECOGNIZED -> throw InvalidProtobufEnumException(
            "invalid image saving strategy $name"
        )
    }
