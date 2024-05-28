package com.simprints.infra.config.sync

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.DeviceConfiguration
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.ProjectWithConfig
import com.simprints.infra.enrolment.records.store.EnrolmentRecordRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
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
            enrolmentRecordRepository = enrolmentRecordRepository
        )
    }

    @Test
    fun `refreshProject should call the correct method`() = runTest {
        coEvery { configRepository.refreshProject(PROJECT_ID) } returns projectWithConfig

        val refreshedProject = configManager.refreshProject(PROJECT_ID)
        assertThat(refreshedProject).isEqualTo(projectWithConfig)
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

}
