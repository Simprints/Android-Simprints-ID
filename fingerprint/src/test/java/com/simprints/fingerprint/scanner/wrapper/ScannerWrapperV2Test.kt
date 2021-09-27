package com.simprints.fingerprint.scanner.wrapper


import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.data.domain.fingerprint.CaptureFingerprintStrategy
import com.simprints.fingerprint.data.domain.images.SaveFingerprintImagesStrategy
import com.simprints.fingerprint.scanner.controllers.v2.*
import com.simprints.fingerprint.scanner.domain.AcquireImageResponse
import com.simprints.fingerprint.scanner.domain.CaptureFingerprintResponse
import com.simprints.fingerprint.scanner.domain.ScannerGeneration
import com.simprints.fingerprint.scanner.domain.versions.ScannerApiVersions
import com.simprints.fingerprint.scanner.domain.versions.ScannerFirmwareVersions
import com.simprints.fingerprint.scanner.domain.versions.ScannerVersion
import com.simprints.fingerprint.scanner.exceptions.safe.*
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
import kotlinx.coroutines.launch
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
    fun `should throw the correct corresponding errors when scanner disconnection fails`() = runBlockingTest {
        coEvery { connectionHelper.disconnectScanner(any()) } answers {
            throw ScannerDisconnectedException()
        } andThenThrows(UnexpectedScannerException("Scanner cannot disconnect"))

        assertThrows<ScannerDisconnectedException> { scannerWrapperV2.disconnect() }
        assertThrows<UnexpectedScannerException> { scannerWrapperV2.disconnect() }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should throw the correct corresponding errors when scanner connection fails`() = runBlockingTest {
        coEvery { connectionHelper.connectScanner(any(), any()) } answers {
            throw ScannerDisconnectedException()
        } andThen {
            throw ScannerNotPairedException()
        } andThen {
            throw BluetoothNotEnabledException()
        } andThenThrows(BluetoothNotSupportedException())

        assertThrows<ScannerDisconnectedException> { scannerWrapperV2.connect() }
        assertThrows<ScannerNotPairedException> { scannerWrapperV2.connect() }
        assertThrows<BluetoothNotEnabledException> { scannerWrapperV2.connect() }
        assertThrows<BluetoothNotSupportedException> { scannerWrapperV2.connect() }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should return the correct scanner info when scanner info hasn been set`() = runBlockingTest {
        val expectedVersion = ScannerVersion(
            ScannerGeneration.VERO_2,
            ScannerFirmwareVersions.UNKNOWN,
            ScannerApiVersions.UNKNOWN
        )

        coEvery { scannerInitialSetupHelper.checkScannerInfoAndAvailableOta(any(), any(), any(), any()) } answers {
            val scannerInfoCallback = args[2] as ((ScannerVersion) -> Unit)
            scannerInfoCallback.invoke(expectedVersion)
        }

        scannerWrapperV2.setScannerInfoAndCheckAvailableOta()

        val actualVersion = scannerWrapperV2.versionInformation()

        assertThat(actualVersion).isEqualTo(expectedVersion)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should return the unknown scanner info when scanner info hasn't been set`() = runBlockingTest {
        val expectedVersion = ScannerVersion(
            ScannerGeneration.VERO_2,
            ScannerFirmwareVersions.UNKNOWN,
            ScannerApiVersions.UNKNOWN
        )

        val actualVersion = scannerWrapperV2.versionInformation()

        assertThat(actualVersion).isEqualTo(expectedVersion)
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
    fun `should not turn off(on) the Un20 sensor when sensorShutdown(sensorWakeup) is called and the sensor's current state is off(on)`() = runBlockingTest {
        every { scannerV2.getUn20Status() } returns Single.just(false) andThen Single.just(true)
        every { scannerV2.turnUn20OffAndAwaitStateChangeEvent() } returns Completable.complete()
        every { scannerV2.turnUn20OnAndAwaitStateChangeEvent() } returns Completable.complete()

        scannerWrapperV2.sensorShutDown()
        scannerWrapperV2.sensorWakeUp()

        verify(exactly = 0) {
            scannerV2.turnUn20OffAndAwaitStateChangeEvent()
            scannerV2.turnUn20OnAndAwaitStateChangeEvent()
        }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should turn on the Un20 sensor when sensorWakeup is called and the sensor's current state is off`() = runBlockingTest {
        every { scannerV2.getUn20Status() } returns Single.just(false)
        every { scannerV2.turnUn20OnAndAwaitStateChangeEvent() } returns Completable.complete()

        scannerWrapperV2.sensorWakeUp()

        verify(exactly = 1) { scannerV2.turnUn20OnAndAwaitStateChangeEvent() }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should turn off the Un20 sensor when sensorShutdown is called and the sensor's current state is on`() = runBlockingTest {
        every { scannerV2.getUn20Status() } returns Single.just(true)
        every { scannerV2.turnUn20OffAndAwaitStateChangeEvent() } returns Completable.complete()

        scannerWrapperV2.sensorShutDown()

        verify(exactly = 1) { scannerV2.turnUn20OffAndAwaitStateChangeEvent() }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should complete execution successfully when setting scanner UI to default, when startLiveFeedback is called`() = runBlockingTest {
        every { scannerWrapperV2["isLiveFeedbackAvailable"]() } returns true
        every { scannerV2.setSmileLedState(any()) } returns Completable.complete()
        every { scannerV2.getImageQualityPreview() } returns Maybe.just(50)

        // get the job of the continuous feedback
        val job = launch {
            scannerWrapperV2.startLiveFeedback()
        }

        // force-stop the continuous live feedback
        job.cancel()
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should complete execution successfully when setting scanner UI to default, when stopLiveFeedback is called`() = runBlockingTest {
        every { scannerWrapperV2["isLiveFeedbackAvailable"]() } returns true
        every { scannerV2.setSmileLedState(any()) } returns Completable.complete()
        every { scannerV2.setScannerLedStateDefault() } returns Completable.complete()

        scannerWrapperV2.stopLiveFeedback()
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
