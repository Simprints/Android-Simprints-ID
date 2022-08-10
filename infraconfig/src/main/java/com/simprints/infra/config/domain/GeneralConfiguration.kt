package com.simprints.infra.config.domain

data class GeneralConfiguration(
    val modalities: List<Modality>,
    val languageOptions: List<String>,
    val defaultLanguage: String,
    val collectLocation: Boolean,
    val duplicateBiometricEnrolmentCheck: Boolean
) {

    enum class Modality {
        FACE,
        FINGERPRINT;
    }
}
