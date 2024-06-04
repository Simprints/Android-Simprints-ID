package com.simprints.fingerprint.infra.necsdkimpl.acquisition.template

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth
import com.secugen.WSQConverter
import com.simprints.fingerprint.infra.basebiosdk.acquisition.domain.TemplateResponse
import com.simprints.fingerprint.infra.necsdkimpl.acquisition.image.ProcessedImageCache
import com.simprints.fingerprint.infra.scanner.capture.FingerprintCaptureWrapper
import com.simprints.fingerprint.infra.scanner.capture.FingerprintCaptureWrapperFactory
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Dpi
import com.simprints.sgimagecorrection.SecugenImageCorrection
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FingerprintTemplateProviderImplTest {

    private lateinit var fingerprintTemplateProviderImpl: FingerprintTemplateProviderImpl

    @RelaxedMockK
    private lateinit var calculateNecImageQualityUseCase: CalculateNecImageQualityUseCase

    @RelaxedMockK
    private lateinit var secugenImageCorrection: SecugenImageCorrection

    @MockK
    private lateinit var acquireImageDistortionConfigurationUseCase: AcquireImageDistortionConfigurationUseCase

    @MockK
    private lateinit var fingerprintCaptureWrapperFactory: FingerprintCaptureWrapperFactory

    @MockK
    private lateinit var extractNecTemplateUseCase: ExtractNecTemplateUseCase

    @RelaxedMockK
    private lateinit var wsqConverter: WSQConverter

    @RelaxedMockK
    private lateinit var processedImageCache: ProcessedImageCache

    @RelaxedMockK
    private lateinit var captureWrapper: FingerprintCaptureWrapper

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()


    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { fingerprintCaptureWrapperFactory.captureWrapper } returns captureWrapper
        coEvery { acquireImageDistortionConfigurationUseCase.invoke() } returns byteArrayOf(1, 2, 3)
        coEvery{
            extractNecTemplateUseCase.invoke(any(), any())
        } returns TemplateResponse(byteArrayOf(1, 2, 3),FingerprintTemplateMetadata(
            templateFormat = NEC_TEMPLATE_FORMAT,
            imageQualityScore = 10
        ))
        fingerprintTemplateProviderImpl = FingerprintTemplateProviderImpl(
            fingerprintCaptureWrapperFactory = fingerprintCaptureWrapperFactory,
            wsqConverter = wsqConverter,
            secugenImageCorrection = secugenImageCorrection,
            acquireImageDistortionConfigurationUseCase = acquireImageDistortionConfigurationUseCase,
            calculateNecImageQualityUseCase = calculateNecImageQualityUseCase,
            captureProcessedImageCache = processedImageCache,
            extractNecTemplateUseCase = extractNecTemplateUseCase,
            ioDispatcher = testCoroutineRule.testCoroutineDispatcher
        )

    }

    @Test(expected = IllegalArgumentException::class)
    fun `test null settings throws exception`() = runTest {
        // Given
        val settings = null
        // When
        fingerprintTemplateProviderImpl.acquireFingerprintTemplate(settings)
        // Then exception is thrown

    }

    @Test
    fun `test acquireFingerprintTemplate success`() = runTest {
        // Given
        val settings = FingerprintTemplateAcquisitionSettings(
            processingResolution = Dpi(500),
            qualityThreshold = 0,
            timeOutMs = 0,
            allowLowQualityExtraction = false
        )
        // When
        fingerprintTemplateProviderImpl.acquireFingerprintTemplate(settings)
        // Then
        coVerify {
            captureWrapper
                .acquireUnprocessedImage(any())
            wsqConverter.fromWSQToRaw(any())
            processedImageCache.recentlyCapturedImage = any()
            secugenImageCorrection.processRawImage(any(), any())
            calculateNecImageQualityUseCase.invoke(any())
            extractNecTemplateUseCase.invoke(any(), any())

        }
    }

    @Test
    fun `test acquireFingerprintTemplate fails if quality score is less than threshold and allowLowQualityExtraction is false`() =
        runTest {
            // Given
            every { calculateNecImageQualityUseCase.invoke(any()) } returns 10
            val settings = FingerprintTemplateAcquisitionSettings(
                processingResolution = Dpi(500),
                qualityThreshold = 20,
                timeOutMs = 0,
                allowLowQualityExtraction = false
            )
            // When
            val result=fingerprintTemplateProviderImpl.acquireFingerprintTemplate(settings)
            // Then
            coVerify {
                captureWrapper
                    .acquireUnprocessedImage(any())
                wsqConverter.fromWSQToRaw(any())
                processedImageCache.recentlyCapturedImage = any()
                secugenImageCorrection.processRawImage(any(), any())
                calculateNecImageQualityUseCase.invoke(any())
            }
            coVerify(exactly = 0) {
                extractNecTemplateUseCase.invoke(any(), any())
            }
            Truth.assertThat(result.template).isEmpty()
        }
    @Test
    fun `test acquireFingerprintTemplate extracts template if quality score is less than threshold and allowLowQualityExtraction is true`() =
        runTest {
            // Given
            every { calculateNecImageQualityUseCase.invoke(any()) } returns 10
            val settings = FingerprintTemplateAcquisitionSettings(
                processingResolution = Dpi(500),
                qualityThreshold = 20,
                timeOutMs = 0,
                allowLowQualityExtraction = true
            )
            // When
            val result=fingerprintTemplateProviderImpl.acquireFingerprintTemplate(settings)
            // Then
            coVerify {
                captureWrapper
                    .acquireUnprocessedImage(any())
                wsqConverter.fromWSQToRaw(any())
                processedImageCache.recentlyCapturedImage = any()
                secugenImageCorrection.processRawImage(any(), any())
                calculateNecImageQualityUseCase.invoke(any())
                extractNecTemplateUseCase.invoke(any(), any())
            }
            Truth.assertThat(result.template).isNotEmpty()
        }
}
