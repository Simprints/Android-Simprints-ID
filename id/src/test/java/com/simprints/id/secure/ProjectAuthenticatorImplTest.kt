@file:Suppress("DEPRECATION")

package com.simprints.id.secure

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.safetynet.SafetyNetClient
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.consent.longconsent.LongConsentRepository
import com.simprints.id.data.db.project.remote.ProjectRemoteDataSource
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.RemoteConfigWrapper
import com.simprints.id.data.secure.SecureLocalDbKeyProvider
import com.simprints.id.secure.models.*
import com.simprints.id.secure.models.remote.ApiAuthenticationData
import com.simprints.id.secure.models.remote.ApiToken
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class ProjectAuthenticatorImplTest {

    @MockK private lateinit var projectRemoteDataSourceMock: ProjectRemoteDataSource
    @MockK private lateinit var longConsentRepositoryMock: LongConsentRepository
    @MockK private lateinit var secureDataManager: SecureLocalDbKeyProvider
    @MockK private lateinit var projectSecretManager: ProjectSecretManager
    @MockK private lateinit var signerManager: SignerManager
    @MockK private lateinit var remoteConfigWrapper: RemoteConfigWrapper
    @MockK private lateinit var preferencesManager: PreferencesManager
    @MockK private lateinit var safetyNetClient: SafetyNetClient
    @MockK private lateinit var authenticationDataManagerMock: AuthenticationDataManager
    @MockK private lateinit var attestationManagerMock: AttestationManager
    @MockK private lateinit var secureApiInterface: SecureApiInterface


    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        getMockAttestationManager()
    }

    @Test
    fun successfulResponse_userShouldSignIn() {
        runBlocking {
            coEvery { authenticationDataManagerMock.requestAuthenticationData(any(), any()) } returns AuthenticationData(Nonce(""), PublicKeyString(""))
            every { preferencesManager.projectLanguages} returns emptyArray()

            val authenticator = buildProjectAuthenticator()

            authenticator.authenticate(NonceScope(PROJECT_ID, USER_ID), PROJECT_SECRET)
        }
    }

    private fun buildProjectAuthenticator(): ProjectAuthenticatorImpl {
        return ProjectAuthenticatorImpl(
            secureApiInterface,
            projectSecretManager,
            safetyNetClient,
            secureDataManager,
            projectRemoteDataSourceMock,
            signerManager,
            remoteConfigWrapper,
            longConsentRepositoryMock,
            preferencesManager,
            attestationManagerMock,
            authenticationDataManagerMock
        )
    }

//    @Test(expected = IOException::class)
//    fun offline_authenticationShouldThrowException() {
//        runBlocking {
//            val nonceScope = NonceScope(PROJECT_ID, USER_ID)
//            val mockService = createMockServiceToFailRequests(apiClient.retrofit)
//            println("ProjectAuthenticator: Authenticator $mockService")
//            val authenticator = ProjectAuthenticatorImpl(
//                mockService,
//                loginInfoManager,
//                safetyNetClient,
//                secureDataManager,
//                projectRemoteDataSourceMock,
//                signerManager,
//                remoteConfigWrapper,
//                longConsentRepositoryMock,
//                preferencesManager
//            )
//
//            authenticator.authenticate(nonceScope, PROJECT_SECRET)
//        }
//    }
//
//    @Test
//    fun getAuthenticationData_invokeAuthenticationDataManagerCorrectly() {
//        runBlocking {
//            val mockAuthenticationDataManager = mockk<AuthenticationDataManager>(relaxed = true)
//
//            val authenticator = ProjectAuthenticatorImpl(
//                mockk(),
//                loginInfoManager,
//                mockk(),
//                secureDataManager,
//                projectRemoteDataSourceMock,
//                signerManager,
//                remoteConfigWrapper,
//                longConsentRepositoryMock,
//                preferencesManager,
//                authenticationDataManager = mockAuthenticationDataManager
//            )
//            authenticator.getAuthenticationData(PROJECT_ID, USER_ID)
//
//            coVerify(exactly = 1) { mockAuthenticationDataManager.requestAuthenticationData(PROJECT_ID, USER_ID) }
//        }
//    }
//
//    @Test
//    fun authenticate_shouldAuthenticateWithRightRequestsToSecureApiInterface() {
//        runBlocking {
//            val mockWebServer = MockWebServer()
//            val mockProjectSecretManager: ProjectSecretManager = mockk()
//
//            every {
//                mockProjectSecretManager.encryptAndStoreAndReturnProjectSecret(any(), any())
//            } returns PROJECT_SECRET
//
//            with(mockWebServer) {
//                enqueue(mockResponseForAuthenticationData())
//                enqueue(mockResponseForApiToken())
//            }
//
//            val authenticatorSpy = spyk(ProjectAuthenticatorImpl(
//                SimApiClientFactory("deviceId", mockWebServer.url("/").toString()).build<SecureApiInterface>().api,
//                loginInfoManager,
//                safetyNetClient,
//                secureDataManager,
//                projectRemoteDataSourceMock,
//                signerManager,
//                remoteConfigWrapper,
//                longConsentRepositoryMock,
//                preferencesManager,
//                getMockAttestationManager()
//            ))
//
//            every { authenticatorSpy.projectSecretManager } returns mockProjectSecretManager
//
//            authenticatorSpy.authenticate(NonceScope(PROJECT_ID, USER_ID), PROJECT_SECRET)
//
//            assertThat(mockWebServer.requestCount).isEqualTo(2)
//        }
//    }
//
//    @Test(expected = SafetyNetException::class)
//    fun safetyNetFailed_shouldThrowRightException() {
//        runBlocking {
//            val mockAttestationManager = mockk<AttestationManager>()
//            val nonceScope = NonceScope(PROJECT_ID, USER_ID)
//
//            every {
//                mockAttestationManager.requestAttestation(any(), any())
//            } throws (SafetyNetException(reason = SafetyNetExceptionReason.SERVICE_UNAVAILABLE))
//
//            val authenticator = ProjectAuthenticatorImpl(
//                SecureApiServiceMock(createMockBehaviorService(apiClient.retrofit, 0, SecureApiInterface::class.java)),
//                loginInfoManager,
//                safetyNetClient,
//                secureDataManager,
//                projectRemoteDataSourceMock,
//                signerManager,
//                remoteConfigWrapper,
//                longConsentRepositoryMock,
//                preferencesManager,
//                mockAttestationManager
//            )
//
//            authenticator.authenticate(nonceScope, PROJECT_SECRET)
//        }
//    }

    private fun getMockAttestationManager(): AttestationManager {
        every {
            attestationManagerMock.requestAttestation(any(), any())
        } returns AttestToken("google_attestation")

        return attestationManagerMock
    }

    private fun mockResponseForAuthenticationData() = with(MockResponse()) {
        setResponseCode(200)
        setBody(JsonHelper.toJson(ApiAuthenticationData("nonce", "publicKeyString")))
    }

    private fun mockResponseForApiToken() = with(MockResponse()) {
        setResponseCode(200)
        setBody(JsonHelper.toJson(ApiToken("firebaseCustomToken")))
    }

    private companion object {
        const val PROJECT_ID = "project_id"
        const val USER_ID = "user_id"
        const val PROJECT_SECRET = "encrypted_project_secret"
    }

}
