package com.simprints.infra.config.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.domain.GeneralConfiguration

@Keep
internal data class ApiGeneralConfiguration(
    val modalities: List<Modality>,
    val languageOptions: List<String>,
    val defaultLanguage: String,
    val collectLocation: Boolean,
    val duplicateBiometricEnrolmentCheck: Boolean
) {

    fun toDomain(): GeneralConfiguration =
        GeneralConfiguration(
            modalities.map { it.toDomain() },
            languageOptions,
            defaultLanguage,
            collectLocation,
            duplicateBiometricEnrolmentCheck,
        )

    @Keep
    enum class Modality {
        FACE,
        FINGERPRINT;

        fun toDomain(): GeneralConfiguration.Modality =
            when (this) {
                FACE -> GeneralConfiguration.Modality.FACE
                FINGERPRINT -> GeneralConfiguration.Modality.FINGERPRINT
            }
    }
}
