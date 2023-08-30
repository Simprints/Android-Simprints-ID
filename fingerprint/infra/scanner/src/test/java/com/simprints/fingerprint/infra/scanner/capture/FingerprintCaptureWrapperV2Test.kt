package com.simprints.fingerprint.infra.scanner.capture

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.infra.scanner.domain.fingerprint.AcquireFingerprintImageResponse
import com.simprints.fingerprint.infra.scanner.domain.fingerprint.AcquireFingerprintTemplateResponse
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
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class FingerprintCaptureWrapperV2Test {
    @MockK
    private lateinit var scannerV2: Scanner

    @MockK
    private lateinit var scannerUiHelper: ScannerUiHelper

    private lateinit var scannerWrapper: FingerprintCaptureWrapperV2

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        scannerWrapper =
            FingerprintCaptureWrapperV2(scannerV2, scannerUiHelper, UnconfinedTestDispatcher())
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
                50
            )
        }
        // and then throws UnexpectedScannerException
        assertThrows<UnexpectedScannerException> {
            scannerWrapper.acquireFingerprintTemplate(
                Dpi(1300),
                1000,
                50
            )
        }
        // and then throws UnknownScannerIssueException
        assertThrows<UnknownScannerIssueException> {
            scannerWrapper.acquireFingerprintTemplate(
                Dpi(1300),
                1000,
                50
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
                qualityThreshold = 7
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
                qualityThreshold
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
                    qualityThreshold
                )
            }
        }

    @Test
    fun `should trigger bad_scan LED when captured fingerprint's image quality score is less than specified image quality_threshold`() =
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
                qualityThreshold
            )

            verify(exactly = 1) { scannerUiHelper.badScanLedState() }
        }

    @Test
    fun `should trigger good_scan LED when captured fingerprint's image quality score is greater or equal to specified image quality_threshold`() =
        runTest {
            val qualityThreshold = 50
            every { scannerV2.getImageQualityScore() } returns Maybe.just(qualityThreshold + 10)
            every { scannerV2.setSmileLedState(any()) } returns Completable.complete()
            every { scannerV2.captureFingerprint(any()) } answers {
                (Single.just(CaptureFingerprintResult.OK))
            }
            every { scannerV2.acquireTemplate(any()) } returns Maybe.just(TemplateData(byteArrayOf()))

            scannerWrapper.acquireFingerprintTemplate(
                Dpi(1300),
                1000,
                qualityThreshold
            )

            verify(exactly = 1) { scannerUiHelper.goodScanLedState() }
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
                    50
                )
            }
        }

}
