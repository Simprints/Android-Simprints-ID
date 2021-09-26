package com.simprints.fingerprint.scanner.wrapper


import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.data.domain.fingerprint.CaptureFingerprintStrategy
import com.simprints.fingerprint.data.domain.images.SaveFingerprintImagesStrategy
import com.simprints.fingerprint.scanner.controllers.v2.*
import com.simprints.fingerprint.scanner.domain.AcquireImageResponse
import com.simprints.fingerprint.scanner.domain.CaptureFingerprintResponse
import com.simprints.fingerprint.scanner.exceptions.safe.NoFingerDetectedException
import com.simprints.fingerprint.scanner.exceptions.unexpected.UnavailableVero2FeatureException
import com.simprints.fingerprint.scanner.exceptions.unexpected.UnexpectedScannerException
import com.simprints.fingerprint.scanner.exceptions.unexpected.UnknownScannerIssueException
import com.simprints.fingerprint.scanner.ui.ScannerUiHelper
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.CaptureFingerprintResult
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.ImageData
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.TemplateData
import com.simprints.fingerprintscanner.v2.scanner.Scanner
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.spyk
import io.mockk.verify
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

class ScannerWrapperV2Test {

    @MockK lateinit var scannerV2: Scanner
    @MockK lateinit var scannerUiHelper: ScannerUiHelper
    @MockK lateinit var scannerInitialSetupHelper: ScannerInitialSetupHelper
    @MockK lateinit var connectionHelper: ConnectionHelper
    @MockK lateinit var cypressOtaHelper: CypressOtaHelper
    @MockK lateinit var stmOtaHelper: StmOtaHelper
    @MockK lateinit var un20OtaHelper: Un20OtaHelper

