package com.simprints.infra.eventsync.event.remote.models.subject.biometricref.fingerprint

import androidx.annotation.Keep
import com.simprints.moduleapi.fingerprint.IFingerIdentifier

@Keep
internal data class ApiFingerprintTemplate(
    val quality: Int,
    val template: String,
    val finger: IFingerIdentifier,
)

