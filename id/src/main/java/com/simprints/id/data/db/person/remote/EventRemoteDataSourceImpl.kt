package com.simprints.id.data.db.person.remote

import com.simprints.core.network.SimApiClientFactory
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.common.models.EventCount
import com.simprints.id.data.db.people_sync.down.domain.EventQuery
import com.simprints.id.data.db.person.domain.personevents.Events
import com.simprints.id.data.db.person.remote.models.personcounts.fromApiToDomain
import com.simprints.id.data.db.person.remote.models.personevents.fromDomainToApi
import com.simprints.id.tools.utils.retrySimNetworkCalls
import okhttp3.ResponseBody

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
                eventType = types.map { it.name }
            ).map { it.fromApiToDomain() }
        }, "EventCount")
    }

    override suspend fun get(query: EventQuery): ResponseBody = with(query.fromDomainToApi()) {
        makeNetworkRequest({ peopleRemoteInterface ->
            peopleRemoteInterface.downloadEvents(
                projectId = projectId,
                moduleIds = moduleIds,
                attendantId = userId,
                subjectId = subjectId,
                modes = modes,
                lastEventId = lastEventId,
                eventType = types.map { it.name }
            )
        }, "EventDownload")
    }

    override suspend fun post(projectId: String, events: Events) {
        makeNetworkRequest({
            it.uploadEvents(projectId, events)
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
