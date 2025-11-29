package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class EventLabels(
    val projectId: String? = null,
    val sessionId: String? = null,
    val deviceId: String? = null,
)
