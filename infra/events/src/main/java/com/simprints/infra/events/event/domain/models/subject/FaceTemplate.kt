package com.simprints.infra.events.event.domain.models.subject

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class FaceTemplate(
    val template: String,
)
