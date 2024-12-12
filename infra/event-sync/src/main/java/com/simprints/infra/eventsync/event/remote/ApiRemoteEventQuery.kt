package com.simprints.infra.eventsync.event.remote

internal data class ApiRemoteEventQuery(
    val projectId: String,
    val userId: String? = null,
    val moduleId: String? = null,
    val subjectId: String? = null,
    val lastEventId: String? = null,
)
