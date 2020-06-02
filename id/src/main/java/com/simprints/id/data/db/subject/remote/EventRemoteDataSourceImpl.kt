package com.simprints.id.data.db.subject.remote

import com.simprints.id.data.db.common.models.EventCount
import com.simprints.id.data.db.subject.domain.subjectevents.Events
import com.simprints.id.data.db.subject.remote.models.subjectcounts.fromApiToDomain
import com.simprints.id.data.db.subject.remote.models.subjectevents.fromDomainToApi
import com.simprints.id.data.db.subjects_sync.down.domain.EventQuery
import com.simprints.id.network.SimApiClient
import com.simprints.id.network.SimApiClientFactory
import java.io.InputStream

class EventRemoteDataSourceImpl(private val simApiClientFactory: SimApiClientFactory) : EventRemoteDataSource {

    override suspend fun count(query: EventQuery): List<EventCount> = with(query.fromDomainToApi()) {
        executeCall("EventCount") { subjectsRemoteInterface ->
            subjectsRemoteInterface.countEvents(
                projectId = projectId,
                moduleIds = moduleIds,
                attendantId = userId,
                subjectId = subjectId,
                modes = modes,
                lastEventId = lastEventId,
                eventType = types.map { it.apiName }
            ).map { it.fromApiToDomain() }
        }
    }

    override suspend fun getStreaming(query: EventQuery): InputStream = with(query.fromDomainToApi()) {
        executeCall("EventDownload") { subjectsRemoteInterface ->
            subjectsRemoteInterface.downloadEvents(
                projectId = projectId,
                moduleIds = moduleIds,
                attendantId = userId,
                subjectId = subjectId,
                modes = modes,
                lastEventId = lastEventId,
                eventType = types.map { it.apiName }
            )
        }
    }.byteStream()

    override suspend fun post(projectId: String, events: Events) {
        executeCall("EventUpload") {
            it.uploadEvents(projectId, events.fromDomainToApi())
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
