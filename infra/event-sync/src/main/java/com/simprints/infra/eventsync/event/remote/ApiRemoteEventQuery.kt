package com.simprints.infra.eventsync.event.remote

import kotlinx.serialization.Serializable

@Serializable
internal data class ApiRemoteEventQuery(
    val projectId: String,
    val userId: String? = null,
    val moduleId: String? = null,
    val subjectId: String? = null,
    val lastEventId: String? = null,
)
