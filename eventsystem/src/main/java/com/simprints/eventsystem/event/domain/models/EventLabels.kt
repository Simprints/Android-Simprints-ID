package com.simprints.eventsystem.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.modality.Modes

@Keep
data class EventLabels(
    val projectId: String? = null,
    val attendantId: String? = null,
    val moduleIds: List<String>? = null,
    val mode: List<Modes>? = null,
    val sessionId: String? = null,
    val deviceId: String? = null)
