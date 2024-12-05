package com.simprints.fingerprint.infra.necsdkimpl.acquisition.template

import com.google.common.truth.Truth
import com.simprints.fingerprint.infra.basebiosdk.acquisition.domain.TemplateResponse
import com.simprints.fingerprint.infra.necsdkimpl.acquisition.image.ProcessedImageCache
import com.simprints.fingerprint.infra.scanner.capture.FingerprintCaptureWrapper
import com.simprints.fingerprint.infra.scanner.capture.FingerprintCaptureWrapperFactory
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Dpi
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class FingerprintTemplateProviderImplTest {

    private lateinit var fingerprintTemplateProviderImpl: FingerprintTemplateProviderImpl

    @RelaxedMockK
    private lateinit var calculateNecImageQualityUseCase: CalculateNecImageQualityUseCase

    @MockK
    private lateinit var fingerprintCaptureWrapperFactory: FingerprintCaptureWrapperFactory

    @MockK
    private lateinit var extractNecTemplateUseCase: ExtractNecTemplateUseCase

    @RelaxedMockK
    private lateinit var processedImageCache: ProcessedImageCache

    @RelaxedMockK
    private lateinit var captureWrapper: FingerprintCaptureWrapper

    @RelaxedMockK
    private lateinit var processRawImage: ProcessRawImageUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { fingerprintCaptureWrapperFactory.captureWrapper } returns captureWrapper
        coEvery {
            extractNecTemplateUseCase.invoke(any(), any())
        } returns TemplateResponse(
            byteArrayOf(1, 2, 3), FingerprintTemplateMetadata(
                templateFormat = NEC_TEMPLATE_FORMAT, imageQualityScore = 10
            )
        )
        fingerprintTemplateProviderImpl = FingerprintTemplateProviderImpl(
            fingerprintCaptureWrapperFactory = fingerprintCaptureWrapperFactory,
            calculateNecImageQualityUseCase = calculateNecImageQualityUseCase,
            captureProcessedImageCache = processedImageCache,
            extractNecTemplateUseCase = extractNecTemplateUseCase,
            processImage = processRawImage
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
            captureWrapper.acquireUnprocessedImage(any())
            processRawImage(any(), any(), any(), any())
            processedImageCache.recentlyCapturedImage = any()
            calculateNecImageQualityUseCase.invoke(any())
            extractNecTemplateUseCase.invoke(any(), any())

        }
    }

    @Test
    fun `test acquireFingerprintTemplate fails if quality score is less than threshold and allowLowQualityExtraction is false`() =
        runTest {
            // Given
            coEvery { calculateNecImageQualityUseCase.invoke(any()) } returns 10
            val settings = FingerprintTemplateAcquisitionSettings(
                processingResolution = Dpi(500),
                qualityThreshold = 20,
                timeOutMs = 0,
                allowLowQualityExtraction = false
            )
            // When
            val result = fingerprintTemplateProviderImpl.acquireFingerprintTemplate(settings)
            // Then
            coVerify {
                captureWrapper.acquireUnprocessedImage(any())
                processedImageCache.recentlyCapturedImage = any()
                processRawImage(any(), any(), any(), any())
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
            coEvery { calculateNecImageQualityUseCase.invoke(any()) } returns 10
            val settings = FingerprintTemplateAcquisitionSettings(
                processingResolution = Dpi(500),
                qualityThreshold = 20,
                timeOutMs = 0,
                allowLowQualityExtraction = true
            )
            // When
            val result = fingerprintTemplateProviderImpl.acquireFingerprintTemplate(settings)
            // Then
            coVerify {
                captureWrapper.acquireUnprocessedImage(any())
                processedImageCache.recentlyCapturedImage = any()
                processRawImage(any(), any(), any(), any())
                calculateNecImageQualityUseCase.invoke(any())
                extractNecTemplateUseCase.invoke(any(), any())
            }
            Truth.assertThat(result.template).isNotEmpty()
        }
}
