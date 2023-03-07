package com.simprints.infra.eventsync.event.remote.models

import com.simprints.infra.config.domain.models.GeneralConfiguration.Modality
import com.simprints.infra.eventsync.event.remote.models.ApiModality.FACE
import com.simprints.infra.eventsync.event.remote.models.ApiModality.FINGERPRINT

enum class ApiModality {
    FACE,
    FINGERPRINT
}

fun Modality.fromDomainToApi(): ApiModality = when (this) {
    Modality.FACE -> FACE
    Modality.FINGERPRINT -> FINGERPRINT
}
