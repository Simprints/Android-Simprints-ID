package com.simprints.eventsystem.events_sync.down.domain

import androidx.annotation.Keep
import com.simprints.core.domain.modality.Modes
import com.simprints.eventsystem.event.remote.ApiRemoteEventQuery
import com.simprints.eventsystem.event.remote.fromDomainToApi


@Keep
data class RemoteEventQuery(val projectId: String,
                            val attendantId: String? = null,
                            val moduleIds: List<String>? = null,
                            val subjectId: String? = null,
                            val lastEventId: String? = null,
                            val modes: List<Modes>)

internal fun RemoteEventQuery.fromDomainToApi() =
    ApiRemoteEventQuery(
        projectId = projectId,
        userId = attendantId,
        moduleIds = moduleIds,
        subjectId = subjectId,
        lastEventId = lastEventId,
        modes = modes.map {  it.fromDomainToApi() },
    )
