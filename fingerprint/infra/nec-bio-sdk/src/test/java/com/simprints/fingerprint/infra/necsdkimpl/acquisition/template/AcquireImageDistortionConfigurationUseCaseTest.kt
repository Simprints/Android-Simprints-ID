package com.simprints.fingerprint.infra.necsdkimpl.acquisition.template

import com.google.common.truth.Truth
import com.simprints.fingerprint.infra.imagedistortionconfig.ImageDistortionConfigRepo
import com.simprints.fingerprint.infra.scanner.capture.FingerprintCaptureWrapper
import com.simprints.fingerprint.infra.scanner.capture.FingerprintCaptureWrapperFactory
import com.simprints.fingerprint.infra.scanner.v2.scanner.ScannerInfo
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalStdlibApi::class)
class AcquireImageDistortionConfigurationUseCaseTest {
    @MockK
    private lateinit var fingerprintCaptureWrapperFactory: FingerprintCaptureWrapperFactory

    @MockK
    private lateinit var captureWrapper: FingerprintCaptureWrapper

    @MockK
    private lateinit var imageDistortionConfigRepo: ImageDistortionConfigRepo
    private lateinit var scannerInfo: ScannerInfo

    private lateinit var acquireImageDistortionConfigurationUseCase: AcquireImageDistortionConfigurationUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        scannerInfo = ScannerInfo().apply {
            setScannerId("scannerId")
            setUn20SerialNumber("serialNumber")
        }
        every { fingerprintCaptureWrapperFactory.captureWrapper } returns captureWrapper

        acquireImageDistortionConfigurationUseCase = AcquireImageDistortionConfigurationUseCase(
            fingerprintCaptureWrapperFactory,
            scannerInfo,
            imageDistortionConfigRepo,
        )
    }

    @Test
    fun `should acquire image distortion configuration from scanner if not stored locally`() = runTest {
        // given
        val distortionConfiguration = byteArrayOf(1, 2, 3).toHexString()
        coEvery { imageDistortionConfigRepo.getConfig(any()) } returns null
        coJustRun { imageDistortionConfigRepo.saveConfig(any(), any(), any()) }
        coEvery {
            captureWrapper.acquireImageDistortionMatrixConfiguration()
        } returns distortionConfiguration.hexToByteArray()

        // when
        val result = acquireImageDistortionConfigurationUseCase()

        // then
        Truth.assertThat(result.toHexString()).isEqualTo(distortionConfiguration)
        coVerify { captureWrapper.acquireImageDistortionMatrixConfiguration() }
        coVerify { imageDistortionConfigRepo.saveConfig(any(), any(), any()) }
    }

    @Test
    fun `should acquire image distortion configuration from shared preferences if available`() = runTest {
        // given
        val distortionConfiguration = byteArrayOf(1, 2, 3)
        coEvery { imageDistortionConfigRepo.getConfig(any()) } returns distortionConfiguration

        // when
        val result = acquireImageDistortionConfigurationUseCase()

        // then
        Truth.assertThat(result).isEqualTo(distortionConfiguration)
        coVerify(exactly = 0) { captureWrapper.acquireImageDistortionMatrixConfiguration() }
    }
}
