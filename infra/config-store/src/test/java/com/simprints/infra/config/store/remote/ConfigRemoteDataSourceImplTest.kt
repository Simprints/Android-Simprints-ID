package com.simprints.infra.config.store.remote

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.remote.models.ApiFileUrl
import com.simprints.infra.config.store.testtools.apiDeviceState
import com.simprints.infra.config.store.testtools.apiProject
import com.simprints.infra.config.store.testtools.deviceState
import com.simprints.infra.config.store.testtools.project
import com.simprints.infra.config.store.testtools.projectConfiguration
import com.simprints.infra.network.SimNetwork
import com.simprints.infra.network.exceptions.BackendMaintenanceException
import com.simprints.infra.network.exceptions.SyncCloudIntegrationException
import com.simprints.testtools.common.alias.InterfaceInvocation
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ConfigRemoteDataSourceImplTest {
    companion object {
        private const val PROJECT_ID = "projectId"
        private const val FILE_ID = "fileId"
        private const val DEVICE_ID = "deviceId"
        private const val URL = "url"
        private const val PRIVACY_NOTICE = "privacy notice"
    }

    private val remoteInterface = mockk<ConfigRemoteInterface>()
    private val simApiClient = mockk<SimNetwork.SimApiClient<ConfigRemoteInterface>>()
    private val authStore = mockk<AuthStore>()
    private val privacyNoticeDownloader = mockk<(String) -> String>()
    private val configRemoteDataSourceImpl =
        ConfigRemoteDataSourceImpl(authStore, UnconfinedTestDispatcher(), privacyNoticeDownloader)

    @Before
    fun setup() {
        coEvery { authStore.buildClient<ConfigRemoteInterface>(any()) } returns simApiClient
        coEvery { simApiClient.executeCall<Any>(any()) } coAnswers {
            val args = this.args
            @Suppress("UNCHECKED_CAST")
            (args[0] as InterfaceInvocation<ConfigRemoteInterface, Any>).invoke(
                remoteInterface,
            )
        }
    }

    @Test
    fun `Get successful project`() = runTest(StandardTestDispatcher()) {
        coEvery { remoteInterface.getProject(PROJECT_ID) } returns apiProject

        val receivedProject = configRemoteDataSourceImpl.getProject(PROJECT_ID)

        assertThat(receivedProject.project).isEqualTo(project)
        assertThat(receivedProject.configuration).isEqualTo(projectConfiguration)
    }

    @Test
    fun `Get no project if backend maintenance exception`() = runTest(StandardTestDispatcher()) {
        val exception = BackendMaintenanceException(estimatedOutage = 100)
        coEvery { remoteInterface.getProject(PROJECT_ID) } throws exception

        val receivedException = assertThrows<BackendMaintenanceException> {
            configRemoteDataSourceImpl.getProject(PROJECT_ID)
        }
        assertThat(receivedException).isEqualTo(exception)
    }

    @Test
    fun `Get no project if sync cloud integration exception`() = runTest(StandardTestDispatcher()) {
        val exception = SyncCloudIntegrationException(cause = Exception())
        coEvery { remoteInterface.getProject(PROJECT_ID) } throws exception

        val receivedException = assertThrows<SyncCloudIntegrationException> {
            configRemoteDataSourceImpl.getProject(PROJECT_ID)
        }
        assertThat(receivedException).isEqualTo(exception)
    }

    @Test
    fun `Get successful privacy notice`() = runTest(StandardTestDispatcher()) {
        coEvery { remoteInterface.getFileUrl(PROJECT_ID, FILE_ID) } returns ApiFileUrl(URL)
        every { privacyNoticeDownloader(URL) } returns PRIVACY_NOTICE

        val receivedPrivacyNotice =
            configRemoteDataSourceImpl.getPrivacyNotice(PROJECT_ID, FILE_ID)

        assertThat(receivedPrivacyNotice).isEqualTo(PRIVACY_NOTICE)
    }

    @Test
    fun `Get successful device state`() = runTest(StandardTestDispatcher()) {
        coEvery { remoteInterface.getDeviceState(any(), any(), any()) } returns apiDeviceState

        val receivedState =
            configRemoteDataSourceImpl.getDeviceState(PROJECT_ID, DEVICE_ID, "")
        assertThat(receivedState).isEqualTo(deviceState)
    }
}
