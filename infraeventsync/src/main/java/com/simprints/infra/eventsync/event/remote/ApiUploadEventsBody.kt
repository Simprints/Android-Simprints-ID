package com.simprints.infra.eventsync.event.remote

import androidx.annotation.Keep
import com.simprints.infra.eventsync.event.remote.models.ApiEvent

@Keep
internal data class ApiUploadEventsBody(val events: List<ApiEvent>)
