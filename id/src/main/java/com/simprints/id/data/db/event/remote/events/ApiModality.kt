package com.simprints.id.data.db.event.remote.events

import com.simprints.id.data.db.event.remote.events.ApiModality.FACE
import com.simprints.id.data.db.event.remote.events.ApiModality.FINGERPRINT
import com.simprints.id.domain.modality.Modality

enum class ApiModality {
    FACE,
    FINGERPRINT
}

fun Modality.fromDomainToApi() = when(this) {
    Modality.FACE -> FACE
    Modality.FINGER -> FINGERPRINT
}
