package com.simprints.eventsystem.event.remote.models

import com.simprints.eventsystem.event.remote.models.ApiModality.FACE
import com.simprints.eventsystem.event.remote.models.ApiModality.FINGERPRINT
import com.simprints.infra.config.domain.models.GeneralConfiguration.Modality

enum class ApiModality {
    FACE,
    FINGERPRINT
}

fun Modality.fromDomainToApi(): ApiModality = when (this) {
    Modality.FACE -> FACE
    Modality.FINGERPRINT -> FINGERPRINT
}
