package com.simprints.id.secure

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.network.BaseUrlProvider
import com.simprints.core.network.NetworkConstants.Companion.DEFAULT_BASE_URL
import com.simprints.core.network.SimApiClient
import com.simprints.core.network.SimApiClientFactory
import com.simprints.id.exceptions.safe.data.db.SimprintsInternalServerException
import com.simprints.id.secure.SecureApiInterface.Companion.API_KEY
import com.simprints.id.secure.models.AuthenticationData
import com.simprints.id.secure.models.Nonce
import com.simprints.id.secure.models.PublicKeyString
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.common.retrofit.FakeResponseInterceptor
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class AuthenticationDataManagerImplTest: AutoCloseKoinTest() {

    companion object {
        private const val PROJECT_ID = "projectId"
        private const val USER_ID = "userId"
    }

    private val nonceFromServer = "nonce_from_server"
    private val publicKeyFromServer = "public_key_from_server"
    private val validAuthenticationJsonResponse = "{\"nonce\":\"$nonceFromServer\", \"publicKey\":\"$publicKeyFromServer\"}"
    private val expectedAuthenticationData = AuthenticationData(Nonce(nonceFromServer), PublicKeyString(publicKeyFromServer))
    private val expectedUrl = DEFAULT_BASE_URL + "projects/$PROJECT_ID/users/$USER_ID/authentication-data?key=$API_KEY"

    private val validateUrl: (url: String) -> Unit  = {
        assertThat(it).isEqualTo(expectedUrl)
    }

    private lateinit var apiClient: SimApiClient<SecureApiInterface>

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        UnitTestConfig(this).setupFirebase()

        val mockBaseUrlProvider = mockk<BaseUrlProvider>()
        every { mockBaseUrlProvider.getApiBaseUrl() } returns DEFAULT_BASE_URL
        apiClient = SimApiClientFactory(mockBaseUrlProvider, "deviceId").build()
    }

    @Test
    fun successfulResponse_shouldObtainValidAuthenticationData() {
        runBlocking {
            apiClient.okHttpClientConfig.addInterceptor(FakeResponseInterceptor(200, validAuthenticationJsonResponse, validateUrl = validateUrl))

            val actualAuthenticationData = makeTestRequestForAuthenticationData()

            assertThat(actualAuthenticationData).isEqualTo(expectedAuthenticationData)
        }
    }

    @Test
    fun receivingAnErrorFromServer_shouldThrowAnException() {
        runBlocking {
            apiClient.okHttpClientConfig.addInterceptor(FakeResponseInterceptor(500, validateUrl = validateUrl))

            assertThrows<SimprintsInternalServerException> {
                makeTestRequestForAuthenticationData()
            }
        }
    }

    private suspend fun makeTestRequestForAuthenticationData(): AuthenticationData {
        val authenticationDataManagerSpy = spyk(AuthenticationDataManagerImpl(mockk()))
        every { authenticationDataManagerSpy.apiClient } returns apiClient.api

        return authenticationDataManagerSpy.requestAuthenticationData(PROJECT_ID, USER_ID)
    }
}
