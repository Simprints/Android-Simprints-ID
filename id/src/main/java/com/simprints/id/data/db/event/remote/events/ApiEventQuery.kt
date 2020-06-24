package com.simprints.id.data.db.event.remote.events

import com.simprints.id.data.db.subjects_sync.down.domain.EventQuery
import com.simprints.id.data.db.event.remote.events.subject.ApiModes

data class ApiEventQuery(val projectId: String,
                         val userId: String?,
                         val moduleIds: List<String>?,
                         val subjectId: String?,
                         val lastEventId: String?,
                         val modes: List<ApiModes>,
                         val types: List<ApiEventPayloadType>)

fun EventQuery.fromDomainToApi() =
    ApiEventQuery(projectId, userId, moduleIds, subjectId,
        lastEventId, modes.map { it.fromDomainToApi() }, types.map { com.simprints.id.data.db.event.remote.events.subject.fromDomainToApi() })
