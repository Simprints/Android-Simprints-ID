package com.simprints.infra.events.event.domain.models.scope

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
enum class EventScopeEndCause {
    WORKFLOW_ENDED,
    NEW_SESSION,
}
