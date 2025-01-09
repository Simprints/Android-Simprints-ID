package com.simprints.fingerprint.infra.imagedistortionconfig

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.infra.imagedistortionconfig.local.DbImageDistortionConfig
import com.simprints.fingerprint.infra.imagedistortionconfig.local.ImageDistortionConfigLocalRepo
import com.simprints.fingerprint.infra.imagedistortionconfig.remote.ImageDistortionConfigRemoteRepo
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ImageDistortionConfigRepoTest {
    @MockK
    private lateinit var localRepo: ImageDistortionConfigLocalRepo

    @MockK
    private lateinit var remoteRepo: ImageDistortionConfigRemoteRepo

    private lateinit var repo: ImageDistortionConfigRepo

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        repo = ImageDistortionConfigRepo(localRepo, remoteRepo)
    }

    @Test
    fun `saveConfig should save to localRepo`() = runBlocking {
        // Given
        val scannerId = "scanner123"
        val un20SerialNumber = "serial456"
        val configFile = byteArrayOf(1, 2, 3, 4)

        coJustRun { localRepo.saveConfig(scannerId, un20SerialNumber, configFile) }

        // When
        repo.saveConfig(scannerId, un20SerialNumber, configFile)

        // Then
        coVerify(exactly = 1) { localRepo.saveConfig(scannerId, un20SerialNumber, configFile) }
    }

    @Test
    fun `uploadPendingConfigs should upload all pending configs and mark as uploaded`() = runTest {
        // Given
        val pendingConfigs = listOf(
            DbImageDistortionConfig(
                id = 1,
                scannerId = "scanner1",
                un20SerialNumber = "serial1",
                configFile = byteArrayOf(1, 2, 3),
                isUploaded = false,
            ),
            DbImageDistortionConfig(
                id = 2,
                scannerId = "scanner2",
                un20SerialNumber = "serial2",
                configFile = byteArrayOf(4, 5, 6),
                isUploaded = false,
            ),
        )

        coEvery { localRepo.getPendingUploads() } returns pendingConfigs
        coEvery { remoteRepo.uploadConfig(any(), any()) } returns true
        coJustRun { localRepo.markAsUploaded(any()) }

        // When
        val result = repo.uploadPendingConfigs()

        // Then
        coVerifySequence {
            localRepo.getPendingUploads()
            remoteRepo.uploadConfig(pendingConfigs[0].un20SerialNumber, pendingConfigs[0].configFile)
            localRepo.markAsUploaded(1)
            remoteRepo.uploadConfig(pendingConfigs[1].un20SerialNumber, pendingConfigs[1].configFile)
            localRepo.markAsUploaded(2)
        }
        assertThat(result).isTrue()
    }

    @Test
    fun `uploadPendingConfigs should return false if upload fails`() = runTest {
        // Given
        val pendingConfigs = listOf(
            DbImageDistortionConfig(
                id = 1,
                scannerId = "scanner1",
                un20SerialNumber = "serial1",
                configFile = byteArrayOf(1, 2, 3),
                isUploaded = false,
            ),
            DbImageDistortionConfig(
                id = 2,
                scannerId = "scanner2",
                un20SerialNumber = "serial2",
                configFile = byteArrayOf(4, 5, 6),
                isUploaded = false,
            ),
        )

        coEvery { localRepo.getPendingUploads() } returns pendingConfigs
        coEvery { remoteRepo.uploadConfig(pendingConfigs[0].un20SerialNumber, pendingConfigs[0].configFile) } returns true
        coEvery { remoteRepo.uploadConfig(pendingConfigs[1].un20SerialNumber, pendingConfigs[1].configFile) } returns false
        coJustRun { localRepo.markAsUploaded(any()) }

        // When
        val result = repo.uploadPendingConfigs()

        // Then
        coVerify(exactly = 1) { remoteRepo.uploadConfig(pendingConfigs[0].un20SerialNumber, pendingConfigs[0].configFile) }
        coVerify(exactly = 1) { localRepo.markAsUploaded(1) }
        coVerify(exactly = 1) { remoteRepo.uploadConfig(pendingConfigs[1].un20SerialNumber, pendingConfigs[1].configFile) }
        coVerify(exactly = 0) { localRepo.markAsUploaded(2) }
        assertThat(result).isFalse()
    }

    @Test
    fun `getConfig should retrieve config from localRepo`() = runTest {
        // Given
        val scannerId = "scanner123"
        val configFile = byteArrayOf(1, 2, 3)

        coEvery { localRepo.getConfigFile(scannerId) } returns configFile

        // When
        val result = repo.getConfig(scannerId)

        // Then
        coVerify(exactly = 1) { localRepo.getConfigFile(scannerId) }
        assertThat(result).isEqualTo(configFile)
    }
}