    private lateinit var scannerWrapperV2: ScannerWrapperV2


    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        scannerWrapperV2 = spyk(ScannerWrapperV2(
            scannerV2,
            scannerUiHelper,
            "Mock mac address",
            scannerInitialSetupHelper,
            connectionHelper,
            cypressOtaHelper,
            stmOtaHelper,
            un20OtaHelper
        ),
        recordPrivateCalls = true)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should return actual image data in ImageResponse when appropriate image-save strategy is provided and image data is returned from scanner`() = runBlockingTest {
        val expectedImageResponse = AcquireImageResponse(imageBytes = byteArrayOf())
        every { scannerV2.acquireImage(any()) } returns Maybe.just(
            ImageData(expectedImageResponse.imageBytes, 128)
        )

        val actualImageResponse = scannerWrapperV2.acquireImage(
            SaveFingerprintImagesStrategy.WSQ_15
        )

        assertThat(actualImageResponse.imageBytes).isEqualTo(expectedImageResponse.imageBytes)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should throw NoFingerDetectedException when trying to acquire fingerprint image and scanner returns a null ImageData`() = runBlockingTest {
        every { scannerV2.acquireImage(any()) } returns Maybe.empty()

        assertThrows<NoFingerDetectedException> {
            scannerWrapperV2.acquireImage(
                SaveFingerprintImagesStrategy.WSQ_15
            )
        }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should throw UnexpectedScannerException when trying to acquire fingerprint image and save fingerprint strategy is NEVER`() = runBlockingTest {
        assertThrows<UnexpectedScannerException> {
            scannerWrapperV2.acquireImage(
                SaveFingerprintImagesStrategy.NEVER
            )
        }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should return correct capture response when capture result and image quality are OK`() = runBlockingTest {
        val qualityThreshold = 50
        val expectedCaptureResponse = CaptureFingerprintResponse(
            template = byteArrayOf(),
            imageQualityScore = qualityThreshold
        )
        every { scannerV2.getImageQualityScore() } returns Maybe.just(qualityThreshold)
        every { scannerV2.setSmileLedState(any()) } returns Completable.complete()
        every { scannerV2.captureFingerprint(any()) } answers {
            (Single.just(CaptureFingerprintResult.OK))
        }
        every { scannerV2.acquireTemplate(any()) } returns Maybe.just(TemplateData(expectedCaptureResponse.template))



        val actualResponse = scannerWrapperV2.captureFingerprint(
            CaptureFingerprintStrategy.SECUGEN_ISO_1300_DPI,
            1000,
            qualityThreshold
        )

        assertThat(expectedCaptureResponse.template).isEqualTo(actualResponse.template)
        assertThat(expectedCaptureResponse.imageQualityScore).isEqualTo(actualResponse.imageQualityScore)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should throw NoFingerDetectedException when no fingerprint template is returned after fingerprint is captured`() = runBlockingTest {
        val qualityThreshold = 50
        every { scannerV2.getImageQualityScore() } returns Maybe.just(qualityThreshold)
        every { scannerV2.setSmileLedState(any()) } returns Completable.complete()
        every { scannerV2.captureFingerprint(any()) } answers {
            (Single.just(CaptureFingerprintResult.OK))
        }
        every { scannerV2.acquireTemplate(any()) } returns Maybe.empty()


        assertThrows<NoFingerDetectedException> {
            scannerWrapperV2.captureFingerprint(
                CaptureFingerprintStrategy.SECUGEN_ISO_1300_DPI,
                1000,
                qualityThreshold
            )
        }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should trigger bad_scan LED when captured fingerprint's image quality score is less than specified image quality_threshold`() = runBlockingTest {
        val qualityThreshold = 50
        every { scannerV2.getImageQualityScore() } returns Maybe.just(qualityThreshold - 10)
        every { scannerV2.setSmileLedState(any()) } returns Completable.complete()
        every { scannerV2.captureFingerprint(any()) } answers {
            (Single.just(CaptureFingerprintResult.OK))
        }
        every { scannerV2.acquireTemplate(any()) } returns Maybe.just(TemplateData(byteArrayOf()))

        scannerWrapperV2.captureFingerprint(
            CaptureFingerprintStrategy.SECUGEN_ISO_1300_DPI,
            1000,
            qualityThreshold
        )

        verify(exactly = 1) { scannerUiHelper.badScanLedState() }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should trigger good_scan LED when captured fingerprint's image quality score is greater or equal to specified image quality_threshold`() = runBlockingTest {
        val qualityThreshold = 50
        every { scannerV2.getImageQualityScore() } returns Maybe.just(qualityThreshold + 10)
        every { scannerV2.setSmileLedState(any()) } returns Completable.complete()
        every { scannerV2.captureFingerprint(any()) } answers {
            (Single.just(CaptureFingerprintResult.OK))
        }
        every { scannerV2.acquireTemplate(any()) } returns Maybe.just(TemplateData(byteArrayOf()))

        scannerWrapperV2.captureFingerprint(
            CaptureFingerprintStrategy.SECUGEN_ISO_1300_DPI,
            1000,
            qualityThreshold
        )

        verify(exactly = 1) { scannerUiHelper.goodScanLedState() }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should throw NoFingerDetectedException when captured fingerprint's image quality score is less than no_image quality_threshold`() = runBlockingTest {
        every { scannerV2.getImageQualityScore() } returns Maybe.empty()
        every { scannerV2.setSmileLedState(any()) } returns Completable.complete()
        every { scannerV2.captureFingerprint(any()) } answers {
            (Single.just(CaptureFingerprintResult.OK))
        }

        assertThrows<NoFingerDetectedException> {
            scannerWrapperV2.captureFingerprint(
                CaptureFingerprintStrategy.SECUGEN_ISO_1300_DPI,
                1000,
                50
            )
        }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should throw corresponding errors when capture fingerprint result is not OK`() = runBlockingTest {
        every { scannerV2.captureFingerprint(any()) } answers {
            (Single.just(CaptureFingerprintResult.FINGERPRINT_NOT_FOUND))
        } andThen {
            Single.just(CaptureFingerprintResult.DPI_UNSUPPORTED)
        } andThen {
            Single.just(CaptureFingerprintResult.UNKNOWN_ERROR)
        }

        every { scannerV2.setSmileLedState(any()) } returns Completable.complete()
        every { scannerWrapperV2["isLiveFeedbackAvailable"]() } returns true


        // first throws NoFingerDetectedException
        assertThrows<NoFingerDetectedException> {
            scannerWrapperV2.captureFingerprint(
                CaptureFingerprintStrategy.SECUGEN_ISO_1300_DPI,
                1000,
                50
            )
        }
        // and then throws UnexpectedScannerException
        assertThrows<UnexpectedScannerException> {
            scannerWrapperV2.captureFingerprint(
                CaptureFingerprintStrategy.SECUGEN_ISO_1300_DPI,
                1000,
                50
            )
        }
        // and then throws UnknownScannerIssueException
        assertThrows<UnknownScannerIssueException> {
            scannerWrapperV2.captureFingerprint(
                CaptureFingerprintStrategy.SECUGEN_ISO_1300_DPI,
                1000,
                50
            )
        }
    }


    @Test
    @ExperimentalCoroutinesApi
    fun `should throw UnavailableVero2FeatureException when startLiveFeedback is called and live feedback is not available`() = runBlockingTest {
        every { scannerWrapperV2["isLiveFeedbackAvailable"]() } returns false

        assertThrows<UnavailableVero2FeatureException> {
            scannerWrapperV2.startLiveFeedback()
        }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should throw UnavailableVero2FeatureException when stopLiveFeedback is called and live feedback is not available`() = runBlockingTest {
        every { scannerWrapperV2["isLiveFeedbackAvailable"]() } returns false

        assertThrows<UnavailableVero2FeatureException> {
            scannerWrapperV2.stopLiveFeedback()
        }
    }


}
