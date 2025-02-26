package com.simprints.infra.config.sync

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.DeviceConfiguration
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.ProjectWithConfig
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.toList
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
    private lateinit var project: Project

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        configManager = ConfigManager(
            configRepository = configRepository,
            enrolmentRecordRepository = enrolmentRecordRepository,
            configSyncCache = configSyncCache,
        )
    }

    @Test
    fun `refreshProject should call the correct method`() = runTest {
        coEvery { configRepository.refreshProject(PROJECT_ID) } returns projectWithConfig

        val refreshedProject = configManager.refreshProject(PROJECT_ID)
        assertThat(refreshedProject).isEqualTo(projectWithConfig)

        coVerify { configSyncCache.saveUpdateTime() }
    }

    @Test
    fun `getProject should call the correct method`() = runTest {
        coEvery { configRepository.getProject() } returns project

        val gottenProject = configManager.getProject(PROJECT_ID)
        assertThat(gottenProject).isEqualTo(project)
    }

    @Test
    fun `getProjectConfiguration should call the correct method`() = runTest {
        coEvery { configRepository.getProjectConfiguration() } returns projectConfiguration
        every { projectConfiguration.projectId } returns PROJECT_ID

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
    fun `watchProjectConfiguration should emit values from the local data source`() = runTest {
        val config1 = projectConfiguration.copy(projectId = "project1")
        val config2 = projectConfiguration.copy(projectId = "project2")

        coEvery { configRepository.watchProjectConfiguration() } returns kotlinx.coroutines.flow.flow {
            emit(config1)
            emit(config2)
        }

        val emittedConfigs = configManager.watchProjectConfiguration().toList()

        assertThat(emittedConfigs).hasSize(2)
        assertThat(emittedConfigs[0]).isEqualTo(config1)
        assertThat(emittedConfigs[1]).isEqualTo(config2)
    }

    @Test
    fun `watchProjectConfiguration should call getProjectConfiguration on start to invoke download if config empty`() = runTest {
        coEvery { configRepository.watchProjectConfiguration() } returns kotlinx.coroutines.flow.flow {
            emit(projectConfiguration)
        }

        val emittedConfigs = configManager.watchProjectConfiguration().toList()

        coVerify(exactly = 1) { configRepository.getProjectConfiguration() }

        assertThat(emittedConfigs).hasSize(1)
        assertThat(emittedConfigs[0]).isEqualTo(projectConfiguration)
    }
}
