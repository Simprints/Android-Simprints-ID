package com.simprints.id.secure

import com.simprints.id.BuildConfig
import com.simprints.id.secure.models.Nonce
import com.simprints.id.secure.models.NonceScope
import com.simprints.id.tools.base.RxJavaTest
import com.simprints.id.tools.retrofit.FakeResponseInterceptor
import com.simprints.id.tools.roboletric.TestApplication
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.HttpException
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, application = TestApplication::class)
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
        val apiServiceMock = createMockServiceToFailRequests(apiService.retrofit)

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
}
