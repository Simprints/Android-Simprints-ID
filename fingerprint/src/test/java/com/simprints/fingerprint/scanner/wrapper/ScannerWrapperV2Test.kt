package com.simprints.fingerprint.scanner.wrapper


import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.data.domain.fingerprint.CaptureFingerprintStrategy
import com.simprints.fingerprint.data.domain.images.SaveFingerprintImagesStrategy
import com.simprints.fingerprint.scanner.controllers.v2.*
import com.simprints.fingerprint.scanner.domain.AcquireImageResponse
import com.simprints.fingerprint.scanner.domain.BatteryInfo
import com.simprints.fingerprint.scanner.domain.CaptureFingerprintResponse
import com.simprints.fingerprint.scanner.domain.ScannerGeneration
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
import com.simprints.fingerprintscanner.v2.exceptions.ota.OtaFailedException
import com.simprints.fingerprintscanner.v2.scanner.Scanner
import com.simprints.fingerprintscanner.v2.scanner.ScannerExtendedInfoReaderHelper
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.IOException

class ScannerWrapperV2Test {

    @MockK
    lateinit var scannerV2: Scanner
    @MockK
    lateinit var scannerUiHelper: ScannerUiHelper
    @MockK
    lateinit var scannerInitialSetupHelper: ScannerInitialSetupHelper
    @MockK
    lateinit var connectionHelper: ConnectionHelper
    @MockK
    lateinit var cypressOtaHelper: CypressOtaHelper
    @MockK
    lateinit var stmOtaHelper: StmOtaHelper
    @MockK
    lateinit var un20OtaHelper: Un20OtaHelper

