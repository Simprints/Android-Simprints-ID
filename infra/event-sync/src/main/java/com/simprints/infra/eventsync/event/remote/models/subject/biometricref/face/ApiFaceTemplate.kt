package com.simprints.infra.eventsync.event.remote.models.subject.biometricref.face

import androidx.annotation.Keep

@Keep
internal data class ApiFaceTemplate(
    val template: String,
)
