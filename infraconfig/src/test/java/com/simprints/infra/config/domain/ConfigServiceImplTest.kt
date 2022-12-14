package com.simprints.infra.config.domain

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.domain.ConfigServiceImpl.Companion.PRIVACY_NOTICE_FILE
import com.simprints.infra.config.domain.models.DeviceConfiguration
import com.simprints.infra.config.domain.models.PrivacyNoticeResult.*
import com.simprints.infra.config.local.ConfigLocalDataSource
import com.simprints.infra.config.remote.ConfigRemoteDataSource
import com.simprints.infra.config.testtools.deviceConfiguration
import com.simprints.infra.config.testtools.project
import com.simprints.infra.config.testtools.projectConfiguration
import com.simprints.infra.network.exceptions.BackendMaintenanceException
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.*
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ConfigServiceImplTest {

    companion object {
        private const val PROJECT_ID = "projectId"
        private const val LANGUAGE = "fr"
        private const val PRIVACY_NOTICE = "privacy notice"
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
    fun `should throw the exception if there is an issue`() = runTest {
        val exception = Exception("exception")
        coEvery { localDataSource.getProject() } throws exception

        val receivedException = assertThrows<Exception> { configServiceImpl.getProject(PROJECT_ID) }

        assertThat(receivedException).isEqualTo(exception)
        coVerify(exactly = 1) { localDataSource.getProject() }
        coVerify(exactly = 0) { localDataSource.saveProject(project) }
        coVerify(exactly = 0) { remoteDataSource.getProject(PROJECT_ID) }
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
                "${PRIVACY_NOTICE_FILE}_$LANGUAGE"
            )
        } returns PRIVACY_NOTICE

        val results = configServiceImpl.getPrivacyNotice(PROJECT_ID, LANGUAGE).toList()

        assertThat(results).isEqualTo(
            listOf(
                InProgress(LANGUAGE),
                Succeed(LANGUAGE, PRIVACY_NOTICE)
            )
        )
        verify(exactly = 1) {
            localDataSource.storePrivacyNotice(
                PROJECT_ID,
                LANGUAGE,
                PRIVACY_NOTICE
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
                    "${PRIVACY_NOTICE_FILE}_$LANGUAGE"
                )
            } throws exception

            val results = configServiceImpl.getPrivacyNotice(PROJECT_ID, LANGUAGE).toList()

            assertThat(results).isEqualTo(
                listOf(
                    InProgress(LANGUAGE),
                    FailedBecauseBackendMaintenance(LANGUAGE, exception, 10)
                )
            )
            verify(exactly = 0) { localDataSource.storePrivacyNotice(PROJECT_ID, LANGUAGE, any()) }
        }

    @Test
    fun `should return a Failed if it fails to download the privacy notice with another exception`() =
        runTest {
            val exception = Exception()
            every { localDataSource.hasPrivacyNoticeFor(PROJECT_ID, LANGUAGE) } returns false
            coEvery {
                remoteDataSource.getPrivacyNotice(
                    PROJECT_ID,
                    "${PRIVACY_NOTICE_FILE}_$LANGUAGE"
                )
            } throws exception

            val results = configServiceImpl.getPrivacyNotice(PROJECT_ID, LANGUAGE).toList()

            assertThat(results).isEqualTo(
                listOf(
                    InProgress(LANGUAGE),
                    Failed(LANGUAGE, exception)
                )
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
}
