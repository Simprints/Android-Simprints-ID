package com.simprints.id.commontesttools.state

import com.simprints.id.data.db.remote.people.RemotePeopleManager
import com.simprints.id.data.db.remote.sessions.RemoteSessionsManager
import com.simprints.core.network.NetworkConstants
import com.simprints.core.network.SimApiClient
import com.simprints.id.secure.SecureApiInterface
import com.simprints.testtools.common.retrofit.createMockBehaviorService
import com.simprints.testtools.common.syntax.whenever
import io.reactivex.Single

fun replaceRemoteDbManagerApiClientsWithFailingClients(remotePeopleManagerSpy: RemotePeopleManager, remoteSessionsManagerSpy: RemoteSessionsManager) {
    createFailingApiClientForRemotePeopleManager(remotePeopleManagerSpy) { getPeopleApiClient() }
    createFailingApiClientForRemoteSessionsManager(remoteSessionsManagerSpy) { getSessionsApiClient() }
    createFailingApiClientForRemoteSessionsManager(remoteSessionsManagerSpy) { getSessionsApiClient() }
}

fun replaceSecureApiClientWithFailingClientProvider() = createFailingApiClient<SecureApiInterface>()

inline fun <reified T> createFailingApiClientForRemotePeopleManager(remotePeopleManagerSpy: RemotePeopleManager, getClient: RemotePeopleManager.() -> Single<T>) {
    val poorNetworkClientMock: T = createFailingApiClient()
    whenever(remotePeopleManagerSpy.getClient()).thenReturn(Single.just(poorNetworkClientMock))
}

inline fun <reified T> createFailingApiClientForRemoteSessionsManager(remoteSessionsManagerSpy: RemoteSessionsManager, getClient: RemoteSessionsManager.() -> Single<T>) {
    val poorNetworkClientMock: T = createFailingApiClient()
    whenever(remoteSessionsManagerSpy.getClient()).thenReturn(Single.just(poorNetworkClientMock))
}

inline fun <reified T> createFailingApiClient(): T {
    val apiClient = SimApiClient(T::class.java, NetworkConstants.baseUrl)
    return createMockBehaviorService(apiClient.retrofit, 100, T::class.java).returningResponse(null)
}
