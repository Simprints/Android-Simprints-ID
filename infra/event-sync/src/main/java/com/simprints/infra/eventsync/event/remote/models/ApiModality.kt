package com.simprints.infra.eventsync.event.remote.models

import com.simprints.infra.config.store.models.GeneralConfiguration.Modality
import com.simprints.infra.eventsync.event.remote.models.ApiModality.EAR
import com.simprints.infra.eventsync.event.remote.models.ApiModality.FACE
import com.simprints.infra.eventsync.event.remote.models.ApiModality.FINGERPRINT

internal enum class ApiModality {
    FACE,
    FINGERPRINT,
    EAR,
}

internal fun Modality.fromDomainToApi(): ApiModality = when (this) {
    Modality.FACE -> FACE // TODO for now
    Modality.FINGERPRINT -> FINGERPRINT
    Modality.EAR -> EAR
}
