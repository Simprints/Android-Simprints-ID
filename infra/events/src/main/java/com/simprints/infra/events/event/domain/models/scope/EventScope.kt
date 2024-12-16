package com.simprints.infra.events.event.domain.models.scope

import androidx.annotation.Keep
import com.simprints.core.tools.time.Timestamp

@Keep
data class EventScope(
    val id: String,
    val projectId: String,
    val type: EventScopeType,
    val createdAt: Timestamp,
    var endedAt: Timestamp?,
    val payload: EventScopePayload,
)
