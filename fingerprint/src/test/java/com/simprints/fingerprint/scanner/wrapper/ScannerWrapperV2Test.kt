package com.simprints.fingerprint.scanner.wrapper

import arrow.core.extensions.id.applicative.just
import com.simprints.fingerprint.data.domain.images.SaveFingerprintImagesStrategy
import com.simprints.fingerprint.scanner.controllers.v2.*
import com.simprints.fingerprint.scanner.domain.BatteryInfo
import com.simprints.fingerprint.scanner.domain.ScannerGeneration
import com.simprints.fingerprint.scanner.domain.versions.ScannerFirmwareVersions
import com.simprints.fingerprint.scanner.domain.versions.ScannerVersion
import com.simprints.fingerprint.scanner.exceptions.safe.NoFingerDetectedException
import com.simprints.fingerprint.scanner.exceptions.safe.ScannerDisconnectedException
import com.simprints.fingerprint.scanner.ui.ScannerUiHelper
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.ImageData
import com.simprints.fingerprintscanner.v2.scanner.Scanner
import io.mockk.CapturingSlot
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
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


}
