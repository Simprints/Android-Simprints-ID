package com.simprints.feature.troubleshooting.overview.usecase

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.infra.scanner.ScannerManager
import com.simprints.fingerprint.infra.scanner.wrapper.ScannerWrapper
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.FingerprintConfiguration.BioSdk
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CollectScannerStateUseCaseTest {
    @MockK
    private lateinit var configRepository: ConfigRepository

    @MockK
    private lateinit var scannerManager: ScannerManager

    @MockK
    private lateinit var scanner: ScannerWrapper

    private lateinit var useCase: CollectScannerStateUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        useCase = CollectScannerStateUseCase(configRepository, scannerManager)
    }

    @Test
    fun `does not connect to scanner if not configured`() = runTest {
        coEvery { configRepository.getProjectConfiguration().fingerprint } returns null

        assertThat(useCase()).isNotEmpty()

        coVerify(exactly = 0) {
            scannerManager.initScanner()
            scannerManager.scanner.connect()
        }
    }

    @Test
    fun `connects and disconnects to scanner`() = runTest {
        coEvery {
            configRepository.getProjectConfiguration().fingerprint?.allowedSDKs
        } returns listOf(BioSdk.SECUGEN_SIM_MATCHER)
        coJustRun { scannerManager.initScanner() }
        every { scannerManager.scanner } returns scanner
        every { scanner.versionInformation() } returns mockk(relaxed = true)
        every { scanner.batteryInformation() } returns mockk(relaxed = true)

        assertThat(useCase()).isNotEmpty()
        coVerify {
            scannerManager.initScanner()
            scanner.connect()
            scanner.disconnect()
        }
    }

    @Test
    fun `handles connection exceptions`() = runTest {
        coEvery {
            configRepository.getProjectConfiguration().fingerprint?.allowedSDKs
        } returns listOf(BioSdk.SECUGEN_SIM_MATCHER)
        coEvery { scannerManager.initScanner() } throws RuntimeException("test")

        assertThat(useCase()).isNotEmpty()
    }
}
