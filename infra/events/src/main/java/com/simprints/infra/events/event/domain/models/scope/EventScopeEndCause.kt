package com.simprints.infra.events.event.domain.models.scope

import androidx.annotation.Keep

@Keep
enum class EventScopeEndCause {
    WORKFLOW_ENDED,
    NEW_SESSION,
}
