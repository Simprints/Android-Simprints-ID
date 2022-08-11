package com.simprints.infra.config.local.models

import com.simprints.infra.config.domain.models.GeneralConfiguration
import com.simprints.infra.config.exceptions.InvalidProtobufEnumException

internal fun GeneralConfiguration.toProto(): ProtoGeneralConfiguration =
    ProtoGeneralConfiguration.newBuilder()
        .addAllModalities(modalities.map { it.toProto() })
        .addAllLanguageOptions(languageOptions)
        .setDefaultLanguage(defaultLanguage)
        .setCollectLocation(collectLocation)
        .setDuplicateBiometricEnrolmentCheck(duplicateBiometricEnrolmentCheck)
        .build()

internal fun GeneralConfiguration.Modality.toProto(): ProtoGeneralConfiguration.Modality =
    when (this) {
        GeneralConfiguration.Modality.FACE -> ProtoGeneralConfiguration.Modality.FACE
        GeneralConfiguration.Modality.FINGERPRINT -> ProtoGeneralConfiguration.Modality.FINGERPRINT
    }


internal fun ProtoGeneralConfiguration.toDomain(): GeneralConfiguration =
    GeneralConfiguration(
        modalitiesList.map { it.toDomain() },
        languageOptionsList,
        defaultLanguage,
        collectLocation,
        duplicateBiometricEnrolmentCheck
    )


internal fun ProtoGeneralConfiguration.Modality.toDomain(): GeneralConfiguration.Modality =
    when (this) {
        ProtoGeneralConfiguration.Modality.FACE -> GeneralConfiguration.Modality.FACE
        ProtoGeneralConfiguration.Modality.FINGERPRINT -> GeneralConfiguration.Modality.FINGERPRINT
        ProtoGeneralConfiguration.Modality.UNRECOGNIZED -> throw InvalidProtobufEnumException("invalid modality $name")
    }
