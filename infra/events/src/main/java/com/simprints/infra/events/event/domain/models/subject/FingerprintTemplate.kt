package com.simprints.infra.events.event.domain.models.subject

import androidx.annotation.Keep
import com.simprints.core.domain.sample.SampleIdentifier

@Keep
data class FingerprintTemplate(
    val template: String,
    val finger: SampleIdentifier,
)
