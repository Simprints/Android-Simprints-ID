package com.simprints.id.data.db.person.remote.models.personevents

import com.simprints.core.network.SimApiClientFactory
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.person.remote.PeopleRemoteInterface
import com.simprints.id.data.db.person.remote.models.personcounts.ApiEventCounts
import com.simprints.id.tools.utils.retrySimNetworkCalls
import okhttp3.ResponseBody

class EventRemoteDataSourceImpl(private val remoteDbManager: RemoteDbManager,
                                private val simApiClientFactory: SimApiClientFactory) : EventRemoteDataSource {

    override suspend fun count(query: ApiEventQuery): ApiEventCounts =
        makeNetworkRequest({ peopleRemoteInterface ->
            peopleRemoteInterface.requestRecordCount(
                projectId = query.projectId,
                moduleIds = query.moduleIds?.toTypedArray(),
                attendantId = query.userId,
                subjectId = query.subjectId,
                modes = query.modes.toTypedArray(),
                lastEventId = query.lastEventId,
                eventType = query.types.map { it.name }.toTypedArray()
            )
        }, "RecordCount")

    override suspend fun get(query: ApiEventQuery): ResponseBody =
        makeNetworkRequest({ peopleRemoteInterface ->
            peopleRemoteInterface.downloadEnrolmentEvents(
                projectId = query.projectId,
                moduleIds = query.moduleIds?.toTypedArray(),
                attendantId = query.userId,
                subjectId = query.subjectId,
                modes = query.modes.toTypedArray(),
                lastEventId = query.lastEventId,
                eventType = query.types.map { it.name }.toTypedArray()
            )
        }, "RecordDownload")

    override suspend fun write(projectId: String, events: ApiEvents) {
        makeNetworkRequest({
            it.postEnrolmentRecordEvents(projectId, events)
        }, "RecordWrite")
    }

    private suspend fun <T> makeNetworkRequest(
        block: suspend (client: PeopleRemoteInterface) -> T,
        traceName: String
    ): T =
        retrySimNetworkCalls(getPeopleApiClient(), block, traceName)


    private suspend fun getPeopleApiClient(): PeopleRemoteInterface {
        val token = remoteDbManager.getCurrentToken()
        return simApiClientFactory.build<PeopleRemoteInterface>(token).api
    }
}
