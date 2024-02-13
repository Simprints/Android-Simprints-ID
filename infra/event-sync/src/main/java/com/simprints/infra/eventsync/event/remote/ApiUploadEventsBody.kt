package com.simprints.infra.eventsync.event.remote

import androidx.annotation.Keep
import com.simprints.infra.eventsync.event.remote.models.session.ApiEventScope

@Keep
internal data class ApiUploadEventsBody(
    val sessions: List<ApiEventScope>,
)
