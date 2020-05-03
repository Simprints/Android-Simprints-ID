package com.simprints.id.data.db.subject.remote

import com.simprints.core.network.SimApiClientFactory
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.common.models.EventCount
import com.simprints.id.data.db.subjects_sync.down.domain.EventQuery
import com.simprints.id.data.db.subject.domain.subjectevents.Events
import com.simprints.id.data.db.subject.remote.models.subjectcounts.fromApiToDomain
import com.simprints.id.data.db.subject.remote.models.subjectevents.fromDomainToApi
import com.simprints.id.tools.utils.retrySimNetworkCalls
import java.io.InputStream

class EventRemoteDataSourceImpl(private val remoteDbManager: RemoteDbManager,
                                private val simApiClientFactory: SimApiClientFactory) : EventRemoteDataSource {

    override suspend fun count(query: EventQuery): List<EventCount> = with(query.fromDomainToApi()) {
        makeNetworkRequest({ peopleRemoteInterface ->
            peopleRemoteInterface.countEvents(
                projectId = projectId,
                moduleIds = moduleIds,
                attendantId = userId,
                subjectId = subjectId,
                modes = modes,
                lastEventId = lastEventId,
                eventType = types.map { it.apiName }
            ).map { it.fromApiToDomain() }
        }, "EventCount")
    }

    override suspend fun getStreaming(query: EventQuery): InputStream = with(query.fromDomainToApi()) {
        makeNetworkRequest({ peopleRemoteInterface ->
            peopleRemoteInterface.downloadEvents(
                projectId = projectId,
                moduleIds = moduleIds,
                attendantId = userId,
                subjectId = subjectId,
                modes = modes,
                lastEventId = lastEventId,
                eventType = types.map { it.apiName }
            )
        }, "EventDownload")
    }.byteStream()

    override suspend fun post(projectId: String, events: Events) {
        makeNetworkRequest({
            it.uploadEvents(projectId, events.fromDomainToApi())
        }, "EventUpload")
    }

    private suspend fun <T> makeNetworkRequest(
            block: suspend (client: EventRemoteInterface) -> T,
            traceName: String
    ): T =
        retrySimNetworkCalls(getPeopleApiClient(), block, traceName)


    internal suspend fun getPeopleApiClient(): EventRemoteInterface {
        val token = remoteDbManager.getCurrentToken()
        return simApiClientFactory.build<EventRemoteInterface>(token).api
    }
}
