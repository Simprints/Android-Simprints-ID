package com.simprints.id.data.db.event.remote.models

import com.simprints.id.data.db.event.remote.models.subject.ApiModes
import com.simprints.id.data.db.event.remote.models.subject.fromDomainToApi
import com.simprints.id.data.db.subjects_sync.down.domain.SyncEventQuery

data class ApiEventQuery(val projectId: String,
                         val userId: String?,
                         val moduleIds: List<String>?,
                         val subjectId: String?,
                         val lastEventId: String?,
                         val modes: List<ApiModes>,
                         val types: List<ApiEventPayloadType>)

fun SyncEventQuery.fromDomainToApi() =
    ApiEventQuery(projectId, userId, moduleIds, subjectId,
        lastEventId, modes.map { it.fromDomainToApi() }, types.map { it.fromDomainToApi() })
