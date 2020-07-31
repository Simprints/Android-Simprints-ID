package com.simprints.id.data.db.event.remote.models

import com.simprints.id.data.db.event.remote.models.ApiModality.FACE
import com.simprints.id.data.db.event.remote.models.ApiModality.FINGERPRINT
import com.simprints.id.domain.modality.Modality

enum class ApiModality {
    FACE,
    FINGERPRINT
}

fun Modality.fromDomainToApi() = when(this) {
    Modality.FACE -> FACE
    Modality.FINGER -> FINGERPRINT
}
