package com.simprints.infra.events.event.domain.models.subject

import androidx.annotation.Keep
import com.simprints.core.domain.fingerprint.IFingerIdentifier

@Keep
data class FingerprintTemplate(
    val quality: Int,
    val template: String,
    val finger: IFingerIdentifier,
)
