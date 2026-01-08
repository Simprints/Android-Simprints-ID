package com.simprints.infra.config.store

import com.google.common.truth.Truth.*
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.infra.authstore.AuthStore
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
import com.simprints.infra.logging.Simber
import com.simprints.infra.network.SimNetwork
import com.simprints.infra.network.exceptions.BackendMaintenanceException
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.net.HttpURLConnection

class ConfigRepositoryImplTest {
    @MockK
    private lateinit var authStore: AuthStore

    @MockK
    private lateinit var localDataSource: ConfigLocalDataSource

    @MockK
    private lateinit var remoteDataSource: ConfigRemoteDataSource

    @MockK
    private lateinit var simNetwork: SimNetwork

    @MockK
    private lateinit var tokenizationProcessor: TokenizationProcessor

    @MockK
    private lateinit var configSyncCache: ConfigSyncCache

    private lateinit var configRepository: ConfigRepositoryImpl

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        every { tokenizationProcessor.tokenizeIfNecessary(any(), any(), any()) } answers { firstArg() }

        mockkStatic(Simber::class)
        configRepository = ConfigRepositoryImpl(
            authStore = authStore,
            localDataSource = localDataSource,
            remoteDataSource = remoteDataSource,
            simNetwork = simNetwork,
            tokenizationProcessor = tokenizationProcessor,
            configSyncCache = configSyncCache,
            deviceId = DEVICE_ID,
        )
    }

    @After
    fun cleanup() {
        unmockkStatic(Simber::class)
    }

    @Test
    fun `getProject should get the project locally if available`() = runTest {
        coEvery { localDataSource.getProject() } returns project

        val result = configRepository.getProject()

        assertThat(result).isEqualTo(project)
        coVerify(exactly = 1) { localDataSource.getProject() }
        coVerify(exactly = 0) { remoteDataSource.getProject(any()) }
    }

    @Test
    fun `getProject should call the fetch method when cannot get local project`() = runTest {
        coEvery { localDataSource.getProject() } throws NoSuchElementException()
        every { authStore.signedInProjectId } returns PROJECT_ID
        coEvery { remoteDataSource.getProject(any()) } returns ProjectWithConfig(project, projectConfiguration)

        configRepository.getProject()
        coVerify(exactly = 1) { remoteDataSource.getProject(PROJECT_ID) }
    }

    @Test
    fun `getProject should returns null when no project ID`() = runTest {
        every { authStore.signedInProjectId } returns ""
        coEvery { localDataSource.getProject() } throws NoSuchElementException()

        val result = configRepository.getProject()
        assertThat(result).isNull()
        coVerify(exactly = 0) { localDataSource.saveProject(project) }
        coVerify(exactly = 0) { remoteDataSource.getProject(PROJECT_ID) }
    }

    @Test
    fun `getProject should returns null when fetch fails`() = runTest {
        every { authStore.signedInProjectId } returns PROJECT_ID
        coEvery { localDataSource.getProject() } throws NoSuchElementException()
        coEvery { remoteDataSource.getProject(any()) } throws NoSuchElementException()

        val result = configRepository.getProject()
        assertThat(result).isNull()
        coVerify(exactly = 0) { localDataSource.saveProject(project) }
    }

    @Test
    fun `refreshProject() should get the project remotely and save it and update the api base url if not empty`() = runTest {
        coEvery { localDataSource.saveProject(project) } returns Unit
        coEvery { remoteDataSource.getProject(PROJECT_ID) } returns ProjectWithConfig(project, projectConfiguration)

        configRepository.refreshProject(PROJECT_ID)
        coVerify(exactly = 1) { localDataSource.saveProject(project) }
        coVerify(exactly = 1) { localDataSource.saveProjectConfiguration(projectConfiguration) }
        coVerify(exactly = 1) { remoteDataSource.getProject(PROJECT_ID) }
        coVerify(exactly = 1) { simNetwork.setApiBaseUrl(project.baseUrl) }
        coVerify(exactly = 1) { configSyncCache.saveUpdateTime() }
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

        configRepository.refreshProject(PROJECT_ID)
        coVerify(exactly = 1) { localDataSource.saveProject(project) }
        coVerify(exactly = 1) { remoteDataSource.getProject(PROJECT_ID) }
        coVerify(exactly = 0) { simNetwork.setApiBaseUrl(project.baseUrl) }
    }

    @Test
    fun `observeIsProjectRefreshing should initially emit false`() = runTest {
        val isRefreshing = configRepository.observeIsProjectRefreshing().first()
        assertThat(isRefreshing).isFalse()
    }

    @Test
    fun `observeIsProjectRefreshing should emit false after refreshProject completes`() = runTest {
        coEvery { configRepository.refreshProject(PROJECT_ID) } returns ProjectWithConfig(project, projectConfiguration)

        configRepository.refreshProject(PROJECT_ID)
        val isRefreshing = configRepository.observeIsProjectRefreshing().first()
        assertThat(isRefreshing).isFalse()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `observeIsProjectRefreshing should emit true during refreshProject and false when done`() = runTest {
        coEvery { configRepository.refreshProject(PROJECT_ID) } coAnswers {
            delay(1000)
            ProjectWithConfig(project, projectConfiguration)
        }

        assertThat(configRepository.observeIsProjectRefreshing().first()).isFalse() // before

        launch { configRepository.refreshProject(PROJECT_ID) }
        advanceTimeBy(500)

        assertThat(configRepository.observeIsProjectRefreshing().first()).isTrue() // during

        advanceTimeBy(1000)

        assertThat(configRepository.observeIsProjectRefreshing().first()).isFalse() // after
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `observeIsProjectRefreshing should emit false even when refreshProject fails`() = runTest {
        coEvery { configRepository.refreshProject(PROJECT_ID) } coAnswers {
            delay(500)
            throw Exception("Test exception")
        }

        assertThat(configRepository.observeIsProjectRefreshing().first()).isFalse() // before

        launch {
            try {
                configRepository.refreshProject(PROJECT_ID)
            } catch (_: Exception) {
                // Expected
            }
        }
        advanceTimeBy(1000)

        assertThat(configRepository.observeIsProjectRefreshing().first()).isFalse() // after failure
    }

    @Test
    fun `getProjectConfiguration should get the config locally if available`() = runTest {
        coEvery { localDataSource.getProjectConfiguration() } returns projectConfiguration

        val result = configRepository.getProjectConfiguration()

        assertThat(result).isEqualTo(projectConfiguration)
        coVerify(exactly = 1) { localDataSource.getProject() }
        coVerify(exactly = 0) { remoteDataSource.getProject(any()) }
    }

    @Test
    fun `getProjectConfiguration should call the fetch method when default local configuration`() = runTest {
        coEvery { localDataSource.getProjectConfiguration() } returns projectConfiguration.copy(projectId = "")
        every { authStore.signedInProjectId } returns PROJECT_ID
        coEvery { remoteDataSource.getProject(any()) } returns ProjectWithConfig(project, projectConfiguration)

        configRepository.getProjectConfiguration()
        coVerify(exactly = 1) { remoteDataSource.getProject(PROJECT_ID) }
    }

    @Test
    fun `getProjectConfiguration should returns default configuration not logger in`() = runTest {
        val defaultConfiguration = projectConfiguration.copy(projectId = "")
        coEvery { localDataSource.getProjectConfiguration() } returns defaultConfiguration
        every { authStore.signedInProjectId } returns ""

        val result = configRepository.getProjectConfiguration()
        assertThat(result).isEqualTo(defaultConfiguration)
        coVerify(exactly = 0) { remoteDataSource.getProject(PROJECT_ID) }
    }

    @Test
    fun `getProjectConfiguration should returns null when fetch fails`() = runTest {
        every { authStore.signedInProjectId } returns PROJECT_ID
        val defaultConfiguration = projectConfiguration.copy(projectId = "")
        coEvery { localDataSource.getProjectConfiguration() } returns defaultConfiguration
        coEvery { remoteDataSource.getProject(any()) } throws NoSuchElementException()

        val result = configRepository.getProjectConfiguration()
        assertThat(result).isEqualTo(defaultConfiguration)
        coVerify(exactly = 0) { localDataSource.saveProject(project) }
    }

    @Test
    fun `getProjectConfiguration tokenizes modules before returning`() = runTest {
        coEvery { localDataSource.getProjectConfiguration() } returns projectConfiguration.copy(
            synchronization = projectConfiguration.synchronization.copy(
                down = projectConfiguration.synchronization.down.copy(
                    simprints = projectConfiguration.synchronization.down.simprints?.copy(
                        moduleOptions = listOf(
                            "module1".asTokenizableRaw(),
                            "module2".asTokenizableRaw(),
                        ),
                    ),
                ),
            ),
        )

        configRepository.getProjectConfiguration()
        coVerify(exactly = 2) { tokenizationProcessor.tokenizeIfNecessary(any(), any(), any()) }
    }

    @Test
    fun `observeProjectConfiguration should emit values from the local data source`() = runTest {
        val config1 = projectConfiguration.copy(projectId = "project1")
        val config2 = projectConfiguration.copy(projectId = "project2")

        coEvery { localDataSource.observeProjectConfiguration() } returns flow {
            emit(config1)
            emit(config2)
        }

        val emittedConfigs = configRepository.observeProjectConfiguration().toList()

        assertThat(emittedConfigs).hasSize(2)
        assertThat(emittedConfigs[0]).isEqualTo(config1)
        assertThat(emittedConfigs[1]).isEqualTo(config2)
        coVerify(exactly = 0) { remoteDataSource.getProject(any()) }
    }

    @Test
    fun `observeProjectConfiguration should call getProjectConfiguration on start to invoke download if config empty`() = runTest {
        coEvery { localDataSource.observeProjectConfiguration() } returns flow {
            emit(projectConfiguration)
        }

        val emittedConfigs = configRepository.observeProjectConfiguration().toList()

        coVerify(exactly = 1) { localDataSource.getProjectConfiguration() }

        assertThat(emittedConfigs).hasSize(1)
        assertThat(emittedConfigs[0]).isEqualTo(projectConfiguration)
    }

    @Test
    fun `getDeviceState should call the correct method`() = runTest {
        coEvery { localDataSource.getProject() } returns project
        coEvery { localDataSource.getDeviceConfiguration() } returns deviceConfiguration
        coEvery { remoteDataSource.getDeviceState(any(), any(), any()) } returns deviceState

        val result = configRepository.getDeviceState()
        assertThat(result).isEqualTo(deviceState)
    }

    @Test
    fun `getDeviceConfiguration should call the correct method`() = runTest {
        coEvery { localDataSource.getDeviceConfiguration() } returns deviceConfiguration

        val gottenDeviceConfiguration = configRepository.getDeviceConfiguration()
        assertThat(gottenDeviceConfiguration).isEqualTo(deviceConfiguration)
    }

    @Test
    fun `updateDeviceConfiguration should call the correct method`() = runTest {
        val update: (c: DeviceConfiguration) -> DeviceConfiguration = {
            it
        }

        configRepository.updateDeviceConfiguration(update)
        coVerify(exactly = 1) { localDataSource.updateDeviceConfiguration(update) }
    }

    @Test
    fun `should return the privacy notice correctly if it has been cached`() = runTest {
        every { localDataSource.hasPrivacyNoticeFor(PROJECT_ID, LANGUAGE) } returns true
        every { localDataSource.getPrivacyNotice(PROJECT_ID, LANGUAGE) } returns PRIVACY_NOTICE

        val results = configRepository.getPrivacyNotice(PROJECT_ID, LANGUAGE).toList()
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

        val results = configRepository.getPrivacyNotice(PROJECT_ID, LANGUAGE).toList()

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

            val results = configRepository.getPrivacyNotice(PROJECT_ID, LANGUAGE).toList()

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

        val results = configRepository.getPrivacyNotice(PROJECT_ID, LANGUAGE).toList()

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
        configRepository.clearData()

        coVerify(exactly = 1) { localDataSource.clearProject() }
        coVerify(exactly = 1) { localDataSource.clearProjectConfiguration() }
        coVerify(exactly = 1) { localDataSource.clearDeviceConfiguration() }
        verify(exactly = 1) { localDataSource.deletePrivacyNotices() }
    }

    @Test
    fun `should return project config from local data source`() = runTest {
        val config = projectConfiguration.copy(projectId = "project1")
        coEvery { localDataSource.getProjectConfiguration() } returns config

        val result = configRepository.getProjectConfiguration()

        assertThat(result).isEqualTo(config)
        coVerify { localDataSource.getProjectConfiguration() }
    }

    @Test
    fun `should log when failing to download privacy notice with non-404 response status code`() = runTest {
        val code = HttpURLConnection.HTTP_INTERNAL_ERROR // 500
        val exception = Exception("Server error").apply {
            initCause(HttpException(Response.error<String>(code, "".toResponseBody(null))))
        }
        every { localDataSource.hasPrivacyNoticeFor(PROJECT_ID, LANGUAGE) } returns false
        coEvery {
            remoteDataSource.getPrivacyNotice(
                PROJECT_ID,
                "${PRIVACY_NOTICE_FILE}_$LANGUAGE",
            )
        } throws exception

        configRepository.getPrivacyNotice(PROJECT_ID, LANGUAGE).toList()

        verify(exactly = 1) { Simber.i(eq("Failed to download privacy notice"), eq(exception)) }
    }

    @Test
    fun `should not log when failing to download privacy notice with 404 response status code`() = runTest {
        val code = HttpURLConnection.HTTP_NOT_FOUND
        val exception = Exception("Resource not found").apply {
            initCause(HttpException(Response.error<String>(code, "".toResponseBody(null))))
        }
        every { localDataSource.hasPrivacyNoticeFor(PROJECT_ID, LANGUAGE) } returns false
        coEvery {
            remoteDataSource.getPrivacyNotice(
                PROJECT_ID,
                "${PRIVACY_NOTICE_FILE}_$LANGUAGE",
            )
        } throws exception

        configRepository.getPrivacyNotice(PROJECT_ID, LANGUAGE).toList()

        verify(exactly = 0) { Simber.i(any(), any()) }
    }

    @Test
    fun `observeDeviceConfiguration should track values from the local data source`() = runTest {
        val config1 = deviceConfiguration.copy(selectedModules = emptyList())
        val config2 = deviceConfiguration.copy(
            selectedModules = listOf(
                "module1".asTokenizableEncrypted(),
                "module2".asTokenizableEncrypted(),
            ),
        )

        coEvery { localDataSource.observeDeviceConfiguration() } returns flow {
            emit(config1)
            emit(config2)
        }

        val emittedConfigs = configRepository.observeDeviceConfiguration().toList()

        assertThat(emittedConfigs).hasSize(2)
        assertThat(emittedConfigs[0]).isEqualTo(config1)
        assertThat(emittedConfigs[1]).isEqualTo(config2)
        coVerify(exactly = 1) { localDataSource.observeDeviceConfiguration() }
    }

    companion object {
        private const val PROJECT_ID = "projectId"
        private const val DEVICE_ID = "deviceId"
        private const val LANGUAGE = "fr"
        private const val PRIVACY_NOTICE = "privacy notice"
    }
}
