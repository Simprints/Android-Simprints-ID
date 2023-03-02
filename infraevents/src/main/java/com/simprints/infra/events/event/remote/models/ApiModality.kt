package com.simprints.infra.events.remote.models

import com.simprints.infra.config.domain.models.GeneralConfiguration.Modality
import com.simprints.infra.events.remote.models.ApiModality.FACE
import com.simprints.infra.events.remote.models.ApiModality.FINGERPRINT

enum class ApiModality {
    FACE,
    FINGERPRINT
}

fun Modality.fromDomainToApi(): ApiModality = when (this) {
    Modality.FACE -> FACE
    Modality.FINGERPRINT -> FINGERPRINT
}
