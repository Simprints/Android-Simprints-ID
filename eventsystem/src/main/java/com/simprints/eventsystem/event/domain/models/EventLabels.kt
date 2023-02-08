package com.simprints.eventsystem.event.domain.models

import androidx.annotation.Keep

@Keep
data class EventLabels(
    val projectId: String? = null,
    val sessionId: String? = null,
    val deviceId: String? = null
)
