package com.simprints.id.secure

import com.simprints.id.BuildConfig
import com.simprints.id.secure.models.Nonce
import com.simprints.id.secure.models.NonceScope
import com.simprints.id.secure.models.PublicKeyString
import com.simprints.id.tools.base.RxJavaTest
import com.simprints.id.tools.retrofit.FakeResponseInterceptor
import com.simprints.id.tools.retrofit.buildResponse
import com.simprints.id.tools.retrofit.givenNetworkFailurePercentIs
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Query
import retrofit2.mock.BehaviorDelegate
import retrofit2.mock.Calls
import retrofit2.mock.MockRetrofit
import retrofit2.mock.NetworkBehavior
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class)
class NonceManagerTest : RxJavaTest() {

    private val validNonceJsonResponse = "{\"value\":\"nonce_from_server\"}"

    @Test
    fun successfulResponse_shouldObtainANonce() {
        val apiService = ApiService()
        forceOkHttpToReturnSuccessfulResponse(apiService.okHttpClientConfig)

        val testObserver = makeTestRequestNonce(apiService.api)
        testObserver.awaitTerminalEvent()

        testObserver
            .assertNoErrors()
            .assertValue { nonce -> nonce.value == "nonce_from_server" }
    }

    @Test
    fun offline_shouldThrowAnException() {
        val apiService = ApiService()
        var apiServiceMock = createMockServiceToFailRequests(apiService.retrofit)

        val testObserver = makeTestRequestNonce(apiServiceMock)
        testObserver.awaitTerminalEvent()

        testObserver.assertError(IOException::class.java)
    }

    @Test
    fun receivingAnErrorFromServer_shouldThrowAnException() {
        val apiService = ApiService()
        apiService.okHttpClientConfig.addInterceptor(FakeResponseInterceptor(500))

        val testObserver = makeTestRequestNonce(apiService.api)
        testObserver.awaitTerminalEvent()

        testObserver.assertError(HttpException::class.java)
    }

    private fun forceOkHttpToReturnSuccessfulResponse(okHttpClientConfig: OkHttpClient.Builder) {
        okHttpClientConfig.addInterceptor(FakeResponseInterceptor(200, validNonceJsonResponse))
    }

    private fun makeTestRequestNonce(apiServiceMock: ApiServiceInterface): TestObserver<Nonce> {
        return NonceManager(apiServiceMock).requestNonce(NonceScope("projectId_param", "userId_param"))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .test()
    }

    private fun createMockServiceToFailRequests(retrofit: Retrofit): ApiServiceMock {
        // Creating a mockServer with 100% of failure rate.
        val networkBehavior = NetworkBehavior.create()
        givenNetworkFailurePercentIs(networkBehavior, 100)

        val mockRetrofit = MockRetrofit.Builder(retrofit)
            .networkBehavior(networkBehavior)
            .build()
        return ApiServiceMock(mockRetrofit.create(ApiServiceInterface::class.java))
    }
}

// It's required to use NetworkBehavior, even if response is not used in the tests (e.g failing responses due to no connectivity)
// To mock response (code, body, type) use FakeResponseInterceptor for okHttpClient
class ApiServiceMock(private val delegate: BehaviorDelegate<ApiServiceInterface>) : ApiServiceInterface {
    private val response = buildResponse(200, "nonce_from_server")

    override fun publicKey(@Query("key") key: String): Single<PublicKeyString> {
        return delegate.returningResponse(Calls.response(Response.success(response.body(), response))).publicKey(key)
    }

    override fun nonce(headers: Map<String, String>, key: String): Single<Nonce> {
        return delegate.returningResponse(Calls.response(Response.success(response.body(), response))).nonce(headers, key)
    }
}
