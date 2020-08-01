package com.simprints.id.data.db.event.remote

import com.simprints.id.data.db.common.models.EventCount
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.remote.models.fromApiToDomain
import com.simprints.id.data.db.event.remote.models.fromDomainToApi
import com.simprints.id.network.SimApiClient
import com.simprints.id.network.SimApiClientFactory
import java.io.InputStream

class EventRemoteDataSourceImpl(private val simApiClientFactory: SimApiClientFactory) : EventRemoteDataSource {

    override suspend fun count(query: ApiEventQuery): List<EventCount> =
        with(query) {
            executeCall("EventCount") { eventsRemoteInterface ->
                eventsRemoteInterface.countEvents(
                    projectId = projectId,
                    moduleIds = moduleIds,
                    attendantId = userId,
                    subjectId = subjectId,
                    modes = modes,
                    lastEventId = lastEventId,
                    eventType = types.map { it.key }
                ).map { it.fromApiToDomain() }
            }
        }

    override suspend fun getStreaming(query: ApiEventQuery): InputStream =
        with(query) {
            executeCall("EventDownload") { eventsRemoteInterface ->
                eventsRemoteInterface.downloadEvents(
                    projectId = projectId,
                    moduleIds = moduleIds,
                    attendantId = userId,
                    subjectId = subjectId,
                    modes = modes,
                    lastEventId = lastEventId,
                    eventType = types.map { it.key }
                )
            }
        }.byteStream()

    override suspend fun post(projectId: String, events: List<Event>) {
        executeCall("EventUpload") {
            it.uploadEvents(projectId, ApiUploadEventsBody(events.map { it.fromDomainToApi() }))
        }
    }

    private suspend fun <T> executeCall(nameCall: String, block: suspend (EventRemoteInterface) -> T): T =
        with(getEventsApiClient()) {
            executeCall(nameCall) {
                block(it)
            }
        }

    private suspend fun getEventsApiClient(): SimApiClient<EventRemoteInterface> =
        simApiClientFactory.buildClient(EventRemoteInterface::class)
}
