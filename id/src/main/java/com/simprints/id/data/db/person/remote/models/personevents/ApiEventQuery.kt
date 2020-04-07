package com.simprints.id.data.db.person.remote.models.personevents

import com.simprints.id.data.db.people_sync.down.domain.EventQuery
import com.simprints.id.data.db.person.remote.models.ApiModes
import com.simprints.id.data.db.person.remote.models.fromDomainToApi

data class ApiEventQuery(val projectId: String,
                         val userId: String?,
                         val moduleIds: List<String>?,
                         val subjectId: String?,
                         val lastEventId: String?,
                         val modes: List<ApiModes>,
                         val types: List<ApiEnrolmentRecordOperationType>)

fun EventQuery.fromDomainToApi() =
    ApiEventQuery(projectId, userId, moduleIds, subjectId,
        lastEventId, modes.map { it.fromDomainToApi() }, types.map { it.fromDomainToApi() })
