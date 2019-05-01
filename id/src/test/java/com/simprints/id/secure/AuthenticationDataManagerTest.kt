package com.simprints.id.secure

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.core.network.SimApiClient
import com.simprints.id.exceptions.safe.data.db.SimprintsInternalServerException
import com.simprints.id.secure.models.AuthenticationData
import com.simprints.id.secure.models.Nonce
import com.simprints.id.secure.models.PublicKeyString
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.common.retrofit.FakeResponseInterceptor
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class AuthenticationDataManagerTest {

    private val nonceFromServer = "nonce_from_server"
    private val publicKeyFromServer = "public_key_from_server"
    private val validAuthenticationJsonResponse = "{\"nonce\":\"$nonceFromServer\", \"publicKey\":\"$publicKeyFromServer\"}"
    private val expectedAuthenticationData = AuthenticationData(Nonce(nonceFromServer), PublicKeyString(publicKeyFromServer))

    private lateinit var apiClient: SimApiClient<SecureApiInterface>

    @Before
    fun setUp() {
        apiClient = SimApiClient(SecureApiInterface::class.java, SecureApiInterface.baseUrl)
    }

    @Test
    fun successfulResponse_shouldObtainValidAuthenticationData() {
        UnitTestConfig(this).rescheduleRxMainThread()

        forceOkHttpToReturnSuccessfulResponse(apiClient.okHttpClientConfig)

        val testObserver = makeTestRequestForAuthenticationData(apiClient.api)
        testObserver.awaitTerminalEvent()

        testObserver.assertNoErrors()
            .assertValue {
                it == expectedAuthenticationData
            }
    }

    @Test
    fun offline_shouldThrowAnException() {
        val apiServiceMock = createMockServiceToFailRequests(apiClient.retrofit)

        val testObserver = makeTestRequestForAuthenticationData(apiServiceMock)
        testObserver.awaitTerminalEvent()

        testObserver.assertError(IOException::class.java)
    }

    @Test
    fun receivingAnErrorFromServer_shouldThrowAnException() {
        apiClient.okHttpClientConfig.addInterceptor(FakeResponseInterceptor(500))

        val testObserver = makeTestRequestForAuthenticationData(apiClient.api)
        testObserver.awaitTerminalEvent()

        testObserver.assertError(SimprintsInternalServerException::class.java)
    }

    private fun makeTestRequestForAuthenticationData(secureApiInterfaceMock: SecureApiInterface) =
        AuthenticationDataManager(secureApiInterfaceMock).requestAuthenticationData("projectId", "userId")
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .test()

    private fun forceOkHttpToReturnSuccessfulResponse(okHttpClientConfig: OkHttpClient.Builder) {
        okHttpClientConfig.addInterceptor(FakeResponseInterceptor(200, validAuthenticationJsonResponse))
    }
}
