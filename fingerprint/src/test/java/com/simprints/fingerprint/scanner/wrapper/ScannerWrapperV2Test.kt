package com.simprints.fingerprint.scanner.wrapper

import com.simprints.fingerprintscanner.v2.tools.lang.objects
import com.simprints.fingerprint.data.domain.fingerprint.CaptureFingerprintStrategy
import com.simprints.fingerprint.data.domain.images.SaveFingerprintImagesStrategy
import com.simprints.fingerprint.scanner.controllers.v2.*
import com.simprints.fingerprint.scanner.domain.BatteryInfo
import com.simprints.fingerprint.scanner.domain.ScannerGeneration
import com.simprints.fingerprint.scanner.domain.ota.CypressOtaStep
import com.simprints.fingerprint.scanner.domain.ota.StmOtaStep
import com.simprints.fingerprint.scanner.domain.ota.Un20OtaStep
import com.simprints.fingerprint.scanner.domain.versions.ScannerFirmwareVersions
import com.simprints.fingerprint.scanner.domain.versions.ScannerVersion
import com.simprints.fingerprint.scanner.exceptions.safe.NoFingerDetectedException
import com.simprints.fingerprint.scanner.exceptions.safe.ScannerDisconnectedException
import com.simprints.fingerprint.scanner.exceptions.unexpected.UnexpectedScannerException
import com.simprints.fingerprint.scanner.exceptions.unexpected.UnknownScannerIssueException
import com.simprints.fingerprint.scanner.ui.ScannerUiHelper
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.CaptureFingerprintResult
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.ImageData
import com.simprints.fingerprintscanner.v2.exceptions.ota.OtaFailedException
import com.simprints.fingerprintscanner.v2.scanner.Scanner
import com.simprints.testtools.common.syntax.awaitAndAssertSuccess
import io.mockk.CapturingSlot
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.rxkotlin.toObservable
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException

class ScannerWrapperV2Test {

    @MockK private lateinit var scannerV2: Scanner
    @MockK private lateinit var scannerUiHelper: ScannerUiHelper
    private val macAddress: String = "1210120"
    @MockK private lateinit var scannerInitialSetupHelper: ScannerInitialSetupHelper
    @MockK private lateinit var connectionHelper: ConnectionHelper
    @MockK private lateinit var cypressOtaHelper: CypressOtaHelper
    @MockK private lateinit var stmOtaHelper: StmOtaHelper
    @MockK private lateinit var un20OtaHelper: Un20OtaHelper


