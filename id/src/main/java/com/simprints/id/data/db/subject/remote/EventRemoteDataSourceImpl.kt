package com.simprints.id.data.db.subject.remote

import com.simprints.id.data.db.common.models.EventCount
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.remote.models.ApiEvents
import com.simprints.id.data.db.event.remote.models.fromApiToDomain
import com.simprints.id.data.db.event.remote.models.fromDomainToApi
import com.simprints.id.data.db.subjects_sync.down.domain.SyncEventQuery
import com.simprints.id.network.SimApiClient
import com.simprints.id.network.SimApiClientFactory
import java.io.InputStream

class EventRemoteDataSourceImpl(private val simApiClientFactory: SimApiClientFactory) : EventRemoteDataSource {

    override suspend fun count(query: SyncEventQuery): List<EventCount> = with(query.fromDomainToApi()) {
        executeCall("EventCount") { subjectsRemoteInterface ->
            subjectsRemoteInterface.countEvents(
                projectId = projectId,
                moduleIds = moduleIds,
                attendantId = userId,
                subjectId = subjectId,
                modes = modes,
                lastEventId = lastEventId,
                eventType = types.map { it.name }
            ).map { it.fromApiToDomain() }
        }
    }

    override suspend fun getStreaming(query: SyncEventQuery): InputStream = with(query.fromDomainToApi()) {
        executeCall("EventDownload") { subjectsRemoteInterface ->
            subjectsRemoteInterface.downloadEvents(
                projectId = projectId,
                moduleIds = moduleIds,
                attendantId = userId,
                subjectId = subjectId,
                modes = modes,
                lastEventId = lastEventId,
                eventType = types.map { it.name }
            )
        }
    }.byteStream()

    override suspend fun post(projectId: String, events: List<Event>) {
        executeCall("EventUpload") {
            it.uploadEvents(projectId, ApiEvents(events.map { it.fromDomainToApi() }))
        }
    }

    private suspend fun <T> executeCall(nameCall: String, block: suspend (EventRemoteInterface) -> T): T =
        with(getSubjectsApiClient()) {
            executeCall(nameCall) {
                block(it)
            }
        }

    suspend fun getSubjectsApiClient(): SimApiClient<EventRemoteInterface> =
        simApiClientFactory.buildClient(EventRemoteInterface::class)
}
