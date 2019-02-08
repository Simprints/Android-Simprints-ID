package com.simprints.id.secure

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.activities.ShadowAndroidXMultiDex
import com.simprints.id.exceptions.safe.data.db.SimprintsInternalServerException
import com.simprints.id.network.SimApiClient
import com.simprints.id.secure.models.PublicKeyString
import com.simprints.id.shared.givenNetworkFailurePercentIs
import com.simprints.testframework.unit.reactive.RxJavaTest
import com.simprints.id.testUtils.retrofit.FakeResponseInterceptor
import com.simprints.id.testUtils.roboletric.TestApplication
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import retrofit2.Retrofit
import retrofit2.mock.MockRetrofit
import retrofit2.mock.NetworkBehavior
import java.io.IOException

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class PublicKeyManagerTest : RxJavaTest {

    private val validPublicKeyJsonResponse = "{\"value\":\"public_key_from_server\"}"
    private lateinit var apiClient: SimApiClient<SecureApiInterface>

    @Before
    fun setUp() {
        apiClient = SimApiClient(SecureApiInterface::class.java, SecureApiInterface.baseUrl)
    }

    @Test
    fun successfulResponse_shouldObtainThePublicKey() {
        forceOkHttpToReturnSuccessfulResponse(apiClient.okHttpClientConfig)

        val testObserver = makeTestRequestPublicKey(apiClient.api)
        testObserver.awaitTerminalEvent()

        testObserver
            .assertNoErrors()
            .assertValue { publicKeyString -> publicKeyString.value == "public_key_from_server" }
    }
    @Test
    fun offline_shouldThrowAnException() {
        val apiServiceMock = createMockServiceToFailRequests(apiClient.retrofit)

        val testObserver = makeTestRequestPublicKey(apiServiceMock)
        testObserver.awaitTerminalEvent()

        testObserver.assertError(IOException::class.java)
    }

    @Test
    fun receivingAnErrorFromServer_shouldThrowAnException() {
        apiClient.okHttpClientConfig.addInterceptor(FakeResponseInterceptor(500))

        val testObserver = makeTestRequestPublicKey(apiClient.api)
        testObserver.awaitTerminalEvent()

        testObserver.assertError(SimprintsInternalServerException::class.java)
    }

    private fun forceOkHttpToReturnSuccessfulResponse(okHttpClientConfig: OkHttpClient.Builder) {
        okHttpClientConfig.addInterceptor(FakeResponseInterceptor(200, validPublicKeyJsonResponse))
    }

    private fun makeTestRequestPublicKey(secureApiMock: SecureApiInterface): TestObserver<PublicKeyString> {
        return PublicKeyManager(secureApiMock).requestPublicKey("some_project_id", "some_user_id")
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .test()
    }

    private fun createMockServiceToFailRequests(retrofit: Retrofit): SecureApiInterface {
        // Creating a mockServer with 100% of failure rate.
        val networkBehavior = NetworkBehavior.create()
        givenNetworkFailurePercentIs(networkBehavior, 100)

        val mockRetrofit = MockRetrofit.Builder(retrofit)
            .networkBehavior(networkBehavior)
            .build()
        return ApiServiceMock(mockRetrofit.create(SecureApiInterface::class.java))
    }
}
