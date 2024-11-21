package com.simprints.fingerprint.infra.scanner.capture

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.infra.scanner.domain.fingerprint.AcquireFingerprintImageResponse
import com.simprints.fingerprint.infra.scanner.domain.fingerprint.AcquireFingerprintTemplateResponse
import com.simprints.fingerprint.infra.scanner.domain.fingerprint.RawUnprocessedImage
import com.simprints.fingerprint.infra.scanner.exceptions.safe.NoFingerDetectedException
import com.simprints.fingerprint.infra.scanner.exceptions.unexpected.UnexpectedScannerException
import com.simprints.fingerprint.infra.scanner.exceptions.unexpected.UnknownScannerIssueException
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.CaptureFingerprintResult
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Dpi
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.ImageData
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.TemplateData
import com.simprints.fingerprint.infra.scanner.v2.scanner.Scanner
import com.simprints.fingerprint.infra.scanner.v2.tools.ScannerUiHelper
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FingerprintCaptureWrapperV2Test {
    @MockK
    private lateinit var scannerV2: Scanner

    @MockK
    private lateinit var scannerUiHelper: ScannerUiHelper

    private lateinit var scannerWrapper: FingerprintCaptureWrapperV2

    @MockK
    private lateinit var tracker: FingerprintScanningStatusTracker

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        scannerWrapper =
            FingerprintCaptureWrapperV2(scannerV2, scannerUiHelper, UnconfinedTestDispatcher(),tracker)
    }

    @Test
    fun `test acquireImageDistortionMatrixConfiguration success`() = runTest {
        val expectedResp = byteArrayOf(1, 2, 3)
        every { scannerV2.acquireImageDistortionConfigurationMatrix() } returns Maybe.just(
            expectedResp
        )
        val actualResponse = scannerWrapper.acquireImageDistortionMatrixConfiguration()
        assertThat(actualResponse).isEqualTo(expectedResp)
    }

    @Test
    fun `test acquireUnprocessedImage success`() = runTest {
        // Given
        val imageData = ImageData(
            byteArrayOf(
                0x05, 0x06, 0x07, 0x08, 0x05, 0x06, 0x07, 0x08,
                0x05, 0x06, 0x07, 0x08, 0x05, 0x06, 0x07, 0x08,
                0x05, 0x06, 0x07, 0x08, 0x05, 0x06, 0x07, 0x08
            ), 1
        )
        every { scannerV2.captureFingerprint(any()) } returns Single.just(CaptureFingerprintResult.OK)
        every { scannerV2.acquireUnprocessedImage(any()) } returns Maybe.just(imageData)
        // When
        val actualResponse = scannerWrapper.acquireUnprocessedImage(Dpi(500))
        // Then
        assertThat(actualResponse.rawUnprocessedImage.imageData).isEqualTo(
            RawUnprocessedImage(
                imageData.image
            ).imageData
        )
    }

    @Test(expected = NoFingerDetectedException::class)
    fun `test acquireUnprocessedImage throws NoFingerDetectedException when scanner returns empty`() =
        runTest {
            // Given
            every { scannerV2.acquireUnprocessedImage(any()) } returns Maybe.empty()
            every { scannerV2.captureFingerprint(any()) } returns Single.just(
                CaptureFingerprintResult.OK
            )
            // When
            scannerWrapper.acquireUnprocessedImage(Dpi(500))
            // Then throw NoFingerDetectedException

        }

    @Test
    fun `should throw illegal argument exception when capture DPI is null`() = runTest {
        assertThrows<IllegalArgumentException> {
            scannerWrapper.acquireFingerprintTemplate(
                null,
                1000,
                50,
                false
            )
        }
    }

    @Test
    fun `should throw illegal argument exception when capture DPI is less than 500`() = runTest {
        assertThrows<IllegalArgumentException> {
            scannerWrapper.acquireFingerprintTemplate(
                Dpi(499),
                1000,
                50,
                false
            )
        }
    }

    @Test
    fun `should throw illegal argument exception when capture DPI is greater than 1700`() =
        runTest {
            assertThrows<IllegalArgumentException> {
                scannerWrapper.acquireFingerprintTemplate(
                    Dpi(1701),
                    1000,
                    50,
                    false
                )
            }
        }

    @Test
    fun `should throw corresponding errors when capture fingerprint result is not OK`() = runTest {
        every { scannerV2.captureFingerprint(any()) } answers {
            (Single.just(CaptureFingerprintResult.FINGERPRINT_NOT_FOUND))
        } andThenAnswer {
            Single.just(CaptureFingerprintResult.DPI_UNSUPPORTED)
        } andThenAnswer {
            Single.just(CaptureFingerprintResult.UNKNOWN_ERROR)
        }

        every { scannerV2.setSmileLedState(any()) } returns Completable.complete()


        // first throws NoFingerDetectedException
        assertThrows<NoFingerDetectedException> {
            scannerWrapper.acquireFingerprintTemplate(
                Dpi(1300),
                1000,
                50,
                false
            )
        }
        // and then throws UnexpectedScannerException
        assertThrows<UnexpectedScannerException> {
            scannerWrapper.acquireFingerprintTemplate(
                Dpi(1300),
                1000,
                50,
                false
            )
        }
        // and then throws UnknownScannerIssueException
        assertThrows<UnknownScannerIssueException> {
            scannerWrapper.acquireFingerprintTemplate(
                Dpi(1300),
                1000,
                50,
                false
            )
        }
    }


    @Test
    fun `should return actual image data in ImageResponse when appropriate image-save strategy is provided and image data is returned from scanner`() =
        runTest {
            val expectedImageResponse = AcquireFingerprintImageResponse(imageBytes = byteArrayOf())
            every { scannerV2.acquireImage(any()) } returns Maybe.just(
                ImageData(expectedImageResponse.imageBytes, 128)
            )

            val actualImageResponse = scannerWrapper.acquireFingerprintImage()

            assertThat(actualImageResponse.imageBytes).isEqualTo(expectedImageResponse.imageBytes)
        }

    @Test
    fun `should throw NoFingerDetectedException when trying to acquire fingerprint image and scanner returns a null ImageData`() =
        runTest {
            every { scannerV2.acquireImage(any()) } returns Maybe.empty()

            assertThrows<NoFingerDetectedException> { scannerWrapper.acquireFingerprintImage() }
        }

    @Test(expected = UnexpectedScannerException::class)
    fun `should throw UnexpectedScannerException when DPI_UNSUPPORTED error is returned during capture`() =
        runTest {
            every {
                scannerV2.captureFingerprint(any())
            } returns Single.just(CaptureFingerprintResult.DPI_UNSUPPORTED)
            every { scannerV2.setSmileLedState(any()) } returns Completable.complete()
            every { scannerV2.getImageQualityScore() } returns Maybe.empty()
            // When
            scannerWrapper.acquireFingerprintTemplate(
                Dpi(1300),
                timeOutMs = 30000,
                qualityThreshold = 7,
                false
            )
        }


    @Test
    fun `should return correct capture response when capture result and image quality are OK`() =
        runTest {
            val qualityThreshold = 50
            val expectedCaptureResponse = AcquireFingerprintTemplateResponse(
                template = byteArrayOf(),
                "ISO_19794_2",
                imageQualityScore = qualityThreshold
            )
            every { scannerV2.getImageQualityScore() } returns Maybe.just(qualityThreshold)
            every { scannerV2.setSmileLedState(any()) } returns Completable.complete()
            every { scannerV2.captureFingerprint(any()) } answers {
                Single.just(CaptureFingerprintResult.OK)
            }
            every { scannerV2.acquireTemplate(any()) } returns Maybe.just(
                TemplateData(
                    expectedCaptureResponse.template
                )
            )

            val actualResponse = scannerWrapper.acquireFingerprintTemplate(
                Dpi(1300),
                1000,
                qualityThreshold,
                false
            )

            assertThat(expectedCaptureResponse.template).isEqualTo(actualResponse.template)
            assertThat(expectedCaptureResponse.imageQualityScore).isEqualTo(actualResponse.imageQualityScore)
        }

    @Test
    fun `should throw NoFingerDetectedException when no fingerprint template is returned after fingerprint is captured`() =
        runTest {
            val qualityThreshold = 50
            every { scannerV2.getImageQualityScore() } returns Maybe.just(qualityThreshold)
            every { scannerV2.setSmileLedState(any()) } returns Completable.complete()
            every { scannerV2.captureFingerprint(any()) } answers {
                (Single.just(CaptureFingerprintResult.OK))
            }
            every { scannerV2.acquireTemplate(any()) } returns Maybe.empty()

            assertThrows<NoFingerDetectedException> {
                scannerWrapper.acquireFingerprintTemplate(
                    Dpi(1300),
                    1000,
                    qualityThreshold,
                    false
                )
            }
        }

    @Test
    fun `should extract template when captured fingerprint's image quality score is less than specified image quality_threshold and allowLowQualityExtraction is true`() =
        runTest {
            val qualityThreshold = 50
            every { scannerV2.getImageQualityScore() } returns Maybe.just(qualityThreshold - 10)
            every { scannerV2.setSmileLedState(any()) } returns Completable.complete()
            every { scannerV2.captureFingerprint(any()) } answers {
                (Single.just(CaptureFingerprintResult.OK))
            }
            every { scannerV2.acquireTemplate(any()) } returns Maybe.just(TemplateData(byteArrayOf()))

            scannerWrapper.acquireFingerprintTemplate(
                Dpi(1300),
                1000,
                qualityThreshold,
                true
            )

            verify(exactly = 1) {scannerV2.acquireTemplate(any())  }
        }

    @Test
    fun `should throw NoFingerDetectedException when captured fingerprint's image quality score is less than no_image quality_threshold`() =
        runTest {
            every { scannerV2.getImageQualityScore() } returns Maybe.empty()
            every { scannerV2.setSmileLedState(any()) } returns Completable.complete()
            every { scannerV2.captureFingerprint(any()) } answers {
                (Single.just(CaptureFingerprintResult.OK))
            }

            assertThrows<NoFingerDetectedException> {
                scannerWrapper.acquireFingerprintTemplate(
                    Dpi(1300),
                    1000,
                    50,
                    false
                )
            }
        }

}
