package com.simprints.id.data.db.person.remote

import com.simprints.core.network.SimApiClientFactory
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.common.models.EventCount
import com.simprints.id.data.db.person.remote.models.personcounts.fromApiToDomain
import com.simprints.id.data.db.person.remote.models.personevents.ApiEventQuery
import com.simprints.id.data.db.person.remote.models.personevents.ApiEvents
import com.simprints.id.tools.utils.retrySimNetworkCalls
import okhttp3.ResponseBody

class EventRemoteDataSourceImpl(private val remoteDbManager: RemoteDbManager,
                                private val simApiClientFactory: SimApiClientFactory) : EventRemoteDataSource {

    override suspend fun count(query: ApiEventQuery): List<EventCount> =
        makeNetworkRequest({ peopleRemoteInterface ->
            peopleRemoteInterface.requestRecordCount(
                projectId = query.projectId,
                moduleIds = query.moduleIds,
                attendantId = query.userId,
                subjectId = query.subjectId,
                modes = query.modes,
                lastEventId = query.lastEventId,
                eventType = query.types.map { it.name }
            ).map { it.fromApiToDomain() }
        }, "RecordCount")

    override suspend fun get(query: ApiEventQuery): ResponseBody =
        makeNetworkRequest({ peopleRemoteInterface ->
            peopleRemoteInterface.downloadEnrolmentEvents(
                projectId = query.projectId,
                moduleIds = query.moduleIds,
                attendantId = query.userId,
                subjectId = query.subjectId,
                modes = query.modes,
                lastEventId = query.lastEventId,
                eventType = query.types.map { it.name }
            )
        }, "RecordDownload")

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
