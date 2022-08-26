package com.simprints.infra.config.domain

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.domain.models.DeviceConfiguration
import com.simprints.infra.config.local.ConfigLocalDataSource
import com.simprints.infra.config.remote.ConfigRemoteDataSource
import com.simprints.infra.config.testtools.deviceConfiguration
import com.simprints.infra.config.testtools.project
import com.simprints.infra.config.testtools.projectConfiguration
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ConfigServiceImplTest {

    companion object {
        private const val PROJECT_ID = "projectId"
    }

    private val localDataSource = mockk<ConfigLocalDataSource>(relaxed = true)
    private val remoteDataSource = mockk<ConfigRemoteDataSource>()
    private val configServiceImpl = ConfigServiceImpl(localDataSource, remoteDataSource)

    @Test
    fun `should get the project locally if available`() = runTest {
        coEvery { localDataSource.getProject() } returns project

        val receivedProject = configServiceImpl.getProject(PROJECT_ID)

        assertThat(receivedProject).isEqualTo(project)
        coVerify(exactly = 1) { localDataSource.getProject() }
        coVerify(exactly = 0) { remoteDataSource.getProject(any()) }
    }

    @Test
    fun `should get the project remotely if not available locally and save it`() = runTest {
        coEvery { localDataSource.saveProject(project) } returns Unit
        coEvery { localDataSource.getProject() } throws NoSuchElementException()
        coEvery { remoteDataSource.getProject(PROJECT_ID) } returns project

        val receivedProject = configServiceImpl.getProject(PROJECT_ID)

        assertThat(receivedProject).isEqualTo(project)
        coVerify(exactly = 1) { localDataSource.getProject() }
        coVerify(exactly = 1) { localDataSource.saveProject(project) }
        coVerify(exactly = 1) { remoteDataSource.getProject(PROJECT_ID) }
    }

    @Test
    fun `refresh project should get the project remotely and save it`() = runTest {
        coEvery { localDataSource.saveProject(project) } returns Unit
        coEvery { remoteDataSource.getProject(PROJECT_ID) } returns project

        configServiceImpl.refreshProject(PROJECT_ID)
        coVerify(exactly = 1) { localDataSource.saveProject(project) }
        coVerify(exactly = 1) { remoteDataSource.getProject(PROJECT_ID) }
    }

    @Test
    fun `should get the project configuration locally`() = runTest {
        coEvery { localDataSource.getProjectConfiguration() } returns projectConfiguration

        val receivedProject = configServiceImpl.getConfiguration()

        assertThat(receivedProject).isEqualTo(projectConfiguration)
        coVerify(exactly = 1) { localDataSource.getProjectConfiguration() }
    }

    @Test
    fun `refresh project configuration should get the project configuration remotely and save it`() =
        runTest {
            coEvery { localDataSource.saveProjectConfiguration(projectConfiguration) } returns Unit
            coEvery { remoteDataSource.getConfiguration(PROJECT_ID) } returns projectConfiguration

            configServiceImpl.refreshConfiguration(PROJECT_ID)
            coVerify(exactly = 1) { localDataSource.saveProjectConfiguration(projectConfiguration) }
            coVerify(exactly = 1) { remoteDataSource.getConfiguration(PROJECT_ID) }
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
}
