package com.simprints.id.data.db.event.remote

import androidx.annotation.Keep
import com.simprints.id.data.db.event.remote.models.ApiEvent

@Keep
data class ApiUploadEventsBody(val events: List<ApiEvent>)
