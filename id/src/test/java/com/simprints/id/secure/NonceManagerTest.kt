package com.simprints.id.secure

import com.simprints.id.BuildConfig
import com.simprints.id.tools.base.RxJavaTest
import com.simprints.id.tools.retrofit.FakeResponseInterceptor
import com.simprints.id.tools.retrofit.buildResponse
import com.simprints.id.tools.retrofit.givenNetworkFailurePercentIs
import io.reactivex.Single
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.mock.BehaviorDelegate
import retrofit2.mock.Calls
import retrofit2.mock.MockRetrofit
import retrofit2.mock.NetworkBehavior
import java.io.IOException


@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class)
class NonceManagerTest : RxJavaTest() {

    @Test
    fun successfulResponse_shouldObtainANonce() {
        val apiService = ApiService()

        // Adding interceptor to return a fake response
        apiService.okHttpClientConfig.addInterceptor(FakeResponseInterceptor(200, "nonce_from_server"))

        val testObserver = NonceManager(apiService.api).requestNonce(NonceScope("projectId", "userID")).test()
        testObserver.awaitTerminalEvent()

        testObserver
            .assertNoErrors()
            .assertValue { nonce -> nonce == "nonce_from_server" }
    }

    @Test
    fun ifOffline_shouldThrowAnException() {
        val apiService = ApiService()

        // Creating a mockServer with 100% of failure rate.
        val networkBehavior = NetworkBehavior.create()
        givenNetworkFailurePercentIs(networkBehavior, 100)

        val mockRetrofit = MockRetrofit.Builder(apiService.retrofit)
            .networkBehavior(networkBehavior)
            .build()

        var apiServiceMock = ApiServiceMock(mockRetrofit.create(ApiServiceInterface::class.java))

        val testObserver = NonceManager(apiServiceMock).requestNonce(NonceScope("projectId", "userID")).test()
        testObserver.awaitTerminalEvent()
        testObserver.assertError(IOException::class.java)
    }

    @Test
    fun receivingAnErrorFromServer_shouldThrowAnException() {
        val apiService = ApiService()
        apiService.okHttpClientConfig.addInterceptor(FakeResponseInterceptor(500))

        val testObserver = NonceManager(apiService.api).requestNonce(NonceScope("projectId", "userID")).test()
        testObserver.awaitTerminalEvent()
        testObserver.assertError(HttpException::class.java)
    }
}

// It's required to use NetworkBehavior, even if response is not required (e.g failed comms due to no connectivity)
// To mock response (code, body, type) use FakeResponseInterceptor for okHttpClient
class ApiServiceMock(private val delegate: BehaviorDelegate<ApiServiceInterface>) : ApiServiceInterface {
    override fun nonce(headers: Map<String, String>, key: String): Single<String> {
        val response = buildResponse(200, "nonce_from_server")
        return delegate.returningResponse(Calls.response(Response.success(response.body(), response))).nonce(headers, key)
    }
}
