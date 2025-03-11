package com.simprints.infra.config.store.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.config.store.models.SettingsPasswordConfig

@Keep
internal data class ApiGeneralConfiguration(
    val modalities: List<Modality>,
    val matchingModalities: List<Modality>,
    val languageOptions: List<String>,
    val defaultLanguage: String,
    val collectLocation: Boolean,
    val duplicateBiometricEnrolmentCheck: Boolean,
    val settingsPassword: String?,
) {
    fun toDomain(): GeneralConfiguration = GeneralConfiguration(
        modalities.map { it.toDomain() },
        matchingModalities.map { it.toDomain() },
        languageOptions,
        defaultLanguage,
        collectLocation,
        duplicateBiometricEnrolmentCheck,
        SettingsPasswordConfig.toDomain(
            settingsPassword,
        ),
    )

    @Keep
    enum class Modality {
        FACE,
        FINGERPRINT,
        EAR,
        ;

        fun toDomain(): GeneralConfiguration.Modality = when (this) {
            FACE -> GeneralConfiguration.Modality.EAR // TODO for now substituring face with ear
            FINGERPRINT -> GeneralConfiguration.Modality.FINGERPRINT
            EAR -> GeneralConfiguration.Modality.EAR
        }
    }
}
