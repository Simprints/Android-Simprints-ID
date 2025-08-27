package com.simprints.infra.config.store.models

import com.simprints.core.domain.modality.Modality

data class GeneralConfiguration(
    val modalities: List<Modality>,
    val matchingModalities: List<Modality>,
    val languageOptions: List<String>,
    val defaultLanguage: String,
    val collectLocation: Boolean,
    val duplicateBiometricEnrolmentCheck: Boolean,
    val settingsPassword: SettingsPasswordConfig,
)
