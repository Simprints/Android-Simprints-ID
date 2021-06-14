package com.simprints.eventsystem.event.remote.models

import com.simprints.core.domain.modality.Modality
import com.simprints.eventsystem.event.remote.models.ApiModality.FACE
import com.simprints.eventsystem.event.remote.models.ApiModality.FINGERPRINT

enum class ApiModality {
    FACE,
    FINGERPRINT
}

fun Modality.fromDomainToApi() = when(this) {
    Modality.FACE -> FACE
    Modality.FINGER -> FINGERPRINT
}
