package com.simprints.id.secure.securitystate.remote

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.secure.SecureApiInterface
import com.simprints.id.secure.models.SecurityState
import com.simprints.id.secure.models.SyncEnrolmentRecord
import com.simprints.id.secure.models.remote.ApiSecurityState
import com.simprints.id.secure.models.remote.ApiSyncEnrolmentRecord
import com.simprints.infra.login.LoginManager
import com.simprints.infra.network.SimNetwork
import com.simprints.infra.network.exceptions.BackendMaintenanceException
import com.simprints.infra.network.exceptions.SyncCloudIntegrationException
import com.simprints.testtools.common.alias.InterfaceInvocation
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SecurityStateRemoteDataSourceImplTest {

    companion object {
        private const val PROJECT_ID = "projectId"
        private const val DEVICE_ID = "deviceId"
        private const val PREVIOUS_INSTRUCTION_ID = "id"
    }

    private val loginManager = mockk<LoginManager>()
    private val remoteInterface = mockk<SecureApiInterface>()
    private val settingsPreferencesManager = mockk<SettingsPreferencesManager> {
        every { lastInstructionId } returns PREVIOUS_INSTRUCTION_ID
    }
    private val simApiClient = mockk<SimNetwork.SimApiClient<SecureApiInterface>>()
    private val securityStateRemoteDataSource =
        SecurityStateRemoteDataSourceImpl(loginManager, settingsPreferencesManager, DEVICE_ID)


    @Before
    fun setUp() {
        coEvery { simApiClient.executeCall<ApiSecurityState>(any()) } coAnswers {
            val args = this.args
            @Suppress("UNCHECKED_CAST")
            (args[0] as InterfaceInvocation<SecureApiInterface, ApiSecurityState>).invoke(
                remoteInterface
            )
        }
        coEvery { loginManager.buildClient(SecureApiInterface::class) } returns simApiClient
        every { loginManager.getSignedInProjectIdOrEmpty() } returns PROJECT_ID
    }

    @Test
    fun `Get successful security state`() = runTest(StandardTestDispatcher()) {
        coEvery {
            remoteInterface.requestSecurityState(
                PROJECT_ID,
                DEVICE_ID,
                PREVIOUS_INSTRUCTION_ID,
            )
        } returns ApiSecurityState(DEVICE_ID, ApiSecurityState.Status.PROJECT_ENDED, ApiSyncEnrolmentRecord("id1", listOf("subject1")))

        val securityState = securityStateRemoteDataSource.getSecurityState()

        assertThat(securityState).isEqualTo(
            SecurityState(
                DEVICE_ID,
                SecurityState.Status.PROJECT_ENDED,
                SyncEnrolmentRecord("id1", listOf("subject1"))
            )
        )
    }

    @Test
    fun `Get no security state if backend maintenance exception`() =
        runTest(StandardTestDispatcher()) {
            val exception = BackendMaintenanceException(estimatedOutage = 100)
            coEvery {
                remoteInterface.requestSecurityState(
                    PROJECT_ID,
                    DEVICE_ID,
                    PREVIOUS_INSTRUCTION_ID,
                )
            } throws exception

            val receivedException = assertThrows<BackendMaintenanceException> {
                securityStateRemoteDataSource.getSecurityState()
            }
            assertThat(receivedException).isEqualTo(exception)
        }

    @Test
    fun `Get no security state if sync cloud integration exception`() =
        runTest(StandardTestDispatcher()) {
            val exception = SyncCloudIntegrationException(cause = Exception())
            coEvery {
                remoteInterface.requestSecurityState(
                    PROJECT_ID,
                    DEVICE_ID,
                    PREVIOUS_INSTRUCTION_ID,
                )
            } throws exception

            val receivedException = assertThrows<SyncCloudIntegrationException> {
                securityStateRemoteDataSource.getSecurityState()
            }
            assertThat(receivedException).isEqualTo(exception)
        }
}
