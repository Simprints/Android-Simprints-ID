package com.simprints.infra.eventsync.event.remote.models.session

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.scope.EventScope
import com.simprints.infra.eventsync.event.remote.models.ApiEvent
import com.simprints.infra.eventsync.event.remote.models.ApiModality
import com.simprints.infra.eventsync.event.remote.models.ApiTimestamp
import com.simprints.infra.eventsync.event.remote.models.fromDomainToApi

@Keep
internal data class ApiEventScope(
    val id: String,
    val projectId: String,
    val startTime: ApiTimestamp,
    val endTime: ApiTimestamp?,
    val endCause: ApiEventScopeEndCause,
    val modalities: List<ApiModality>,
    val sidVersion: String,
    val libSimprintsVersion: String,
    val language: String,
    val device: ApiDevice,
    val databaseInfo: ApiDatabaseInfo,
    val location: ApiLocation?,
    @JsonInclude(Include.NON_EMPTY)
    val projectConfigurationUpdatedAt: String,
    val projectConfigurationId: String,
    val events: List<ApiEvent>,
) {

    companion object {

        fun fromDomain(
            scope: EventScope,
            events: List<Event>,
        ) = ApiEventScope(
            id = scope.id,
            projectId = scope.projectId,
            startTime = scope.createdAt.fromDomainToApi(),
            endTime = scope.endedAt?.fromDomainToApi(),
            endCause = scope.payload.endCause.fromDomainToApi(),
            modalities = scope.payload.modalities.map { it.fromDomainToApi() },
            sidVersion = scope.payload.sidVersion,
            libSimprintsVersion = scope.payload.libSimprintsVersion,
            language = scope.payload.language,
            device = scope.payload.device.fromDomainToApi(),
            databaseInfo = scope.payload.databaseInfo.fromDomainToApi(),
            location = scope.payload.location?.fromDomainToApi(),
            projectConfigurationUpdatedAt = scope.payload.projectConfigurationUpdatedAt,
            projectConfigurationId = scope.payload.projectConfigurationId.orEmpty(),
            events = events.map { it.fromDomainToApi() }
        )
    }
}
