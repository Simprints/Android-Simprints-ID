package com.simprints.infra.config.store.remote.models

import androidx.annotation.Keep
import com.simprints.core.domain.common.Modality
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.config.store.models.SettingsPasswordConfig
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class ApiGeneralConfiguration(
    val modalities: List<String>,
    val matchingModalities: List<String>,
    val languageOptions: List<String>,
    val defaultLanguage: String,
    val collectLocation: Boolean,
    val duplicateBiometricEnrolmentCheck: Boolean,
    val settingsPassword: String? = null,
) {
    fun toDomain(): GeneralConfiguration = GeneralConfiguration(
        modalities.mapNotNull { ApiModality.fromString(it) },
        matchingModalities.mapNotNull { ApiModality.fromString(it) },
        languageOptions,
        defaultLanguage,
        collectLocation,
        duplicateBiometricEnrolmentCheck,
        SettingsPasswordConfig.toDomain(
            settingsPassword,
        ),
    )

    @Keep
    object ApiModality {
        fun fromString(value: String?): Modality? = when (value?.uppercase()) {
            "FACE" -> Modality.FACE
            "FINGERPRINT" -> Modality.FINGERPRINT
            else -> null
        }
    }
}
