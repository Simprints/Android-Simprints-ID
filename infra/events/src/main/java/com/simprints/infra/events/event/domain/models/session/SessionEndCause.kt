package com.simprints.infra.events.event.domain.models.session

import androidx.annotation.Keep

@Keep
enum class SessionEndCause {

    WORKFLOW_ENDED,
    NEW_SESSION,;
}
