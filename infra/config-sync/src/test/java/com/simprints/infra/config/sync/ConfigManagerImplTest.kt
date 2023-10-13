package com.simprints.infra.config.sync

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.DeviceConfiguration
import com.simprints.infra.config.sync.testtools.deviceConfiguration
import com.simprints.infra.config.sync.testtools.project
import com.simprints.infra.config.sync.testtools.projectConfiguration
import com.simprints.infra.config.sync.worker.ConfigurationScheduler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ConfigManagerImplTest {

    companion object {
        private const val PROJECT_ID = "projectId"
        private const val LANGUAGE = "fr"
    }

    private val configRepository = mockk<ConfigRepository>(relaxed = true)
    private val configurationScheduler = mockk<ConfigurationScheduler>(relaxed = true)
    private val configManager =
        ConfigManagerImpl(configRepository, configurationScheduler)

    @Test
    fun `refreshProject should call the correct method`() = runTest {
        coEvery { configRepository.refreshProject(PROJECT_ID) } returns project

        val refreshedProject = configManager.refreshProject(PROJECT_ID)
        assertThat(refreshedProject).isEqualTo(project)
    }

    @Test
    fun `getProject should call the correct method`() = runTest {
        coEvery { configRepository.getProject(PROJECT_ID) } returns project

        val gottenProject = configManager.getProject(PROJECT_ID)
        assertThat(gottenProject).isEqualTo(project)
    }

    @Test
    fun `getProjectConfiguration should call the correct method`() = runTest {
        coEvery { configRepository.getConfiguration() } returns projectConfiguration

        val gottenProjectConfiguration = configManager.getProjectConfiguration()
        assertThat(gottenProjectConfiguration).isEqualTo(projectConfiguration)
    }

    @Test
    fun `refreshProjectConfiguration should call the correct method`() = runTest {
        coEvery { configRepository.refreshConfiguration(PROJECT_ID) } returns projectConfiguration

        val refreshedProjectConfiguration = configManager.refreshProjectConfiguration(PROJECT_ID)
        assertThat(refreshedProjectConfiguration).isEqualTo(projectConfiguration)
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
    fun `scheduleSyncConfiguration should call the correct method`() = runTest {
        configManager.scheduleSyncConfiguration()

        coVerify(exactly = 1) { configurationScheduler.scheduleSync() }
    }

    @Test
    fun `cancelScheduledSyncConfiguration should call the correct method`() = runTest {
        configManager.cancelScheduledSyncConfiguration()

        coVerify(exactly = 1) { configurationScheduler.cancelScheduledSync() }
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
