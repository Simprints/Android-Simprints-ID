package com.simprints.id.data.db.person.remote.models.personevents

import com.simprints.core.network.SimApiClientFactory
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.person.remote.PeopleRemoteInterface
import com.simprints.id.data.db.person.remote.models.personcounts.ApiEventCount
import com.simprints.id.data.db.person.remote.models.personcounts.ApiEventCounts
import com.simprints.id.tools.utils.retrySimNetworkCalls

class EventRemoteDataSourceImpl(private val remoteDbManager: RemoteDbManager,
                                private val simApiClientFactory: SimApiClientFactory) : EventRemoteDataSource {

    override suspend fun count(query: String): ApiEventCounts =
        makeNetworkRequest({
            TODO("make api call")
        }, "RecordCount")

    override suspend fun get(query: String): List<ApiEvent> {

    }

    override suspend fun write(events: List<ApiEvent>) {

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
