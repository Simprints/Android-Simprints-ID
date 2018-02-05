package com.simprints.id.secure

import com.simprints.id.BuildConfig
import com.simprints.id.secure.models.PublicKeyString
import com.simprints.id.tools.base.RxJavaTest
import com.simprints.id.tools.retrofit.FakeResponseInterceptor
import com.simprints.id.tools.retrofit.givenNetworkFailurePercentIs
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.mock.MockRetrofit
import retrofit2.mock.NetworkBehavior
import java.io.IOException


@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class)
class PublicKeyManagerTest : RxJavaTest() {

    private val validPublicKeyJsonResponse = "{\"value\":\"public_key_from_server\"}"

    @Test
    fun successfulResponse_shouldObtainThePublicKey() {
        val apiService = ApiService()
        forceOkHttpToReturnSuccessfulResponse(apiService.okHttpClientConfig)

        val testObserver = makeTestRequestPublicKey(apiService.api)
        testObserver.awaitTerminalEvent()

        testObserver
            .assertNoErrors()
            .assertValue { publicKeyString -> publicKeyString.value == "public_key_from_server" }
    }
    @Test
    fun offline_shouldThrowAnException() {
        val apiService = ApiService()
        val apiServiceMock = createMockServiceToFailRequests(apiService.retrofit)

        val testObserver = makeTestRequestPublicKey(apiServiceMock)
        testObserver.awaitTerminalEvent()

        testObserver.assertError(IOException::class.java)
    }

    @Test
    fun receivingAnErrorFromServer_shouldThrowAnException() {
        val apiService = ApiService()
        apiService.okHttpClientConfig.addInterceptor(FakeResponseInterceptor(500))

        val testObserver = makeTestRequestPublicKey(apiService.api)
        testObserver.awaitTerminalEvent()

        testObserver.assertError(HttpException::class.java)
    }

    private fun forceOkHttpToReturnSuccessfulResponse(okHttpClientConfig: OkHttpClient.Builder) {
        okHttpClientConfig.addInterceptor(FakeResponseInterceptor(200, validPublicKeyJsonResponse))
    }

    private fun makeTestRequestPublicKey(apiServiceMock: ApiServiceInterface): TestObserver<PublicKeyString> {
        return PublicKeyManager(apiServiceMock).requestPublicKey()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .test()
    }

    private fun createMockServiceToFailRequests(retrofit: Retrofit): ApiServiceInterface {
        // Creating a mockServer with 100% of failure rate.
        val networkBehavior = NetworkBehavior.create()
        givenNetworkFailurePercentIs(networkBehavior, 100)

        val mockRetrofit = MockRetrofit.Builder(retrofit)
            .networkBehavior(networkBehavior)
            .build()
        return ApiServiceMock(mockRetrofit.create(ApiServiceInterface::class.java))
    }
}
