package com.simprints.id.commontesttools.state

import com.simprints.core.network.NetworkConstants
import com.simprints.core.network.SimApiClient
import com.simprints.id.data.analytics.eventdata.controllers.remote.RemoteSessionsManager
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource
import com.simprints.id.secure.SecureApiInterface
import com.simprints.testtools.common.retrofit.createMockBehaviorService
import com.simprints.testtools.common.syntax.whenever
import io.reactivex.Single
import kotlinx.coroutines.runBlocking

fun replaceRemoteDbManagerApiClientsWithFailingClients(personRemoteDataSourceSpy: PersonRemoteDataSource,
                                                       remoteSessionsManagerSpy: RemoteSessionsManager) {

    createFailingApiClientForRemotePeopleManager(personRemoteDataSourceSpy) {
        runBlocking {
            getPeopleApiClient()
        }
    }
    createFailingApiClientForRemoteSessionsManager(remoteSessionsManagerSpy) { getSessionsApiClient() }
    createFailingApiClientForRemoteSessionsManager(remoteSessionsManagerSpy) { getSessionsApiClient() }
}

fun replaceSecureApiClientWithFailingClientProvider() = createFailingApiClient<SecureApiInterface>()

inline fun <reified T> createFailingApiClientForRemotePeopleManager(personRemoteDataSourceSpy: PersonRemoteDataSource,
                                                                    getClient: PersonRemoteDataSource.() -> T) {
    val poorNetworkClientMock: T = createFailingApiClient()
    whenever(personRemoteDataSourceSpy.getClient()).thenReturn(poorNetworkClientMock)
}

inline fun <reified T> createFailingApiClientForRemoteSessionsManager(remoteSessionsManagerSpy: RemoteSessionsManager, getClient: RemoteSessionsManager.() -> Single<T>) {
    val poorNetworkClientMock: T = createFailingApiClient()
    whenever(remoteSessionsManagerSpy.getClient()).thenReturn(Single.just(poorNetworkClientMock))
}

inline fun <reified T> createFailingApiClient(): T {
    val apiClient = SimApiClient(T::class.java, NetworkConstants.baseUrl)
    return createMockBehaviorService(apiClient.retrofit, 100, T::class.java).returningResponse(null)
}