    private lateinit var scannerWrapper: ScannerWrapperV2


    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        scannerWrapper = spyk(
            ScannerWrapperV2(
                scannerV2,
                scannerUiHelper,
                "Mock mac address",
                scannerInitialSetupHelper,
                connectionHelper,
                cypressOtaHelper,
                stmOtaHelper,
                un20OtaHelper,
                UnconfinedTestDispatcher(),
            ),
            recordPrivateCalls = true
        )
    }

    @Test
    fun `should throw the correct corresponding errors when scanner disconnection fails`() =
        runTest {
            coEvery { connectionHelper.disconnectScanner(any()) } answers {
                throw ScannerDisconnectedException()
            } andThenThrows (UnexpectedScannerException("Scanner cannot disconnect"))

            assertThrows<ScannerDisconnectedException> { scannerWrapper.disconnect() }
            assertThrows<UnexpectedScannerException> { scannerWrapper.disconnect() }
        }

    @Test
    fun `should success scanner disconnection completes`() = runTest {
        coEvery { connectionHelper.disconnectScanner(any()) } just Runs
        scannerWrapper.disconnect()
    }

    @Test
    fun `should throw the correct corresponding errors when scanner connection fails`() = runTest {
        coEvery { connectionHelper.connectScanner(any(), any()) } answers {
            throw ScannerDisconnectedException()
        } andThenAnswer {
            throw ScannerNotPairedException()
        } andThenAnswer {
            throw BluetoothNotEnabledException()
        } andThenThrows (BluetoothNotSupportedException())

        assertThrows<ScannerDisconnectedException> { scannerWrapper.connect() }
        assertThrows<ScannerNotPairedException> { scannerWrapper.connect() }
        assertThrows<BluetoothNotEnabledException> { scannerWrapper.connect() }
        assertThrows<BluetoothNotSupportedException> { scannerWrapper.connect() }
    }

    @Test(expected = Test.None::class)
    fun `should complete if no connection errors `() = runTest {
        coEvery { connectionHelper.connectScanner(any(), any()) } answers {
            flowOf()
        }
        scannerWrapper.connect()
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `should return the correct scanner info when scanner info has been set`() = runTest {
        val expectedVersion = mockk<ScannerVersion>()
        val expectedBatteryInfo = mockk<BatteryInfo>()

        coEvery {
            scannerInitialSetupHelper.setupScannerWithOtaCheck(
                any(),
                any(),
                any(),
                any()
            )
        } answers {
            val scannerInfoCallback = args[2] as ((ScannerVersion) -> Unit)
            scannerInfoCallback.invoke(expectedVersion)
            val batteryInfoCallback = args[3] as ((BatteryInfo) -> Unit)
            batteryInfoCallback.invoke(expectedBatteryInfo)
        }

        scannerWrapper.setScannerInfoAndCheckAvailableOta()

        val actualVersion = scannerWrapper.versionInformation()

        assertThat(actualVersion).isEqualTo(expectedVersion)
        assertThat(scannerWrapper.batteryInformation()).isEqualTo(expectedBatteryInfo)
    }

    @Test(expected = UnexpectedScannerException::class)
    fun `should throw UnexpectedScannerException if setupScannerWithOtaCheck throws IllegalStateException`() =
        runTest {
            coEvery {
                scannerInitialSetupHelper.setupScannerWithOtaCheck(any(), any(), any(), any())
            } throws IllegalStateException()
            scannerWrapper.setScannerInfoAndCheckAvailableOta()
        }


    @Test
    fun `should return the unknown scanner info when scanner info hasn't been set`() = runTest {
        val expectedVersion = ScannerVersion(
            ScannerExtendedInfoReaderHelper.UNKNOWN_HARDWARE_VERSION,
            ScannerGeneration.VERO_2,
            ScannerFirmwareVersions.UNKNOWN,
        )

        val actualVersion = scannerWrapper.versionInformation()

        assertThat(actualVersion).isEqualTo(expectedVersion)
    }

    @Test
    fun `should return actual image data in ImageResponse when appropriate image-save strategy is provided and image data is returned from scanner`() =
        runTest {
            val expectedImageResponse = AcquireImageResponse(imageBytes = byteArrayOf())
            every { scannerV2.acquireImage(any()) } returns Maybe.just(
                ImageData(expectedImageResponse.imageBytes, 128)
            )

            val actualImageResponse = scannerWrapper.acquireImage(
                SaveFingerprintImagesStrategy.WSQ_15
            )

            assertThat(actualImageResponse.imageBytes).isEqualTo(expectedImageResponse.imageBytes)
        }

    @Test
    fun `should throw NoFingerDetectedException when trying to acquire fingerprint image and scanner returns a null ImageData`() =
        runTest {
            every { scannerV2.acquireImage(any()) } returns Maybe.empty()

            assertThrows<NoFingerDetectedException> {
                scannerWrapper.acquireImage(
                    SaveFingerprintImagesStrategy.WSQ_15
                )
            }
        }

    @Test(expected = IllegalArgumentException::class)
    fun `should throw UnexpectedScannerException when trying to acquire fingerprint image and save fingerprint strategy is NEVER`() =
        runTest {
            scannerWrapper.acquireImage(
                SaveFingerprintImagesStrategy.NEVER
            )

        }

    @Test(expected = UnexpectedScannerException::class)
    fun `should throw UnexpectedScannerException when DPI_UNSUPPORTED error is returned during capture`() =
        runTest {
            every { scannerV2.captureFingerprint(any()) } returns Single.just(
                CaptureFingerprintResult.DPI_UNSUPPORTED
            )
            every { scannerV2.setSmileLedState(any()) } returns Completable.complete()
            every { scannerV2.getImageQualityScore() } returns Maybe.empty()
            // When
            scannerWrapper.captureFingerprint(
                CaptureFingerprintStrategy.SECUGEN_ISO_1300_DPI,
                timeOutMs = 30000,
                qualityThreshold = 7
            )
        }


    @Test
    fun `should return correct capture response when capture result and image quality are OK`() =
        runTest {
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
            every { scannerV2.acquireTemplate(any()) } returns Maybe.just(
                TemplateData(
                    expectedCaptureResponse.template
                )
            )


            val actualResponse = scannerWrapper.captureFingerprint(
                CaptureFingerprintStrategy.SECUGEN_ISO_1300_DPI,
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
                scannerWrapper.captureFingerprint(
                    CaptureFingerprintStrategy.SECUGEN_ISO_1300_DPI,
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

            scannerWrapper.captureFingerprint(
                CaptureFingerprintStrategy.SECUGEN_ISO_1300_DPI,
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

            scannerWrapper.captureFingerprint(
                CaptureFingerprintStrategy.SECUGEN_ISO_1300_DPI,
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
                scannerWrapper.captureFingerprint(
                    CaptureFingerprintStrategy.SECUGEN_ISO_1300_DPI,
                    1000,
                    50
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
        every { scannerWrapper["isLiveFeedbackAvailable"]() } returns true


        // first throws NoFingerDetectedException
        assertThrows<NoFingerDetectedException> {
            scannerWrapper.captureFingerprint(
                CaptureFingerprintStrategy.SECUGEN_ISO_1300_DPI,
                1000,
                50
            )
        }
        // and then throws UnexpectedScannerException
        assertThrows<UnexpectedScannerException> {
            scannerWrapper.captureFingerprint(
                CaptureFingerprintStrategy.SECUGEN_ISO_1300_DPI,
                1000,
                50
            )
        }
        // and then throws UnknownScannerIssueException
        assertThrows<UnknownScannerIssueException> {
            scannerWrapper.captureFingerprint(
                CaptureFingerprintStrategy.SECUGEN_ISO_1300_DPI,
                1000,
                50
            )
        }
    }


    @Test
    fun `should throw UnavailableVero2FeatureException when startLiveFeedback is called and live feedback is not available`() =
        runTest {
            every { scannerWrapper["isLiveFeedbackAvailable"]() } returns false

            assertThrows<UnavailableVero2FeatureException> {
                scannerWrapper.startLiveFeedback()
            }
        }

    @Test
    fun `should not turn off(on) the Un20 sensor when sensorShutdown(sensorWakeup) is called and the sensor's current state is off(on)`() =
        runTest {
            every { scannerV2.getUn20Status() } returns Single.just(false) andThen Single.just(true)
            every { scannerV2.turnUn20OffAndAwaitStateChangeEvent() } returns Completable.complete()
            every { scannerV2.turnUn20OnAndAwaitStateChangeEvent() } returns Completable.complete()

            scannerWrapper.sensorShutDown()
            scannerWrapper.sensorWakeUp()

            verify(exactly = 0) {
                scannerV2.turnUn20OffAndAwaitStateChangeEvent()
                scannerV2.turnUn20OnAndAwaitStateChangeEvent()
            }
        }

    @Test
    fun `should turn on the Un20 sensor when sensorWakeup is called and the sensor's current state is off`() =
        runTest {
            every { scannerV2.getUn20Status() } returns Single.just(false)
            every { scannerV2.turnUn20OnAndAwaitStateChangeEvent() } returns Completable.complete()

            scannerWrapper.sensorWakeUp()

            verify(exactly = 1) { scannerV2.turnUn20OnAndAwaitStateChangeEvent() }
        }

    @Test(expected = ScannerDisconnectedException::class)
    fun `should throw ScannerDisconnectedException if getUn20Status throws IO`() = runTest {
        every { scannerV2.getUn20Status() } throws IOException("")
        scannerWrapper.sensorWakeUp()
    }


    @Test
    fun `should turn off the Un20 sensor when sensorShutdown is called and the sensor's current state is on`() =
        runTest {
            every { scannerV2.getUn20Status() } returns Single.just(true)
            every { scannerV2.turnUn20OffAndAwaitStateChangeEvent() } returns Completable.complete()

            scannerWrapper.sensorShutDown()

            verify(exactly = 1) { scannerV2.turnUn20OffAndAwaitStateChangeEvent() }
        }

    @Test(expected = UnexpectedScannerException::class)
    fun `should throw UnexpectedScannerException when sensorShutdown throws IllegalArgumentException`() =
        runTest {
            coEvery { scannerV2.getUn20Status() } throws IllegalArgumentException()
            scannerWrapper.sensorShutDown()

        }

    @Test
    fun `should complete execution successfully when setting scanner UI to default, when startLiveFeedback is called`() =
        runTest {
            every { scannerWrapper.isLiveFeedbackAvailable() } returns true
            every { scannerV2.setSmileLedState(any()) } returns Completable.complete()
            every { scannerV2.getImageQualityPreview() } returns Maybe.just(50)

        // get the job of the continuous feedback
        val job = launch {
            scannerWrapper.startLiveFeedback()
        }

        // force-stop the continuous live feedback
        job.cancel()
    }

    @Test
    fun `should complete execution successfully when setting scanner UI to default, when stopLiveFeedback is called`() =
        runTest {
            every { scannerWrapper.isLiveFeedbackAvailable() } returns true
            every { scannerV2.setSmileLedState(any()) } returns Completable.complete()
            every { scannerV2.setScannerLedStateDefault() } returns Completable.complete()

            scannerWrapper.stopLiveFeedback()
        }

    @Test
    fun `should throw UnavailableVero2FeatureException when stopLiveFeedback is called and live feedback is not available`() =
        runTest {
            every { scannerWrapper.isLiveFeedbackAvailable() } returns false

            assertThrows<UnavailableVero2FeatureException> {
                scannerWrapper.stopLiveFeedback()
            }
        }

    @Test
    fun `test imageTransfer should be supported in v2 scanners`() {
        assertThat(scannerWrapper.isImageTransferSupported()).isTrue()
    }

    @Test (expected = OtaFailedException::class    )
    fun `should throw OtaFailedException if performOtaSteps throws `()= runTest {
        every { cypressOtaHelper.performOtaSteps(any(),any(),any()) } throws OtaFailedException()
        scannerWrapper.performCypressOta("")
    }
}
