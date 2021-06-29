package com.simprints.eventsystem.event.remote

import androidx.annotation.Keep
import com.simprints.eventsystem.event.remote.models.ApiEvent

@Keep
data class ApiUploadEventsBody(val events: List<ApiEvent>)
