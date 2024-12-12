package com.simprints.infra.eventsync.event.remote.models.session

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.scope.EventScopeEndCause

@Keep
internal enum class ApiEventScopeEndCause {
    WORKFLOW_ENDED,
    NEW_SESSION,
}

internal fun EventScopeEndCause?.fromDomainToApi(): ApiEventScopeEndCause = when (this) {
    null, EventScopeEndCause.WORKFLOW_ENDED -> ApiEventScopeEndCause.WORKFLOW_ENDED
    EventScopeEndCause.NEW_SESSION -> ApiEventScopeEndCause.NEW_SESSION
}
