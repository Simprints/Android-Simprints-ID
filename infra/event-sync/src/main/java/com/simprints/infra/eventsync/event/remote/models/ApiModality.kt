package com.simprints.infra.eventsync.event.remote.models

import com.simprints.infra.config.store.models.GeneralConfiguration.Modality
import com.simprints.infra.eventsync.event.remote.models.ApiModality.FACE
import com.simprints.infra.eventsync.event.remote.models.ApiModality.FINGERPRINT

internal enum class ApiModality {
    FACE,
    FINGERPRINT,
}

internal fun Modality.fromDomainToApi(): ApiModality = when (this) {
    Modality.FACE -> FACE
    Modality.FINGERPRINT -> FINGERPRINT
}
