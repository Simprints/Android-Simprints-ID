package com.simprints.infra.eventsync.event.remote.models.subject.biometricref.fingerprint

import androidx.annotation.Keep
import com.simprints.core.domain.sample.SampleIdentifier

@Keep
internal data class ApiFingerprintTemplate(
    val template: String,
    val finger: SampleIdentifier,
)
