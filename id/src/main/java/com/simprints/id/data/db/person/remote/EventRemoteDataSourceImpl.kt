package com.simprints.id.data.db.person.remote

import com.simprints.core.network.SimApiClientFactory
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.common.models.EventCount
import com.simprints.id.data.db.people_sync.down.domain.EventQuery
import com.simprints.id.data.db.person.remote.models.personcounts.fromApiToDomain
import com.simprints.id.data.db.person.remote.models.personevents.ApiEvents
import com.simprints.id.data.db.person.remote.models.personevents.fromDomainToApi
import com.simprints.id.tools.utils.retrySimNetworkCalls
import okhttp3.ResponseBody

class EventRemoteDataSourceImpl(private val remoteDbManager: RemoteDbManager,
                                private val simApiClientFactory: SimApiClientFactory) : EventRemoteDataSource {

    override suspend fun count(query: EventQuery): List<EventCount> = with(query.fromDomainToApi()) {
        makeNetworkRequest({ peopleRemoteInterface ->
            peopleRemoteInterface.requestRecordCount(
                projectId = projectId,
                moduleIds = moduleIds,
                attendantId = userId,
                subjectId = subjectId,
                modes = modes,
                lastEventId = lastEventId,
                eventType = types.map { it.name }
            ).map { it.fromApiToDomain() }
        }, "RecordCount")
    }

    override suspend fun get(query: EventQuery): ResponseBody = with(query.fromDomainToApi()) {
        makeNetworkRequest({ peopleRemoteInterface ->
            peopleRemoteInterface.downloadEnrolmentEvents(
                projectId = projectId,
                moduleIds = moduleIds,
                attendantId = userId,
                subjectId = subjectId,
                modes = modes,
                lastEventId = lastEventId,
                eventType = types.map { it.name }
            )
        }, "RecordDownload")
    }

    override suspend fun write(projectId: String, events: ApiEvents) {
        makeNetworkRequest({
            it.postEnrolmentRecordEvents(projectId, events)
        }, "RecordWrite")
    }

    private suspend fun <T> makeNetworkRequest(
        block: suspend (client: EnrolmentEventRecordRemoteInterface) -> T,
        traceName: String
    ): T =
        retrySimNetworkCalls(getPeopleApiClient(), block, traceName)


    internal suspend fun getPeopleApiClient(): EnrolmentEventRecordRemoteInterface {
        val token = remoteDbManager.getCurrentToken()
        return simApiClientFactory.build<EnrolmentEventRecordRemoteInterface>(token).api
    }
}
