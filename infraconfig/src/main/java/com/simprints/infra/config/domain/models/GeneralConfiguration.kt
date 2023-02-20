package com.simprints.infra.config.domain.models

data class GeneralConfiguration(
    val modalities: List<Modality>,
    val languageOptions: List<String>,
    val defaultLanguage: String,
    val collectLocation: Boolean,
    val duplicateBiometricEnrolmentCheck: Boolean,
    val settingsPassword: SettingsPasswordConfig,
) {

    enum class Modality {
        FACE,
        FINGERPRINT;
    }
}
