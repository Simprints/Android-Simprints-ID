package com.simprints.infra.events.event.domain.models.subject

import androidx.annotation.Keep
import com.simprints.core.domain.common.TemplateIdentifier
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class FingerprintTemplate(
    val template: String,
    val finger: TemplateIdentifier,
)
