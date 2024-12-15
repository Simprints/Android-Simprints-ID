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
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FingerprintCaptureWrapperV2Test {
    @MockK
    private lateinit var scannerV2: Scanner

    private lateinit var scannerWrapper: FingerprintCaptureWrapperV2

    @MockK
    private lateinit var tracker: FingerprintScanningStatusTracker

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        scannerWrapper = FingerprintCaptureWrapperV2(scannerV2, tracker)
    }

    @Test
    fun `test acquireImageDistortionMatrixConfiguration success`() = runTest {
        val expectedResp = byteArrayOf(1, 2, 3)
        coEvery { scannerV2.acquireImageDistortionConfigurationMatrix() } returns expectedResp

        val actualResponse = scannerWrapper.acquireImageDistortionMatrixConfiguration()
        assertThat(actualResponse).isEqualTo(expectedResp)
    }

    @Test
    fun `test acquireUnprocessedImage success`() = runTest {
        // Given
        val imageData = ImageData(
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
            1,
        )
        coEvery { scannerV2.captureFingerprint(any()) } returns CaptureFingerprintResult.OK
        coEvery { scannerV2.acquireUnprocessedImage(any()) } returns imageData
        // When
        val actualResponse = scannerWrapper.acquireUnprocessedImage(Dpi(500))
        // Then
        assertThat(actualResponse.rawUnprocessedImage.imageData).isEqualTo(
            RawUnprocessedImage(
                imageData.image,
            ).imageData,
        )
    }

    @Test(expected = NoFingerDetectedException::class)
    fun `test acquireUnprocessedImage throws NoFingerDetectedException when scanner returns empty`() = runTest {
        // Given
        coEvery { scannerV2.acquireUnprocessedImage(any()) } returns null
        coEvery { scannerV2.captureFingerprint(any()) } returns CaptureFingerprintResult.OK
        // When
        scannerWrapper.acquireUnprocessedImage(Dpi(500))
        // Then throw NoFingerDetectedException
    }

    @Test
    fun `should throw illegal argument exception when capture DPI is null`() = runTest {
        assertThrows<UnexpectedScannerException> {
            scannerWrapper.acquireFingerprintTemplate(null, 1000, 50, false)
        }
    }

    @Test
    fun `should throw illegal argument exception when capture DPI is less than 500`() = runTest {
        assertThrows<UnexpectedScannerException> {
            scannerWrapper.acquireFingerprintTemplate(Dpi(499), 1000, 50, false)
        }
    }

    @Test
    fun `should throw illegal argument exception when capture DPI is greater than 1700`() = runTest {
        assertThrows<UnexpectedScannerException> {
            scannerWrapper.acquireFingerprintTemplate(Dpi(1701), 1000, 50, false)
        }
    }

    @Test
    fun `should throw corresponding errors when capture fingerprint result is not OK`() = runTest {
        coEvery { scannerV2.captureFingerprint(any()) } answers {
            (CaptureFingerprintResult.FINGERPRINT_NOT_FOUND)
        } andThenAnswer {
            CaptureFingerprintResult.DPI_UNSUPPORTED
        } andThenAnswer {
            CaptureFingerprintResult.UNKNOWN_ERROR
        }

        coJustRun { scannerV2.setSmileLedState(any()) }

        // first throws NoFingerDetectedException
        assertThrows<NoFingerDetectedException> {
            scannerWrapper.acquireFingerprintTemplate(
                Dpi(1300),
                1000,
                50,
                false,
            )
        }
        // and then throws UnexpectedScannerException
        assertThrows<UnexpectedScannerException> {
            scannerWrapper.acquireFingerprintTemplate(
                Dpi(1300),
                1000,
                50,
                false,
            )
        }
        // and then throws UnknownScannerIssueException
        assertThrows<UnknownScannerIssueException> {
            scannerWrapper.acquireFingerprintTemplate(
                Dpi(1300),
                1000,
                50,
                false,
            )
        }
    }

    @Test
    fun `should return actual image data in ImageResponse when appropriate image-save strategy is provided and image data is returned from scanner`() =
        runTest {
            val expectedImageResponse = AcquireFingerprintImageResponse(imageBytes = byteArrayOf())
            coEvery { scannerV2.acquireImage(any()) } returns ImageData(
                expectedImageResponse.imageBytes,
                128,
            )

            val actualImageResponse = scannerWrapper.acquireFingerprintImage()
            assertThat(actualImageResponse.imageBytes).isEqualTo(expectedImageResponse.imageBytes)
        }

    @Test
    fun `should throw NoFingerDetectedException when trying to acquire fingerprint image and scanner returns a null ImageData`() = runTest {
        coEvery { scannerV2.acquireImage(any()) } returns null
        assertThrows<NoFingerDetectedException> { scannerWrapper.acquireFingerprintImage() }
    }

    @Test(expected = UnexpectedScannerException::class)
    fun `should throw UnexpectedScannerException when DPI_UNSUPPORTED error is returned during capture`() = runTest {
        coEvery {
            scannerV2.captureFingerprint(any())
        } returns CaptureFingerprintResult.DPI_UNSUPPORTED
        coJustRun { scannerV2.setSmileLedState(any()) }
        coEvery { scannerV2.getImageQualityScore() } returns null
        // When
        scannerWrapper.acquireFingerprintTemplate(
            Dpi(1300),
            timeOutMs = 30000,
            qualityThreshold = 7,
            false,
        )
    }

    @Test
    fun `should return correct capture response when capture result and image quality are OK`() = runTest {
        val qualityThreshold = 50
        val expectedCaptureResponse = AcquireFingerprintTemplateResponse(
            template = byteArrayOf(),
            "ISO_19794_2",
            imageQualityScore = qualityThreshold,
        )
        coEvery { scannerV2.getImageQualityScore() } returns qualityThreshold
        coJustRun { scannerV2.setSmileLedState(any()) }
        coEvery { scannerV2.captureFingerprint(any()) } answers {
            CaptureFingerprintResult.OK
        }
        coEvery { scannerV2.acquireTemplate(any()) } returns TemplateData(
            expectedCaptureResponse.template,
        )

        val actualResponse = scannerWrapper.acquireFingerprintTemplate(
            Dpi(1300),
            1000,
            qualityThreshold,
            false,
        )

        assertThat(expectedCaptureResponse.template).isEqualTo(actualResponse.template)
        assertThat(expectedCaptureResponse.imageQualityScore).isEqualTo(actualResponse.imageQualityScore)
    }

    @Test
    fun `should throw NoFingerDetectedException when no fingerprint template is returned after fingerprint is captured`() = runTest {
        val qualityThreshold = 50
        coEvery { scannerV2.getImageQualityScore() } returns qualityThreshold
        coJustRun { scannerV2.setSmileLedState(any()) }
        coEvery { scannerV2.captureFingerprint(any()) } answers {
            (CaptureFingerprintResult.OK)
        }
        coEvery { scannerV2.acquireTemplate(any()) } returns null

        assertThrows<NoFingerDetectedException> {
            scannerWrapper.acquireFingerprintTemplate(
                Dpi(1300),
                1000,
                qualityThreshold,
                false,
            )
        }
    }

    @Test
    fun `should extract template when captured fingerprint's image quality score is less than specified image quality_threshold and allowLowQualityExtraction is true`() =
        runTest {
            val qualityThreshold = 50
            coEvery { scannerV2.getImageQualityScore() } returns qualityThreshold - 10
            coJustRun { scannerV2.setSmileLedState(any()) }
            coEvery { scannerV2.captureFingerprint(any()) } answers {
                (CaptureFingerprintResult.OK)
            }
            coEvery { scannerV2.acquireTemplate(any()) } returns TemplateData(byteArrayOf())

            scannerWrapper.acquireFingerprintTemplate(
                Dpi(1300),
                1000,
                qualityThreshold,
                true,
            )

            coVerify(exactly = 1) { scannerV2.acquireTemplate(any()) }
        }

    @Test
    fun `should throw NoFingerDetectedException when captured fingerprint's image quality score is less than no_image quality_threshold`() =
        runTest {
            coEvery { scannerV2.getImageQualityScore() } returns null
            coJustRun { scannerV2.setSmileLedState(any()) }
            coEvery { scannerV2.captureFingerprint(any()) } returns (CaptureFingerprintResult.OK)

            assertThrows<NoFingerDetectedException> {
                scannerWrapper.acquireFingerprintTemplate(
                    Dpi(1300),
                    1000,
                    50,
                    false,
                )
            }
        }
}
