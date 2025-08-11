package com.simprints.infra.config.sync

import com.google.common.truth.Truth.*
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.DeviceConfiguration
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.ProjectWithConfig
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.local.migration.RealmToRoomMigrationScheduler
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
import org.junit.Before
import org.junit.Test

class ConfigManagerTest {
    companion object {
        private const val PROJECT_ID = "projectId"
        private const val LANGUAGE = "fr"
    }

    private lateinit var configManager: ConfigManager

    @MockK
    private lateinit var configRepository: ConfigRepository

    @MockK
    private lateinit var enrolmentRecordRepository: EnrolmentRecordRepository

    @MockK
    private lateinit var configSyncCache: ConfigSyncCache

    @MockK
    private lateinit var projectWithConfig: ProjectWithConfig

    @MockK
    private lateinit var projectConfiguration: ProjectConfiguration

    @MockK
    private lateinit var deviceConfiguration: DeviceConfiguration

    @MockK
    private lateinit var realmToRoomMigrationScheduler: RealmToRoomMigrationScheduler

    @MockK
    private lateinit var project: Project

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        configManager = ConfigManager(
            configRepository = configRepository,
            enrolmentRecordRepository = enrolmentRecordRepository,
            configSyncCache = configSyncCache,
            realmToRoomMigrationScheduler = realmToRoomMigrationScheduler,
        )
    }

    @Test
    fun `refreshProject should call the correct method`() = runTest {
        coEvery { configRepository.refreshProject(PROJECT_ID) } returns projectWithConfig

        val refreshedProject = configManager.refreshProject(PROJECT_ID)
        assertThat(refreshedProject).isEqualTo(projectWithConfig)

        coVerify { configSyncCache.saveUpdateTime() }
        coVerify { realmToRoomMigrationScheduler.scheduleMigrationWorkerIfNeeded() }
    }

    @Test
    fun `getProject should call the correct method`() = runTest {
        coEvery { configRepository.getProject() } returns project

        val gottenProject = configManager.getProject(PROJECT_ID)
        assertThat(gottenProject).isEqualTo(project)
    }

    @Test
    fun `getProject should call the refresh method when cannot get from local`() = runTest {
        coEvery { configRepository.getProject() } throws NoSuchElementException()

        configManager.getProject(PROJECT_ID)
        coVerify(exactly = 1) { configRepository.refreshProject(PROJECT_ID) }
    }

    @Test
    fun `getProjectConfiguration should call the correct method`() = runTest {
        coEvery { configRepository.getProjectConfiguration() } returns projectConfiguration
        every { projectConfiguration.projectId } returns PROJECT_ID

        val gottenProjectConfiguration = configManager.getProjectConfiguration()
        assertThat(gottenProjectConfiguration).isEqualTo(projectConfiguration)
    }

    @Test
    fun `getProjectConfiguration return default config if not logged in`() = runTest {
        every { projectConfiguration.projectId } returns ""
        coEvery { configRepository.getProjectConfiguration() } returns projectConfiguration
        coEvery { configRepository.refreshProject(any()) } throws Exception()

        val gottenProjectConfiguration = configManager.getProjectConfiguration()
        assertThat(gottenProjectConfiguration).isEqualTo(projectConfiguration)
    }

    @Test
    fun `refreshProjectConfiguration should call the correct method`() = runTest {
        coEvery { configRepository.refreshProject(PROJECT_ID) } returns projectWithConfig

        val refreshedProjectConfiguration = configManager.refreshProject(PROJECT_ID)
        assertThat(refreshedProjectConfiguration).isEqualTo(projectWithConfig)
    }

    @Test
    fun `getDeviceConfiguration should call the correct method`() = runTest {
        coEvery { configRepository.getDeviceConfiguration() } returns deviceConfiguration

        val gottenDeviceConfiguration = configManager.getDeviceConfiguration()
        assertThat(gottenDeviceConfiguration).isEqualTo(deviceConfiguration)
    }

    @Test
    fun `updateDeviceConfiguration should call the correct method`() = runTest {
        val update: (c: DeviceConfiguration) -> DeviceConfiguration = {
            it
        }

        configManager.updateDeviceConfiguration(update)
        coVerify(exactly = 1) { configRepository.updateDeviceConfiguration(update) }
    }

    @Test
    fun `clearData should call the correct method`() = runTest {
        configManager.clearData()
        coVerify(exactly = 1) { configRepository.clearData() }
    }

    @Test
    fun `getPrivacyNotice should call the correct method`() = runTest {
        configManager.getPrivacyNotice(PROJECT_ID, LANGUAGE)
        coVerify(exactly = 1) { configRepository.getPrivacyNotice(PROJECT_ID, LANGUAGE) }
    }

    @Test
    fun `observeProjectConfiguration should emit values from the local data source`() = runTest {
        val config1 = projectConfiguration.copy(projectId = "project1")
        val config2 = projectConfiguration.copy(projectId = "project2")

        coEvery { configRepository.observeProjectConfiguration() } returns flow {
            emit(config1)
            emit(config2)
        }

        val emittedConfigs = configManager.observeProjectConfiguration().toList()

        assertThat(emittedConfigs).hasSize(2)
        assertThat(emittedConfigs[0]).isEqualTo(config1)
        assertThat(emittedConfigs[1]).isEqualTo(config2)
    }

    @Test
    fun `observeProjectConfiguration should call getProjectConfiguration on start to invoke download if config empty`() = runTest {
        coEvery { configRepository.observeProjectConfiguration() } returns flow {
            emit(projectConfiguration)
        }

        val emittedConfigs = configManager.observeProjectConfiguration().toList()

        coVerify(exactly = 1) { configRepository.getProjectConfiguration() }

        assertThat(emittedConfigs).hasSize(1)
        assertThat(emittedConfigs[0]).isEqualTo(projectConfiguration)
    }

    @Test
    fun `observeIsProjectRefreshing should initially emit false`() = runTest {
        val isRefreshing = configManager.observeIsProjectRefreshing().first()
        assertThat(isRefreshing).isFalse()
    }

    @Test
    fun `observeIsProjectRefreshing should emit false after refreshProject completes`() = runTest {
        coEvery { configRepository.refreshProject(PROJECT_ID) } returns projectWithConfig
        configManager.refreshProject(PROJECT_ID)

        val isRefreshing = configManager.observeIsProjectRefreshing().first()

        assertThat(isRefreshing).isFalse()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `observeIsProjectRefreshing should emit true during refreshProject and false when done`() = runTest {
        coEvery { configRepository.refreshProject(PROJECT_ID) } coAnswers {
            delay(1000)
            projectWithConfig
        }

        assertThat(configManager.observeIsProjectRefreshing().first()).isFalse() // before

        launch { configManager.refreshProject(PROJECT_ID) }
        advanceTimeBy(500)

        assertThat(configManager.observeIsProjectRefreshing().first()).isTrue() // during

        advanceTimeBy(1000)

        assertThat(configManager.observeIsProjectRefreshing().first()).isFalse() // after
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `observeIsProjectRefreshing should emit false even when refreshProject fails`() = runTest {
        coEvery { configRepository.refreshProject(PROJECT_ID) } coAnswers {
            delay(500)
            throw Exception("Test exception")
        }

        assertThat(configManager.observeIsProjectRefreshing().first()).isFalse() // before

        launch {
            try {
                configManager.refreshProject(PROJECT_ID)
            } catch (e: Exception) {
                // Expected
            }
        }
        advanceTimeBy(1000)

        assertThat(configManager.observeIsProjectRefreshing().first()).isFalse() // after failure
    }
}
