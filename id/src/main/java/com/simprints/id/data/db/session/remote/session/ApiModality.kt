package com.simprints.id.data.db.session.remote.session

import com.simprints.id.domain.modality.Modality

enum class ApiModality {
    FACE,
    FINGERPRINT
}

fun Modality.fromDomainToApi() = when(this) {
    Modality.FACE -> ApiModality.FACE
    Modality.FINGER -> ApiModality.FINGERPRINT
}
