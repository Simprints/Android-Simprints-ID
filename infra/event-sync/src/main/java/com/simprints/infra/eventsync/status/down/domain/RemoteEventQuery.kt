package com.simprints.infra.eventsync.status.down.domain

import androidx.annotation.Keep
import com.simprints.core.domain.modality.Modes
import com.simprints.infra.eventsync.event.remote.ApiRemoteEventQuery


@Keep
internal data class RemoteEventQuery(
    val projectId: String,
    val attendantId: String? = null,
    val moduleId: String? = null,
    val subjectId: String? = null,
    val lastEventId: String? = null,
    val modes: List<Modes>,
) {

    internal fun fromDomainToApi() = ApiRemoteEventQuery(
        projectId = projectId,
        userId = attendantId,
        moduleId = moduleId,
        subjectId = subjectId,
        lastEventId = lastEventId,
    )
}

