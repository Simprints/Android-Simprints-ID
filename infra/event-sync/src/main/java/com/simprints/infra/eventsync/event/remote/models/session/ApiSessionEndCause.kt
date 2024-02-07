package com.simprints.infra.eventsync.event.remote.models.session

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.session.SessionEndCause

@Keep
internal enum class ApiSessionEndCause {

    WORKFLOW_ENDED,
    NEW_SESSION,
}

internal fun SessionEndCause?.fromDomainToApi(): ApiSessionEndCause = when (this) {
    null, SessionEndCause.WORKFLOW_ENDED -> ApiSessionEndCause.WORKFLOW_ENDED
    SessionEndCause.NEW_SESSION -> ApiSessionEndCause.NEW_SESSION
}
