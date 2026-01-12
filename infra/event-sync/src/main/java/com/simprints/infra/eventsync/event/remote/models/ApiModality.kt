package com.simprints.infra.eventsync.event.remote.models

import com.simprints.core.domain.common.Modality
import com.simprints.infra.eventsync.event.remote.models.ApiModality.FACE
import com.simprints.infra.eventsync.event.remote.models.ApiModality.FINGERPRINT
import kotlinx.serialization.Serializable

@Serializable
internal enum class ApiModality {
    FACE,
    FINGERPRINT,
}

internal fun Modality.fromDomainToApi(): ApiModality = when (this) {
    Modality.FACE -> FACE
    Modality.FINGERPRINT -> FINGERPRINT
}
