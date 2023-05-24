package com.simprints.id.secure

import com.google.android.play.core.integrity.model.IntegrityErrorCode
import com.simprints.id.secure.models.NonceScope
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.GeneralConfiguration
import com.simprints.infra.config.domain.models.ProjectConfiguration
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.authstore.domain.models.AuthenticationData
import com.simprints.infra.authstore.domain.models.Token
import com.simprints.infra.authstore.exceptions.RequestingIntegrityTokenException
import com.simprints.infra.network.exceptions.BackendMaintenanceException
import com.simprints.infra.security.SecurityManager
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.IOException

class ProjectAuthenticatorImplTest {

    @MockK
    private lateinit var configManager: ConfigManager

    @MockK
    private lateinit var secureDataManager: SecurityManager

    @MockK
    private lateinit var projectSecretManager: ProjectSecretManager

    @MockK
    private lateinit var signerManager: SignerManager

    @MockK
    private lateinit var authStore: AuthStore

    private lateinit var authenticator: ProjectAuthenticator

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockManagers()

        authenticator = buildProjectAuthenticator()
    }

    @Test
    fun successfulResponse_userShouldSignIn() = runTest(StandardTestDispatcher()) {

        authenticator.authenticate(NonceScope(PROJECT_ID, USER_ID), PROJECT_SECRET, DEVICE_ID)
    }

    @Test
    fun offline_authenticationShouldThrowException() = runTest(StandardTestDispatcher()) {
        coEvery {
            authStore.requestAuthToken(
                PROJECT_ID,
                USER_ID,
                any()
            )
        } throws IOException()

        assertThrows<IOException> {
            authenticator.authenticate(NonceScope(PROJECT_ID, USER_ID), PROJECT_SECRET, DEVICE_ID)
        }
    }

    @Test
    fun maintenance_authenticationShouldThrowMaintenanceException() =
        runTest(StandardTestDispatcher()) {
            coEvery {
                authStore.requestAuthToken(
                    PROJECT_ID,
                    USER_ID,
                    any()
                )
            } throws BackendMaintenanceException(
                estimatedOutage = null
            )

            assertThrows<BackendMaintenanceException> {
                authenticator.authenticate(
                    NonceScope(PROJECT_ID, USER_ID),
                    PROJECT_SECRET,
                    DEVICE_ID
                )
            }
        }

    @Test
    fun authenticate_invokeAuthenticationDataManagerCorrectly() =
        runTest(StandardTestDispatcher()) {

            authenticator.authenticate(NonceScope(PROJECT_ID, USER_ID), PROJECT_SECRET, DEVICE_ID)

            coVerify(exactly = 1) {
                authStore.requestAuthenticationData(
                    PROJECT_ID,
                    USER_ID,
                    DEVICE_ID,
                )
            }
        }

    @Test
    fun authenticate_invokeSignerManagerCorrectly() = runTest(StandardTestDispatcher()) {

        authenticator.authenticate(NonceScope(PROJECT_ID, USER_ID), PROJECT_SECRET, DEVICE_ID)

        coVerify(exactly = 1) { signerManager.signIn(PROJECT_ID, USER_ID, any()) }
    }

    @Test
    fun authenticate_invokeSecureDataManagerCorrectly() = runTest(StandardTestDispatcher()) {

        authenticator.authenticate(NonceScope(PROJECT_ID, USER_ID), PROJECT_SECRET, DEVICE_ID)

        coVerify(exactly = 1) { secureDataManager.createLocalDatabaseKeyIfMissing(PROJECT_ID) }
    }

    @Test
    fun `authenticate should fetch the correct long consents`() =
        runTest(StandardTestDispatcher()) {
            authenticator.authenticate(NonceScope(PROJECT_ID, USER_ID), PROJECT_SECRET, DEVICE_ID)

            coVerify(exactly = 1) { configManager.getPrivacyNotice(PROJECT_ID, LANGUAGE_1) }
            coVerify(exactly = 1) { configManager.getPrivacyNotice(PROJECT_ID, LANGUAGE_2) }
        }

    @Test
    fun integrityFailed_shouldThrowRightException() = runTest(StandardTestDispatcher()) {
        coEvery { authStore.requestIntegrityToken(any()) } throws RequestingIntegrityTokenException(
            IntegrityErrorCode.API_NOT_AVAILABLE
        )

        assertThrows<RequestingIntegrityTokenException> {
            authenticator.authenticate(NonceScope(PROJECT_ID, USER_ID), PROJECT_SECRET, DEVICE_ID)
        }
    }

    private fun buildProjectAuthenticator(): ProjectAuthenticatorImpl {
        return ProjectAuthenticatorImpl(
            authStore,
            projectSecretManager,
            secureDataManager,
            configManager,
            signerManager,
        )
    }

    private fun mockManagers() {
        coEvery {
            authStore.requestAuthenticationData(
                any(),
                any(),
                any()
            )
        } returns AuthenticationData(
            PUBLIC_KEY,
            ""
        )
        coEvery {
            authStore.requestAuthToken(
                PROJECT_ID,
                USER_ID,
                any()
            )
        } returns Token("", "", "", "")
        coEvery { configManager.refreshProjectConfiguration(PROJECT_ID) } returns ProjectConfiguration(
            PROJECT_ID,
            general = GeneralConfiguration(
                mockk(),
                languageOptions = listOf(LANGUAGE_1, LANGUAGE_2),
                defaultLanguage = LANGUAGE_1,
                mockk(),
                mockk(),
                mockk()
            ),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
        )
        coEvery { authStore.requestIntegrityToken(any()) } returns "token"
    }

    private companion object {
        private const val PUBLIC_KEY =
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCAmxhSp1nSNOkRianJtMEP6uEznURRKeLmnr5q/KJnMosVeSHCtFlsDeNrjaR9r90sUgn1oA++ixcu3h6sG4nq4BEgDHi0aHQnZrFNq+frd002ji5sb9dUM2n6M7z8PPjMNiy7xl//qDIbSuwMz9u5G1VjovE4Ej0E9x1HLmXHRQIDAQAB"
        private const val PROJECT_ID = "project_id"
        private const val USER_ID = "user_id"
        private const val PROJECT_SECRET = "encrypted_project_secret"
        private const val DEVICE_ID = "device_id"
        private const val LANGUAGE_1 = "en"
        private const val LANGUAGE_2 = "fr"
    }

}