    private lateinit var scannerWrapper: ScannerWrapperV2

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        scannerWrapper = ScannerWrapperV2(
            scannerV2,
            scannerUiHelper,
            macAddress,
            scannerInitialSetupHelper,
            connectionHelper,
            cypressOtaHelper,
            stmOtaHelper,
            un20OtaHelper
        )
    }


    @Test
    fun shouldReturn_scannerVersion_withEmptyDefaults_inTheCorrectFormat_withNewApiFormat() {
        // Given
        val expectedVersionInfo = ScannerVersion(
            hardwareVersion = "E-1",
            generation = ScannerGeneration.VERO_2,
            firmware = ScannerFirmwareVersions.UNKNOWN
        )

        // When
        val actualVersionInfo = scannerWrapper.versionInformation()

        // Then
        assertEquals(expectedVersionInfo, actualVersionInfo)
    }


    @Test
    fun `should throw NoFingerDetectedException when FINGERPRINT_NOT_FOUND error is returned during capture`() {
        // Given
        every { scannerV2.captureFingerprint(any()) } returns Single.just(CaptureFingerprintResult.FINGERPRINT_NOT_FOUND)
        every { scannerV2.setSmileLedState(any()) } returns Completable.complete()
        every { scannerV2.getImageQualityScore() } returns Maybe.empty()

        // When
        val testObserver = scannerWrapper.captureFingerprint(
            CaptureFingerprintStrategy.SECUGEN_ISO_1300_DPI,
            timeOutMs = 30000,
            qualityThreshold = 7
        ).test()

        // Then
        testObserver.assertError { it is NoFingerDetectedException }
        verify(exactly = 1) { scannerV2.setSmileLedState(scannerUiHelper.badScanLedState()) }
    }

    @Test
    fun `should throw UnexpectedScannerException when DPI_UNSUPPORTED error is returned during capture`() {
        // Given
        every { scannerV2.captureFingerprint(any()) } returns Single.just(CaptureFingerprintResult.DPI_UNSUPPORTED)
        every { scannerV2.setSmileLedState(any()) } returns Completable.complete()
        every { scannerV2.getImageQualityScore() } returns Maybe.empty()

        // When
        val testObserver = scannerWrapper.captureFingerprint(
            CaptureFingerprintStrategy.SECUGEN_ISO_1300_DPI,
            timeOutMs = 30000,
            qualityThreshold = 7
        ).test()

        // Then
        testObserver.assertError { it is UnexpectedScannerException }
    }

    @Test
    fun `should throw UnknownScannerIssueException when UNKNOWN_ERROR error is returned during capture`() {
        // Given
        every { scannerV2.captureFingerprint(any()) } returns Single.just(CaptureFingerprintResult.UNKNOWN_ERROR)
        every { scannerV2.setSmileLedState(any()) } returns Completable.complete()
        every { scannerV2.getImageQualityScore() } returns Maybe.empty()

        // When
        val testObserver = scannerWrapper.captureFingerprint(
            CaptureFingerprintStrategy.SECUGEN_ISO_1300_DPI,
            timeOutMs = 30000,
            qualityThreshold = 7
        ).test()

        // Then
        testObserver.assertError { it is UnknownScannerIssueException }
    }


    @Test
    fun `should complete successfully when connecting to scanner completes without an error`() {
        // given
        every { connectionHelper.connectScanner(scannerV2, any(), any()) } returns Completable.complete()
        // when
        val testObserver = scannerWrapper.connect().test()
        // then
        testObserver.assertComplete()
    }

    @Test
    fun `should return ScannerDisconnectedException when connecting to scanner throws an IOException`() {
        // given
        every { connectionHelper.connectScanner(scannerV2, any(), any()) } returns Completable.error(
            IOException("some error message")
        )
        // when
        val testObserver = scannerWrapper.connect().test()
        // then
        testObserver.assertError {
            it is ScannerDisconnectedException
        }
    }


    @Test
    fun `should have correct batterInfo and scannerVersion that's read when setting up scanner`() {
        // given
        val expectedBatteryInfo = BatteryInfo(
            charge = 70,
            voltage = 24,
            current = 15,
            temperature = 25
        )
        val expectedScannerVersion = ScannerVersion(
            hardwareVersion = "E-1",
            generation = ScannerGeneration.VERO_2,
            firmware = ScannerFirmwareVersions(
                cypress = "1.E-1.0",
                stm = "1.E-1.1",
                un20 = "1.E-1.0"
            )
        )
        val batteryInfoReaderSlot = CapturingSlot<((BatteryInfo) -> Unit)>()
        val scannerVersionReaderSlot = CapturingSlot<((ScannerVersion) -> Unit)>()

        every {
            scannerInitialSetupHelper.setupScannerWithOtaCheck(
                scannerV2,
                macAddress,
                capture(scannerVersionReaderSlot),
                capture(batteryInfoReaderSlot)
            )
        } answers {
            batteryInfoReaderSlot.captured.invoke(expectedBatteryInfo)
            scannerVersionReaderSlot.captured.invoke(expectedScannerVersion)
            Completable.complete()
        }


        // when
        val testObserver = scannerWrapper.setup().test()

        // then
        testObserver.assertComplete()
        assertSame(scannerWrapper.batteryInformation(), expectedBatteryInfo)
        assertSame(scannerWrapper.versionInformation(), expectedScannerVersion)
    }

    @Test
    fun `should turn un20 off and complete successfully when un20 is on and scanner is trigger to turn off un20`() {
        // given
        every { scannerV2.getUn20Status() } returns Single.just(true)
        every { scannerV2.turnUn20OffAndAwaitStateChangeEvent() } returns Completable.complete()

        // when
        val testObserver = scannerWrapper.sensorShutDown().test()

        // then
        testObserver.assertComplete()
        verify(exactly = 1) { scannerV2.turnUn20OffAndAwaitStateChangeEvent() }
    }
    @Test
    fun `should turn un20 on and complete successfully when un20 is off and scanner is trigger to turn on un20`() {
        // given
        every { scannerV2.getUn20Status() } returns Single.just(false)
        every { scannerV2.turnUn20OnAndAwaitStateChangeEvent() } returns Completable.complete()

        // when
        val testObserver = scannerWrapper.sensorWakeUp().test()

        // then
        testObserver.assertComplete()
        verify(exactly = 1) { scannerV2.turnUn20OnAndAwaitStateChangeEvent() }
    }

    @Test
    fun `should ignore calls to sensor changes when the un20 sensor is already in desired state`() {
        // given
        every { scannerV2.getUn20Status() } returns Single.just(false) andThen Single.just(true)


        // when
        val testObserverForSensorShutdown = scannerWrapper.sensorShutDown().test()
        val testObserverForSensorWakeup = scannerWrapper.sensorWakeUp().test()

        // then
        testObserverForSensorShutdown.assertComplete()
        testObserverForSensorWakeup.assertComplete()
        verify(exactly = 0) { scannerV2.turnUn20OffAndAwaitStateChangeEvent() }
        verify(exactly = 0) { scannerV2.turnUn20OnAndAwaitStateChangeEvent() }
    }

    @Test
    fun `should throw IllegalArgumentException when trying to acquire (save) image and the configured strategy value is NEVER`() {
        // when
        val testObserver = scannerWrapper.acquireImage(SaveFingerprintImagesStrategy.NEVER).test()

        // then
        testObserver.assertError {
            it is IllegalArgumentException
        }
    }

    @Test
    fun `should throw NoFingerDetectedException when trying to acquire (save) image and the returned value from the scanner is empty`() {
        // given
        every { scannerV2.acquireImage(any()) } returns Maybe.empty()

        // when
        val testObserver = scannerWrapper.acquireImage(SaveFingerprintImagesStrategy.WSQ_15).test()

        // then
        testObserver.assertError {
            it is NoFingerDetectedException
        }
    }

    @Test
    fun `should return correct AcquiredImageResponse when the scanner extracted image bytes are returned without errors`() {
        // given
        val imageBytes = byteArrayOf(0x23, 0x20, 0x21)
        every { scannerV2.acquireImage(any()) } returns Maybe.just(
            ImageData(image = imageBytes, crcValue = 5)
        )

        // when
        val testObserver = scannerWrapper.acquireImage(SaveFingerprintImagesStrategy.WSQ_15_EAGER).test()

        // then
        testObserver.assertComplete()
        assertEquals(testObserver.values().first().imageBytes, imageBytes)
    }

    @Test
    fun `should throw OtaFailedException when trying to perform ota without available scanner hardwareVersion`() {
        // when
        val cypressTestObserver = scannerWrapper.performCypressOta("1.E-1.0").test()
        val stmTestObserver = scannerWrapper.performStmOta("1.E-1.0").test()
        val un20TestObserver = scannerWrapper.performUn20Ota("1.E-1.0").test()


        // then
        cypressTestObserver.assertError { it is OtaFailedException }
        stmTestObserver.assertError { it is OtaFailedException }
        un20TestObserver.assertError { it is OtaFailedException }
    }

    @Test
    fun `should return corresponding Ota response from ota helper classes when scanner hardwareVersion is available`() {
        // Given
        val expectedScannerVersion = ScannerVersion(
            hardwareVersion = "E-1",
            generation = ScannerGeneration.VERO_2,
            firmware = ScannerFirmwareVersions(
                cypress = "1.E-1.0",
                stm = "1.E-1.1",
                un20 = "1.E-1.0"
            )
        )
        val scannerVersionReaderSlot = CapturingSlot<((ScannerVersion) -> Unit)>()
        every {
            scannerInitialSetupHelper.setupScannerWithOtaCheck(
                scannerV2,
                macAddress,
                capture(scannerVersionReaderSlot),
                any()
            )
        } answers {
            scannerVersionReaderSlot.captured.invoke(expectedScannerVersion)
            Completable.complete()
        }

        val cypressOtaSteps = (CypressOtaStep::class.objects())
        val stmOtaSteps = (StmOtaStep::class.objects())
        val un20OtaSteps = (Un20OtaStep::class.objects())

        every { cypressOtaHelper.performOtaSteps(scannerV2, any(), any()) } returns cypressOtaSteps.toObservable()
        every { stmOtaHelper.performOtaSteps(scannerV2, any(), any()) } returns stmOtaSteps.toObservable()
        every { un20OtaHelper.performOtaSteps(scannerV2, any(), any()) } returns un20OtaSteps.toObservable()
        // initialize scanner version
        scannerWrapper.setup()


        // When
        val cypressTestObserver = scannerWrapper.performCypressOta("1.E-1.2").test()
        val stmTestObserver = scannerWrapper.performStmOta("1.E-1.2").test()
        val un20TestObserver = scannerWrapper.performUn20Ota("1.E-1.2").test()

        // Then
        cypressTestObserver.awaitAndAssertSuccess()
        stmTestObserver.awaitAndAssertSuccess()
        un20TestObserver.awaitAndAssertSuccess()

        assertEquals(cypressTestObserver.values(), cypressOtaSteps)
        assertEquals(stmTestObserver.values(), stmOtaSteps)
        assertEquals(un20TestObserver.values(), un20OtaSteps)
    }

}
