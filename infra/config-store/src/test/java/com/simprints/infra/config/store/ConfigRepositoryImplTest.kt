package com.simprints.infra.config.store

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.ConfigRepositoryImpl.Companion.PRIVACY_NOTICE_FILE
import com.simprints.infra.config.store.local.ConfigLocalDataSource
import com.simprints.infra.config.store.models.DeviceConfiguration
import com.simprints.infra.config.store.models.PrivacyNoticeResult.Failed
import com.simprints.infra.config.store.models.PrivacyNoticeResult.FailedBecauseBackendMaintenance
import com.simprints.infra.config.store.models.PrivacyNoticeResult.InProgress
import com.simprints.infra.config.store.models.PrivacyNoticeResult.Succeed
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.ProjectState
import com.simprints.infra.config.store.models.ProjectWithConfig
import com.simprints.infra.config.store.remote.ConfigRemoteDataSource
import com.simprints.infra.config.store.testtools.deviceConfiguration
import com.simprints.infra.config.store.testtools.deviceState
import com.simprints.infra.config.store.testtools.project
import com.simprints.infra.config.store.testtools.projectConfiguration
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.network.SimNetwork
import com.simprints.infra.network.exceptions.BackendMaintenanceException
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ConfigRepositoryImplTest {
    companion object {
        private const val PROJECT_ID = "projectId"
        private const val DEVICE_ID = "deviceId"
        private const val LANGUAGE = "fr"
        private const val PRIVACY_NOTICE = "privacy notice"
    }

    private val localDataSource = mockk<ConfigLocalDataSource>(relaxed = true)
    private val remoteDataSource = mockk<ConfigRemoteDataSource>()
    private val simNetwork = mockk<SimNetwork>(relaxed = true)
    private val tokenizationProcessor = mockk<TokenizationProcessor>(relaxed = true)

    private lateinit var configServiceImpl: ConfigRepositoryImpl

    @Before
    fun setup() {
        configServiceImpl = ConfigRepositoryImpl(
            localDataSource,
            remoteDataSource,
            simNetwork,
            tokenizationProcessor,
            DEVICE_ID,
        )
    }

    @Test
    fun `should get the project locally if available`() = runTest {
        coEvery { localDataSource.getProject() } returns project

        val receivedProject = configServiceImpl.getProject()

        assertThat(receivedProject).isEqualTo(project)
        coVerify(exactly = 1) { localDataSource.getProject() }
        coVerify(exactly = 0) { remoteDataSource.getProject(any()) }
    }

    @Test
    fun `should throw the exception if there is an issue`() = runTest {
        val exception = Exception("exception")
        coEvery { localDataSource.getProject() } throws exception

        val receivedException = assertThrows<Exception> { configServiceImpl.getProject() }

        assertThat(receivedException).isEqualTo(exception)
        coVerify(exactly = 1) { localDataSource.getProject() }
        coVerify(exactly = 0) { localDataSource.saveProject(project) }
        coVerify(exactly = 0) { remoteDataSource.getProject(PROJECT_ID) }
    }

    @Test
    fun `refreshProject() should get the project remotely and save it and update the api base url if not empty`() = runTest {
        coEvery { localDataSource.saveProject(project) } returns Unit
        coEvery { remoteDataSource.getProject(PROJECT_ID) } returns ProjectWithConfig(project, projectConfiguration)

        configServiceImpl.refreshProject(PROJECT_ID)
        coVerify(exactly = 1) { localDataSource.saveProject(project) }
        coVerify(exactly = 1) { localDataSource.saveProjectConfiguration(projectConfiguration) }
        coVerify(exactly = 1) { remoteDataSource.getProject(PROJECT_ID) }
        coVerify(exactly = 1) { simNetwork.setApiBaseUrl(project.baseUrl) }
    }

    @Test
    fun `refreshProject() should get the project remotely and save it and not update the api base url if empty`() = runTest {
        val project = Project(
            "id",
            "name",
            ProjectState.RUNNING,
            "description",
            "creator",
            "url",
            "",
            tokenizationKeys = emptyMap(),
        )
        coEvery { localDataSource.saveProject(project) } returns Unit
        coEvery { remoteDataSource.getProject(PROJECT_ID) } returns ProjectWithConfig(project, projectConfiguration)

        configServiceImpl.refreshProject(PROJECT_ID)
        coVerify(exactly = 1) { localDataSource.saveProject(project) }
        coVerify(exactly = 1) { remoteDataSource.getProject(PROJECT_ID) }
        coVerify(exactly = 0) { simNetwork.setApiBaseUrl(project.baseUrl) }
    }

    @Test
    fun `getDeviceState should call the correct method`() = runTest {
        coEvery { localDataSource.getProject() } returns project
        coEvery { localDataSource.getDeviceConfiguration() } returns deviceConfiguration
        coEvery { remoteDataSource.getDeviceState(any(), any(), any()) } returns deviceState

        val result = configServiceImpl.getDeviceState()
        assertThat(result).isEqualTo(deviceState)
    }

    @Test
    fun `getDeviceConfiguration should call the correct method`() = runTest {
        coEvery { localDataSource.getDeviceConfiguration() } returns deviceConfiguration

        val gottenDeviceConfiguration = configServiceImpl.getDeviceConfiguration()
        assertThat(gottenDeviceConfiguration).isEqualTo(deviceConfiguration)
    }

    @Test
    fun `updateDeviceConfiguration should call the correct method`() = runTest {
        val update: (c: DeviceConfiguration) -> DeviceConfiguration = {
            it
        }

        configServiceImpl.updateDeviceConfiguration(update)
        coVerify(exactly = 1) { localDataSource.updateDeviceConfiguration(update) }
    }

    @Test
    fun `should return the privacy notice correctly if it has been cached`() = runTest {
        every { localDataSource.hasPrivacyNoticeFor(PROJECT_ID, LANGUAGE) } returns true
        every { localDataSource.getPrivacyNotice(PROJECT_ID, LANGUAGE) } returns PRIVACY_NOTICE

        val results = configServiceImpl.getPrivacyNotice(PROJECT_ID, LANGUAGE).toList()
        assertThat(results).isEqualTo(listOf(Succeed(LANGUAGE, PRIVACY_NOTICE)))
    }

    @Test
    fun `should download the privacy notice correctly if it has not been cached`() = runTest {
        every { localDataSource.hasPrivacyNoticeFor(PROJECT_ID, LANGUAGE) } returns false
        coEvery {
            remoteDataSource.getPrivacyNotice(
                PROJECT_ID,
                "${PRIVACY_NOTICE_FILE}_$LANGUAGE",
            )
        } returns PRIVACY_NOTICE

        val results = configServiceImpl.getPrivacyNotice(PROJECT_ID, LANGUAGE).toList()

        assertThat(results).isEqualTo(
            listOf(
                InProgress(LANGUAGE),
                Succeed(LANGUAGE, PRIVACY_NOTICE),
            ),
        )
        verify(exactly = 1) {
            localDataSource.storePrivacyNotice(
                PROJECT_ID,
                LANGUAGE,
                PRIVACY_NOTICE,
            )
        }
    }

    @Test
    fun `should return a FailedBecauseBackendMaintenance if it fails to download the privacy notice with a BackendMaintenanceException`() =
        runTest {
            val exception = BackendMaintenanceException(estimatedOutage = 10)
            every { localDataSource.hasPrivacyNoticeFor(PROJECT_ID, LANGUAGE) } returns false
            coEvery {
                remoteDataSource.getPrivacyNotice(
                    PROJECT_ID,
                    "${PRIVACY_NOTICE_FILE}_$LANGUAGE",
                )
            } throws exception

            val results = configServiceImpl.getPrivacyNotice(PROJECT_ID, LANGUAGE).toList()

            assertThat(results).isEqualTo(
                listOf(
                    InProgress(LANGUAGE),
                    FailedBecauseBackendMaintenance(LANGUAGE, exception, 10),
                ),
            )
            verify(exactly = 0) { localDataSource.storePrivacyNotice(PROJECT_ID, LANGUAGE, any()) }
        }

    @Test
    fun `should return a Failed if it fails to download the privacy notice with another exception`() = runTest {
        val exception = Exception()
        every { localDataSource.hasPrivacyNoticeFor(PROJECT_ID, LANGUAGE) } returns false
        coEvery {
            remoteDataSource.getPrivacyNotice(
                PROJECT_ID,
                "${PRIVACY_NOTICE_FILE}_$LANGUAGE",
            )
        } throws exception

        val results = configServiceImpl.getPrivacyNotice(PROJECT_ID, LANGUAGE).toList()

        assertThat(results).isEqualTo(
            listOf(
                InProgress(LANGUAGE),
                Failed(LANGUAGE, exception),
            ),
        )
        verify(exactly = 0) { localDataSource.storePrivacyNotice(PROJECT_ID, LANGUAGE, any()) }
    }

    @Test
    fun `clearData should clear all the data`() = runTest {
        configServiceImpl.clearData()

        coVerify(exactly = 1) { localDataSource.clearProject() }
        coVerify(exactly = 1) { localDataSource.clearProjectConfiguration() }
        coVerify(exactly = 1) { localDataSource.clearDeviceConfiguration() }
        verify(exactly = 1) { localDataSource.deletePrivacyNotices() }
    }

    @Test
    fun `watchProjectConfiguration should emit values from the local data source`() = runTest {
        val config1 = projectConfiguration.copy(projectId = "project1")
        val config2 = projectConfiguration.copy(projectId = "project2")

        coEvery { localDataSource.watchProjectConfiguration() } returns kotlinx.coroutines.flow.flow {
            emit(config1)
            emit(config2)
        }

        val emittedConfigs = configServiceImpl.watchProjectConfiguration().toList()

        assertThat(emittedConfigs).hasSize(2)
        assertThat(emittedConfigs[0]).isEqualTo(config1)
        assertThat(emittedConfigs[1]).isEqualTo(config2)
        coVerify(exactly = 0) { remoteDataSource.getProject(any()) }
    }

    @Test
    fun `should return project config from local data source`() = runTest {
        val config = projectConfiguration.copy(projectId = "project1")
        coEvery { localDataSource.getProjectConfiguration() } returns config

        val result = configServiceImpl.getProjectConfiguration()

        assertThat(result).isEqualTo(config)
        coVerify { localDataSource.getProjectConfiguration() }
    }
}
