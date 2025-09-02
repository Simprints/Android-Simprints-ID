package com.simprints.infra.config.store.remote.models

import androidx.annotation.Keep
import com.simprints.core.domain.common.Modality
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.config.store.models.SettingsPasswordConfig

@Keep
internal data class ApiGeneralConfiguration(
    val modalities: List<ApiModality>,
    val matchingModalities: List<ApiModality>,
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
    enum class ApiModality {
        FACE,
        FINGERPRINT,
        ;

        fun toDomain(): Modality = when (this) {
            FACE -> Modality.FACE
            FINGERPRINT -> Modality.FINGERPRINT
        }
    }
}
