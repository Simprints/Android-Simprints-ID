package com.simprints.infra.authlogic.authenticator

import com.google.android.play.core.integrity.model.IntegrityErrorCode
import com.simprints.infra.authlogic.authenticator.remote.AuthenticationRemoteDataSource
import com.simprints.infra.authlogic.integrity.IntegrityTokenRequester
import com.simprints.infra.authlogic.integrity.exceptions.RequestingIntegrityTokenException
import com.simprints.infra.authlogic.model.NonceScope
import com.simprints.infra.authstore.domain.models.AuthenticationData
import com.simprints.infra.authstore.domain.models.Token
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.network.exceptions.BackendMaintenanceException
import com.simprints.infra.security.SecurityManager
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.IOException

class ProjectAuthenticatorTest {

    @MockK
    private lateinit var configRepository: ConfigRepository

    @MockK
    private lateinit var secureDataManager: SecurityManager

    @MockK
    private lateinit var projectSecretManager: ProjectSecretManager

    @MockK
    private lateinit var signerManager: SignerManager

    @MockK
    private lateinit var authenticationRemoteDataSource: AuthenticationRemoteDataSource

    @MockK
    private lateinit var integrityTokenRequester: IntegrityTokenRequester

    private lateinit var authenticator: ProjectAuthenticator

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        mockManagers()

        authenticator = ProjectAuthenticator(
            projectSecretManager,
            secureDataManager,
            configRepository,
            signerManager,
            authenticationRemoteDataSource,
            integrityTokenRequester,
        )
    }

    @Test
    fun successfulResponse_userShouldSignIn() = runTest {

        authenticator.authenticate(NonceScope(PROJECT_ID, DEVICE_ID), PROJECT_SECRET)
    }

    @Test
    fun offline_authenticationShouldThrowException() = runTest {
        coEvery {
            authenticationRemoteDataSource.requestAuthToken(
                PROJECT_ID,
                DEVICE_ID,
                any()
            )
        } throws IOException()

        assertThrows<IOException> {
            authenticator.authenticate(NonceScope(PROJECT_ID, DEVICE_ID), PROJECT_SECRET)
        }
    }

    @Test
    fun maintenance_authenticationShouldThrowMaintenanceException() = runTest {
        coEvery {
            authenticationRemoteDataSource.requestAuthToken(
                PROJECT_ID,
                DEVICE_ID,
                any()
            )
        } throws BackendMaintenanceException(
            estimatedOutage = null
        )

        assertThrows<BackendMaintenanceException> {
            authenticator.authenticate(
                NonceScope(PROJECT_ID, DEVICE_ID),
                PROJECT_SECRET,
            )
        }
    }

    @Test
    fun authenticate_invokeAuthenticationDataManagerCorrectly() = runTest {

        authenticator.authenticate(NonceScope(PROJECT_ID, DEVICE_ID), PROJECT_SECRET)

        coVerify(exactly = 1) {
            authenticationRemoteDataSource.requestAuthenticationData(
                PROJECT_ID,
                DEVICE_ID,
            )
        }
    }

    @Test
    fun authenticate_invokeSignerManagerCorrectly() = runTest(StandardTestDispatcher()) {

        authenticator.authenticate(NonceScope(PROJECT_ID, DEVICE_ID), PROJECT_SECRET)

        coVerify(exactly = 1) { signerManager.signIn(PROJECT_ID, any()) }
    }

    @Test
    fun authenticate_invokeSecureDataManagerCorrectly() = runTest(StandardTestDispatcher()) {

        authenticator.authenticate(NonceScope(PROJECT_ID, DEVICE_ID), PROJECT_SECRET)

        coVerify(exactly = 1) { secureDataManager.createLocalDatabaseKeyIfMissing(PROJECT_ID) }
    }

    @Test
    fun `authenticate should fetch the correct long consents`() =
        runTest(StandardTestDispatcher()) {
            authenticator.authenticate(NonceScope(PROJECT_ID, DEVICE_ID), PROJECT_SECRET)

            coVerify(exactly = 1) { configRepository.getPrivacyNotice(PROJECT_ID, LANGUAGE_1) }
            coVerify(exactly = 1) { configRepository.getPrivacyNotice(PROJECT_ID, LANGUAGE_2) }
        }

    @Test
    fun integrityFailed_shouldThrowRightException() = runTest(StandardTestDispatcher()) {
        coEvery { integrityTokenRequester.getToken(any()) } throws RequestingIntegrityTokenException(
            IntegrityErrorCode.API_NOT_AVAILABLE
        )

        assertThrows<RequestingIntegrityTokenException> {
            authenticator.authenticate(NonceScope(PROJECT_ID, DEVICE_ID), PROJECT_SECRET)
        }
    }

    private fun mockManagers() {
        every { projectSecretManager.encryptProjectSecret(any(), any()) } returns PROJECT_SECRET

        coEvery {
            authenticationRemoteDataSource.requestAuthenticationData(any(), any())
        } returns AuthenticationData(PUBLIC_KEY, "")

        coEvery {
            authenticationRemoteDataSource.requestAuthToken(PROJECT_ID, DEVICE_ID, any())
        } returns Token("", "", "", "")

        coEvery { configRepository.getProjectConfiguration() } returns ProjectConfiguration(
            PROJECT_ID,
            general = GeneralConfiguration(
                modalities = mockk(),
                languageOptions = listOf(LANGUAGE_1, LANGUAGE_2),
                defaultLanguage = LANGUAGE_1,
                collectLocation = false,
                duplicateBiometricEnrolmentCheck = false,
                settingsPassword = mockk()
            ),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
        )
        coEvery { configRepository.getPrivacyNotice(any(), any()) } returns emptyFlow()

        coEvery { integrityTokenRequester.getToken(any()) } returns "token"
    }

    private companion object {

        private const val PUBLIC_KEY =
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCAmxhSp1nSNOkRianJtMEP6uEznURRKeLmnr5q/KJnMosVeSHCtFlsDeNrjaR9r90sUgn1oA++ixcu3h6sG4nq4BEgDHi0aHQnZrFNq+frd002ji5sb9dUM2n6M7z8PPjMNiy7xl//qDIbSuwMz9u5G1VjovE4Ej0E9x1HLmXHRQIDAQAB"
        private const val PROJECT_ID = "project_id"
        private const val PROJECT_SECRET = "encrypted_project_secret"
        private const val DEVICE_ID = "device_id"
        private const val LANGUAGE_1 = "en"
        private const val LANGUAGE_2 = "fr"
    }

}
