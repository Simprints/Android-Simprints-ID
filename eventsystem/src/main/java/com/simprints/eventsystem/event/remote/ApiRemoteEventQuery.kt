package com.simprints.eventsystem.event.remote

import com.simprints.eventsystem.event.remote.models.ApiEventPayloadType


internal data class ApiRemoteEventQuery(val projectId: String,
                               val userId: String? = null,
                               val moduleIds: List<String>? = null,
                               val subjectId: String? = null,
                               val lastEventId: String? = null,
                               val modes: List<ApiModes>,
                               val types: List<ApiEventPayloadType>)
