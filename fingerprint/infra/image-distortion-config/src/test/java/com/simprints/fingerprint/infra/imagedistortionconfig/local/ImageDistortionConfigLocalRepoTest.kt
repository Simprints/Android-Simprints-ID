package com.simprints.fingerprint.infra.imagedistortionconfig.local

import com.google.common.truth.Truth
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ImageDistortionConfigLocalRepoTest {
    private lateinit var repo: ImageDistortionConfigLocalRepo
    private val mockDao: ImageDistortionConfigDao = mockk()

    @Before
    fun setUp() {
        repo = ImageDistortionConfigLocalRepo(dao = mockDao)
    }

    @Test
    fun `saveConfig inserts the configuration into the DAO`() = runTest {
        val scannerId = "scanner123"
        val serialNumber = "serial123"
        val configFile = byteArrayOf(1, 2, 3)
        val expectedConfig = DbImageDistortionConfig(
            scannerId = scannerId,
            un20SerialNumber = serialNumber,
            configFile = configFile,
            isUploaded = false,
        )

        coJustRun { mockDao.insertConfig(expectedConfig) }

        repo.saveConfig(scannerId, serialNumber, configFile)

        coVerify { mockDao.insertConfig(expectedConfig) }
    }

    @Test
    fun `getPendingUploads returns pending configurations from DAO`() = runTest {
        val pendingConfigs = listOf(
            DbImageDistortionConfig(1, "scanner1", "serial1", byteArrayOf(1), false),
            DbImageDistortionConfig(2, "scanner2", "serial2", byteArrayOf(2), false),
        )

        coEvery { mockDao.getPendingUploads() } returns pendingConfigs

        val result = repo.getPendingUploads()

        Truth.assertThat(pendingConfigs).isEqualTo(result)
        coVerify { mockDao.getPendingUploads() }
    }

    @Test
    fun `markAsUploaded updates the configuration in the DAO`() = runTest {
        val configId = 123

        coJustRun { mockDao.markAsUploaded(configId) }

        repo.markAsUploaded(configId)

        coVerify { mockDao.markAsUploaded(configId) }
    }

    @Test
    fun `getConfigFile retrieves the configuration file by scannerId`() = runTest {
        val scannerId = "scanner123"
        val expectedConfig = DbImageDistortionConfig(
            id = 1,
            scannerId = scannerId,
            un20SerialNumber = "serial123",
            configFile = byteArrayOf(1, 2, 3),
            isUploaded = false,
        )

        coEvery { mockDao.getConfigByScannerId(scannerId) } returns expectedConfig

        val result = repo.getConfigFile(scannerId)

        Truth.assertThat(expectedConfig.configFile).isEqualTo(result)
        coVerify { mockDao.getConfigByScannerId(scannerId) }
    }

    @Test
    fun `getConfigFile returns null when configuration is not found`() = runTest {
        val scannerId = "scanner123"

        coEvery { mockDao.getConfigByScannerId(scannerId) } returns null

        val result = repo.getConfigFile(scannerId)

        Truth.assertThat(result).isNull()
        coVerify { mockDao.getConfigByScannerId(scannerId) }
    }
}
