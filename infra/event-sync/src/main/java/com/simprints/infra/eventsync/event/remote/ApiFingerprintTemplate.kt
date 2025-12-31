package com.simprints.infra.eventsync.event.remote

import androidx.annotation.Keep
import com.simprints.core.domain.common.TemplateIdentifier
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class ApiFingerprintTemplate(
    val template: String,
    val finger: TemplateIdentifier,
)
