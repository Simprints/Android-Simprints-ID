package com.simprints.id.shared

import com.simprints.id.data.db.remote.people.RemotePeopleManager
import com.simprints.id.data.db.remote.sessions.RemoteSessionsManager
import com.simprints.id.network.NetworkConstants
import com.simprints.id.network.SimApiClient
import com.simprints.id.secure.SecureApiInterface
import com.simprints.testframework.common.syntax.whenever
import io.reactivex.Single
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.mock.BehaviorDelegate
import retrofit2.mock.MockRetrofit
import retrofit2.mock.NetworkBehavior
import java.util.concurrent.TimeUnit

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

inline fun <reified T> createMockBehaviorService(retrofit: Retrofit, failurePercent: Int, service: Class<T>): BehaviorDelegate<T> {
    val networkBehavior = NetworkBehavior.create()
    givenNetworkFailurePercentIs(networkBehavior, failurePercent)

    val mockRetrofit = MockRetrofit.Builder(retrofit)
        .networkBehavior(networkBehavior)
        .build()
    return mockRetrofit.create(service)
}

fun getBuilderResponse(statusCode: Int, body: String = "", contentType: String = "\"text/plain\""): Response.Builder {
    return Response.Builder()
        .code(statusCode)
        .message(body)
        .protocol(Protocol.HTTP_1_0)
        .body(ResponseBody.create(MediaType.parse(contentType), body.toByteArray()))
        .addHeader("content-type", contentType)
        .request(Request.Builder().url("http://localhost").build())
}

fun givenNetworkFailurePercentIs(behavior: NetworkBehavior, failurePercent: Int) {
    behavior.setDelay(0, TimeUnit.MILLISECONDS)
    behavior.setVariancePercent(0)
    behavior.setFailurePercent(failurePercent)
}

fun buildResponse(statusCode: Int, body: String = "", contentType: String = "\"text/plain\""): Response {
    return getBuilderResponse(statusCode, body, contentType).build()
}
