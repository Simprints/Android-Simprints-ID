package com.simprints.id.data.db.event.remote

import com.simprints.id.data.db.event.remote.models.ApiEventPayloadType

data class ApiRemoteEventQuery(val projectId: String,
                               val userId: String? = null,
                               val moduleIds: List<String>? = null,
                               val subjectId: String? = null,
                               val lastEventId: String? = null,
                               val modes: List<ApiModes>,
                               val types: List<ApiEventPayloadType>)
