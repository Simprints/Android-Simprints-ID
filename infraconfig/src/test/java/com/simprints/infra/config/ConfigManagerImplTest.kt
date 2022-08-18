package com.simprints.infra.config

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.domain.ConfigService
import com.simprints.infra.config.testtools.project
import com.simprints.infra.config.testtools.projectConfiguration
import com.simprints.infra.config.worker.ConfigurationScheduler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ConfigManagerImplTest {

    companion object {
        private const val PROJECT_ID = "projectId"
    }

    private val configRepository = mockk<ConfigService>()
    private val configurationScheduler = mockk<ConfigurationScheduler>(relaxed = true)
    private val configManager = ConfigManagerImpl(configRepository, configurationScheduler)

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
    fun `getConfiguration should call the correct method`() = runTest {
        coEvery { configRepository.getConfiguration() } returns projectConfiguration

        val gottenProjectConfiguration = configManager.getConfiguration()
        assertThat(gottenProjectConfiguration).isEqualTo(projectConfiguration)
    }

    @Test
    fun `refreshConfiguration should call the correct method`() = runTest {
        coEvery { configRepository.refreshConfiguration(PROJECT_ID) } returns projectConfiguration

        val refreshedProjectConfiguration = configManager.refreshConfiguration(PROJECT_ID)
        assertThat(refreshedProjectConfiguration).isEqualTo(projectConfiguration)
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
}
