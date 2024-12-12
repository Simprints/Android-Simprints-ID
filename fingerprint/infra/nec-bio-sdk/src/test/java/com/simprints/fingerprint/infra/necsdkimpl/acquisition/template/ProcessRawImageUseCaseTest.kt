package com.simprints.fingerprint.infra.necsdkimpl.acquisition.template

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth
import com.secugen.RawImage
import com.secugen.WSQConverter
import com.simprints.fingerprint.infra.scanner.domain.fingerprint.RawUnprocessedImage
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Dpi
import com.simprints.sgimagecorrection.SecugenImageCorrection
import com.simprints.sgimagecorrection.SecugenImageCorrection.ProcessedImage
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ProcessRawImageUseCaseTest {
    @MockK
    private lateinit var secugenImageCorrection: SecugenImageCorrection

    @MockK
    private lateinit var acquireImageDistortionConfigurationUseCase: AcquireImageDistortionConfigurationUseCase

    @MockK
    private lateinit var wsqConverter: WSQConverter

    private lateinit var processRawImage: ProcessRawImageUseCase

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        coEvery { acquireImageDistortionConfigurationUseCase() } returns byteArrayOf(1, 2, 3)
        processRawImage = ProcessRawImageUseCase(
            secugenImageCorrection,
            acquireImageDistortionConfigurationUseCase,
            wsqConverter,
            testCoroutineRule.testCoroutineDispatcher,
        )
    }

    @Test
    fun `invoke should process raw image and return FingerprintImage`() = runTest {
        // Arrange
        val settings = FingerprintTemplateAcquisitionSettings(
            processingResolution = Dpi(500),
            qualityThreshold = 0,
            timeOutMs = 0,
            allowLowQualityExtraction = true,
        )
        val rawImage = RawUnprocessedImage(
            byteArrayOf(
                0x05,
                0x06,
                0x07,
                0x08,
                0x05,
                0x06,
                0x07,
                0x08,
                0x05,
                0x06,
                0x07,
                0x08,
                0x05,
                0x06,
                0x07,
                0x08,
                0x05,
                0x06,
                0x07,
                0x08,
                0x05,
                0x06,
                0x07,
                0x08,
            ),
        )

        val decodedImage: RawImage =
            RawImage(width = 400, height = 300, ppi = 500, depth = 1, bytes = ByteArray(20))

        val expectedProcessedImage: ProcessedImage = ProcessedImage(
            imageBytes = ByteArray(20),
            width = 400,
            height = 300,
            resolution = 500,
        )

        coEvery { wsqConverter.fromWSQToRaw(rawImage.imageData) } returns decodedImage
        coEvery {
            secugenImageCorrection.processRawImage(decodedImage.bytes, any())
        } returns expectedProcessedImage

        val result = processRawImage(settings, rawImage, byteArrayOf(0), 1)

        Truth.assertThat(result.imageBytes).isEqualTo(expectedProcessedImage.imageBytes)
        Truth.assertThat(result.width).isEqualTo(expectedProcessedImage.width)
        Truth.assertThat(result.height).isEqualTo(expectedProcessedImage.height)
        Truth.assertThat(result.resolution).isEqualTo(expectedProcessedImage.resolution)

        coVerify { wsqConverter.fromWSQToRaw(rawImage.imageData) }
        coVerify { acquireImageDistortionConfigurationUseCase() }
        coVerify {
            secugenImageCorrection.processRawImage(decodedImage.bytes, any())
        }
    }
}
