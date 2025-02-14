package com.simprints.infra.eventsync.event.usecases

import com.simprints.infra.config.store.models.Project
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.scope.EventScope
import com.simprints.infra.eventsync.event.remote.models.fromDomainToApi
import com.simprints.infra.eventsync.event.remote.models.session.ApiEventScope
import com.simprints.infra.eventsync.event.remote.models.session.fromDomainToApi
import javax.inject.Inject

internal class MapDomainEventScopeToApiUseCase @Inject constructor(
    private val mapDomainEventToApiUseCase: MapDomainEventToApiUseCase,
) {

    operator fun invoke(
        scope: EventScope,
        events: List<Event>,
        project: Project,
    ): ApiEventScope {
        val apiEvents = events.map { event -> mapDomainEventToApiUseCase(event, project) }
        return ApiEventScope(
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
            events = apiEvents,
        )
    }
}
