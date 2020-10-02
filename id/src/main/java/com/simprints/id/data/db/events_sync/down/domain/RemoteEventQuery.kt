package com.simprints.id.data.db.events_sync.down.domain

import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.event.remote.ApiRemoteEventQuery
import com.simprints.id.data.db.event.remote.fromDomainToApi
import com.simprints.id.data.db.event.remote.models.fromDomainToApi
import com.simprints.id.domain.modality.Modes

data class RemoteEventQuery(val projectId: String,
                            val attendantId: String? = null,
                            val moduleIds: List<String>? = null,
                            val subjectId: String? = null,
                            val lastEventId: String? = null,
                            val modes: List<Modes>,
                            val types: List<EventType>)

fun RemoteEventQuery.fromDomainToApi() =
    ApiRemoteEventQuery(
        projectId = projectId,
        userId = attendantId,
        moduleIds = moduleIds,
        subjectId = subjectId,
        lastEventId = lastEventId,
        modes = modes.map { it.fromDomainToApi() },
        types = types.map { it.fromDomainToApi() }
    )
