package com.simprints.eventsystem.event.remote


internal data class ApiRemoteEventQuery(val projectId: String,
                               val userId: String? = null,
                               val moduleIds: List<String>? = null,
                               val subjectId: String? = null,
                               val lastEventId: String? = null,
                               val modes: List<ApiModes>)
