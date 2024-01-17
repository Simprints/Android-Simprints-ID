package com.simprints.fingerprint.infra.necsdkimpl.acquisition.template

import com.simprints.fingerprint.infra.necsdkimpl.acquisition.image.ImageCache
import com.simprints.fingerprint.infra.scanner.capture.FingerprintCaptureWrapperFactory
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Dpi
import com.simprints.necwrapper.nec.fingerprint.FingerprintImageQualityCheck
import io.mockk.MockKAnnotations
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
    private lateinit var necImageQualityCalculator: NecImageQualityCalculator

    @RelaxedMockK
    private lateinit var secugenImageCorrection: SecugenImageCorrection

    @RelaxedMockK
    private lateinit var fingerprintCaptureWrapperFactory: FingerprintCaptureWrapperFactory

    @RelaxedMockK
    private lateinit var necTemplateExtractor: NecTemplateExtractor

    @RelaxedMockK
    private lateinit var wsqImageDecoder: WSQImageDecoder

    @RelaxedMockK
    private lateinit var imageCache: ImageCache


    @MockK

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        fingerprintTemplateProviderImpl = FingerprintTemplateProviderImpl(
            fingerprintCaptureWrapperFactory = fingerprintCaptureWrapperFactory,
            wsqImageDecoder = wsqImageDecoder,
            secugenImageCorrection = secugenImageCorrection,
            qualityCalculator = necImageQualityCalculator,
            necTemplateExtractor = necTemplateExtractor,
            captureImageCache = imageCache
        )

    }

    @Test(expected = IllegalArgumentException::class)
    fun `test null settings throws exception`() = runTest {
        // Given
        val settings = null
        // When
        val result = fingerprintTemplateProviderImpl.acquireFingerprintTemplate(settings)
        // Then

    }

    @Test
    fun `test acquireFingerprintTemplate success`() = runTest {
        // Given
        val settings = FingerprintTemplateAcquisitionSettings(
            processingResolution = Dpi(500),
            qualityThreshold = 0,
            timeOutMs = 0
        )
        // When
        val result = fingerprintTemplateProviderImpl.acquireFingerprintTemplate(settings)
        // Then
        coVerify {
            fingerprintCaptureWrapperFactory.captureWrapper
                .acquireUnprocessedImage(any())
            wsqImageDecoder.decode(any())
            imageCache.lastCaptureImage = any()
            secugenImageCorrection.processRawImage(any(), any())
            necImageQualityCalculator.getQualityScore(any())
            necTemplateExtractor.extract(any(), any())

        }
    }
    @Test(expected = FingerprintImageQualityCheck.QualityCheckFailedException::class)
    fun `test acquireFingerprintTemplate fails if quality score is less than threshold`() = runTest {
        // Given
        val settings = FingerprintTemplateAcquisitionSettings(
            processingResolution = Dpi(500),
            qualityThreshold = 30,
            timeOutMs = 0
        )
        every { necImageQualityCalculator.getQualityScore(any()) } returns 10
        coVerify {
            fingerprintCaptureWrapperFactory.captureWrapper
                .acquireUnprocessedImage(any())
            wsqImageDecoder.decode(any())
            imageCache.lastCaptureImage = any()
            secugenImageCorrection.processRawImage(any(), any())
            necImageQualityCalculator.getQualityScore(any())
            necTemplateExtractor.extract(any(), any())

        }
    }
}
