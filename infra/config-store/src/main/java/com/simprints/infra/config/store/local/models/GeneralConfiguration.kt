package com.simprints.infra.config.store.local.models

import com.simprints.core.domain.modality.Modality
import com.simprints.infra.config.store.exceptions.InvalidProtobufEnumException
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.config.store.models.SettingsPasswordConfig

internal fun GeneralConfiguration.toProto(): ProtoGeneralConfiguration = ProtoGeneralConfiguration
    .newBuilder()
    .addAllModalities(modalities.map { it.toProto() })
    .addAllMatchingModalities(matchingModalities.map { it.toProto() })
    .addAllLanguageOptions(languageOptions)
    .setDefaultLanguage(defaultLanguage)
    .setCollectLocation(collectLocation)
    .setDuplicateBiometricEnrolmentCheck(duplicateBiometricEnrolmentCheck)
    .setSettingsPassword(settingsPassword.toProto())
    .build()

internal fun Modality.toProto(): ProtoGeneralConfiguration.Modality = when (this) {
    Modality.FACE -> ProtoGeneralConfiguration.Modality.FACE
    Modality.FINGERPRINT -> ProtoGeneralConfiguration.Modality.FINGERPRINT
}

internal fun ProtoGeneralConfiguration.toDomain(): GeneralConfiguration = GeneralConfiguration(
    modalitiesList.map { it.toDomain() },
    matchingModalitiesList.map { it.toDomain() },
    languageOptionsList,
    defaultLanguage,
    collectLocation,
    duplicateBiometricEnrolmentCheck,
    SettingsPasswordConfig.toDomain(settingsPassword),
)

internal fun ProtoGeneralConfiguration.Modality.toDomain(): Modality = when (this) {
    ProtoGeneralConfiguration.Modality.FACE -> Modality.FACE
    ProtoGeneralConfiguration.Modality.FINGERPRINT -> Modality.FINGERPRINT
    ProtoGeneralConfiguration.Modality.UNRECOGNIZED -> throw InvalidProtobufEnumException("invalid modality $name")
}

internal fun SettingsPasswordConfig.toProto(): String = when (this) {
    SettingsPasswordConfig.NotSet -> ""
    is SettingsPasswordConfig.Locked -> password
    SettingsPasswordConfig.Unlocked -> throw IllegalStateException("Cannot persist Unlocked state")
}
