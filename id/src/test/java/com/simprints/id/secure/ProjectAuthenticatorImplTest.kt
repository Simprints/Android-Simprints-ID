package com.simprints.id.secure

import com.google.android.gms.safetynet.SafetyNetClient
import com.simprints.core.security.SecureLocalDbKeyProvider
import com.simprints.core.tools.utils.LanguageHelper
import com.simprints.id.data.consent.longconsent.LongConsentRepository
import com.simprints.id.data.db.project.remote.ProjectRemoteDataSource
import com.simprints.id.data.prefs.IdPreferencesManager
import com.simprints.id.data.prefs.RemoteConfigWrapper
import com.simprints.id.exceptions.safe.secure.SafetyNetException
import com.simprints.id.exceptions.safe.secure.SafetyNetExceptionReason
import com.simprints.id.secure.models.*
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import java.io.IOException

@ExperimentalCoroutinesApi
class ProjectAuthenticatorImplTest {

    @MockK private lateinit var projectRemoteDataSourceMock: ProjectRemoteDataSource
    @MockK private lateinit var longConsentRepositoryMock: LongConsentRepository
    @MockK private lateinit var secureDataManager: SecureLocalDbKeyProvider
    @MockK private lateinit var projectSecretManager: ProjectSecretManager
    @MockK private lateinit var signerManager: SignerManager
    @MockK private lateinit var remoteConfigWrapper: RemoteConfigWrapper
    @MockK private lateinit var preferencesManagerMock: IdPreferencesManager
    @MockK private lateinit var safetyNetClient: SafetyNetClient
    @MockK private lateinit var authenticationDataManagerMock: AuthenticationDataManager
    @MockK private lateinit var attestationManagerMock: AttestationManager
    @MockK private lateinit var authManagerMock: AuthManager

    private lateinit var authenticator: ProjectAuthenticator
    
    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockManagers()

        authenticator = buildProjectAuthenticator()
    }

    @Test
    fun successfulResponse_userShouldSignIn() = runBlockingTest {

        authenticator.authenticate(NonceScope(PROJECT_ID, USER_ID), PROJECT_SECRET, DEVICE_ID)
    }

    @Test
    fun offline_authenticationShouldThrowException() = runBlockingTest {
        coEvery { authManagerMock.requestAuthToken(any()) } throws IOException()

        assertThrows<IOException> {
            authenticator.authenticate(NonceScope(PROJECT_ID, USER_ID), PROJECT_SECRET, DEVICE_ID)
        }
    }

    @Test
    fun authenticate_invokeAuthenticationDataManagerCorrectly() = runBlockingTest {

        authenticator.authenticate(NonceScope(PROJECT_ID, USER_ID), PROJECT_SECRET, DEVICE_ID)

        coVerify(exactly = 1) { authenticationDataManagerMock.requestAuthenticationData(PROJECT_ID, USER_ID) }
    }

    @Test
    fun authenticate_invokeSignerManagerCorrectly() = runBlockingTest {

        authenticator.authenticate(NonceScope(PROJECT_ID, USER_ID), PROJECT_SECRET, DEVICE_ID)

        coVerify(exactly = 1) { signerManager.signIn(PROJECT_ID, USER_ID, any()) }
    }

    @Test
    fun authenticate_invokeSecureDataManagerCorrectly() = runBlockingTest {

        authenticator.authenticate(NonceScope(PROJECT_ID, USER_ID), PROJECT_SECRET, DEVICE_ID)

        coVerify(exactly = 1) { secureDataManager.setLocalDatabaseKey(PROJECT_ID) }
    }


    @Test
    fun safetyNetFailed_shouldThrowRightException() = runBlockingTest {
        every { attestationManagerMock.requestAttestation(any(), any()) } throws SafetyNetException("", SafetyNetExceptionReason.SERVICE_UNAVAILABLE)

        assertThrows<SafetyNetException> {
            authenticator.authenticate(NonceScope(PROJECT_ID, USER_ID), PROJECT_SECRET, DEVICE_ID)
        }
    }

    private fun buildProjectAuthenticator(): ProjectAuthenticatorImpl {
        return ProjectAuthenticatorImpl(
            authManagerMock,
            projectSecretManager,
            safetyNetClient,
            secureDataManager,
            projectRemoteDataSourceMock,
            signerManager,
            remoteConfigWrapper,
            longConsentRepositoryMock,
            preferencesManagerMock,
            attestationManagerMock,
            authenticationDataManagerMock
        )
    }

    private fun mockManagers() {
        coEvery { authenticationDataManagerMock.requestAuthenticationData(any(), any()) } returns AuthenticationData(Nonce(""), PublicKeyString(""))
        every { preferencesManagerMock.projectLanguages } returns emptyArray()
        coEvery { authManagerMock.requestAuthToken(any()) } returns Token("")
        coEvery { projectRemoteDataSourceMock.loadProjectRemoteConfigSettingsJsonString(any()) } returns mockk()
        every { preferencesManagerMock.projectLanguages } returns emptyArray()
        every { attestationManagerMock.requestAttestation(any(), any()) } returns AttestToken("google_attestation")
        LanguageHelper.prefs = mockk(relaxed = true)
    }

    private companion object {
        const val PROJECT_ID = "project_id"
        const val USER_ID = "user_id"
        const val PROJECT_SECRET = "encrypted_project_secret"
        const val DEVICE_ID = "device_id"
    }

}
