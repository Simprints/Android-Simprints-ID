package com.simprints.infra.events.remote

import androidx.annotation.Keep
import com.simprints.infra.events.remote.models.ApiEvent

@Keep
internal data class ApiUploadEventsBody(val events: List<ApiEvent>)
