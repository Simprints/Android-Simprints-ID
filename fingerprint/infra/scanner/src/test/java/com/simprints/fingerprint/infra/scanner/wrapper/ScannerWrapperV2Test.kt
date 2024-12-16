package com.simprints.fingerprint.infra.scanner.wrapper

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.infra.scanner.domain.BatteryInfo
import com.simprints.fingerprint.infra.scanner.domain.ScannerGeneration
import com.simprints.fingerprint.infra.scanner.domain.versions.ScannerFirmwareVersions
import com.simprints.fingerprint.infra.scanner.domain.versions.ScannerVersion
import com.simprints.fingerprint.infra.scanner.exceptions.safe.BluetoothNotEnabledException
import com.simprints.fingerprint.infra.scanner.exceptions.safe.BluetoothNotSupportedException
import com.simprints.fingerprint.infra.scanner.exceptions.safe.ScannerDisconnectedException
import com.simprints.fingerprint.infra.scanner.exceptions.safe.ScannerNotPairedException
import com.simprints.fingerprint.infra.scanner.exceptions.unexpected.UnavailableVero2FeatureException
import com.simprints.fingerprint.infra.scanner.exceptions.unexpected.UnexpectedScannerException
import com.simprints.fingerprint.infra.scanner.helpers.ConnectionHelper
import com.simprints.fingerprint.infra.scanner.helpers.ScannerInitialSetupHelper
import com.simprints.fingerprint.infra.scanner.v2.exceptions.state.NotConnectedException
import com.simprints.fingerprint.infra.scanner.v2.scanner.Scanner
import com.simprints.fingerprint.infra.scanner.v2.scanner.ScannerExtendedInfoReaderHelper
import com.simprints.fingerprint.infra.scanner.v2.tools.ScannerUiHelper
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
internal class ScannerWrapperV2Test {
    @MockK
    lateinit var scannerV2: Scanner

    @MockK
    lateinit var scannerUiHelper: ScannerUiHelper

    @MockK
    lateinit var scannerInitialSetupHelper: ScannerInitialSetupHelper

    @MockK
    lateinit var connectionHelper: ConnectionHelper

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
                UnconfinedTestDispatcher(),
            ),
            recordPrivateCalls = true,
        )
    }

    @Test
    fun `should throw the correct corresponding errors when scanner disconnection fails`() = runTest {
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
                any(),
                any(),
            )
        } answers {
            val scannerInfoCallback = args[3] as ((ScannerVersion) -> Unit)
            scannerInfoCallback.invoke(expectedVersion)
            val batteryInfoCallback = args[4] as ((BatteryInfo) -> Unit)
            batteryInfoCallback.invoke(expectedBatteryInfo)
        }

        scannerWrapper.setScannerInfoAndCheckAvailableOta(mockk())

        val actualVersion = scannerWrapper.versionInformation()

        assertThat(actualVersion).isEqualTo(expectedVersion)
        assertThat(scannerWrapper.batteryInformation()).isEqualTo(expectedBatteryInfo)
    }

    @Test(expected = UnexpectedScannerException::class)
    fun `should throw UnexpectedScannerException if setupScannerWithOtaCheck throws IllegalStateException`() = runTest {
        coEvery {
            scannerInitialSetupHelper.setupScannerWithOtaCheck(
                any(),
                any(),
                any(),
                any(),
                any(),
            )
        } throws IllegalStateException()
        scannerWrapper.setScannerInfoAndCheckAvailableOta(mockk())
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
    fun `should throw UnavailableVero2FeatureException when startLiveFeedback is called and live feedback is not available`() = runTest {
        every { scannerWrapper["isLiveFeedbackAvailable"]() } returns false

        assertThrows<UnavailableVero2FeatureException> {
            scannerWrapper.startLiveFeedback()
        }
    }

    @Test
    fun `should not turn off(on) the Un20 sensor when sensorShutdown(sensorWakeup) is called and the sensor's current state is off(on)`() =
        runTest {
            coEvery { scannerV2.getUn20Status() } returns false andThen true
            coJustRun { scannerV2.turnUn20Off() }
            coJustRun { scannerV2.turnUn20On() }
            scannerWrapper.sensorShutDown()
            scannerWrapper.sensorWakeUp()

            coVerify(exactly = 0) {
                scannerV2.turnUn20Off()
                scannerV2.turnUn20On()
            }
        }

    @Test
    fun `should turn on the Un20 sensor when sensorWakeup is called and the sensor's current state is off`() = runTest {
        coEvery { scannerV2.getUn20Status() } returns false
        coJustRun { scannerV2.turnUn20On() }

        scannerWrapper.sensorWakeUp()

        coVerify(exactly = 1) { scannerV2.turnUn20On() }
    }

    @Test(expected = ScannerDisconnectedException::class)
    fun `should throw ScannerDisconnectedException if getUn20Status throws IO`() = runTest {
        coEvery { scannerV2.getUn20Status() } throws IOException("")
        scannerWrapper.sensorWakeUp()
    }

    @Test(expected = ScannerDisconnectedException::class)
    fun `should throw ScannerDisconnectedException if getUn20Status throws NotConnectedException`() = runTest {
        coEvery { scannerV2.getUn20Status() } throws NotConnectedException("")
        scannerWrapper.sensorWakeUp()
    }

    @Test
    fun `should turn off the Un20 sensor when sensorShutdown is called and the sensor's current state is on`() = runTest {
        coEvery { scannerV2.getUn20Status() } returns true
        coJustRun { scannerV2.turnUn20Off() }

        scannerWrapper.sensorShutDown()

        coVerify(exactly = 1) { scannerV2.turnUn20Off() }
    }

    @Test(expected = UnexpectedScannerException::class)
    fun `should throw UnexpectedScannerException when sensorShutdown throws IllegalArgumentException`() = runTest {
        coEvery { scannerV2.getUn20Status() } throws IllegalArgumentException()
        scannerWrapper.sensorShutDown()
    }

    @Test
    fun `should complete execution successfully when setting scanner UI to default, when startLiveFeedback is called`() = runTest {
        every { scannerWrapper.isLiveFeedbackAvailable() } returns true
        coJustRun { scannerV2.setSmileLedState(any()) }
        coEvery { scannerV2.getImageQualityPreview() } returns 50

        // get the job of the continuous feedback
        val job = launch {
            scannerWrapper.startLiveFeedback()
        }

        advanceTimeBy(500)

        // force-stop the continuous live feedback
        job.cancel()
    }

    @Test
    fun `should complete execution successfully when startLiveFeedback is called and scanner disconnects`() = runTest {
        every { scannerWrapper.isLiveFeedbackAvailable() } returns true
        coEvery { scannerV2.setSmileLedState(any()) } throws IOException()
        coEvery { scannerV2.getImageQualityPreview() } returns 50

        // get the job of the continuous feedback
        val job = launch {
            scannerWrapper.startLiveFeedback()
        }

        advanceTimeBy(500)

        // force-stop the continuous live feedback
        job.cancel()
    }

    @Test
    fun `should complete execution successfully when setting scanner UI to default, when stopLiveFeedback is called`() = runTest {
        every { scannerWrapper.isLiveFeedbackAvailable() } returns true
        coJustRun { scannerV2.setSmileLedState(any()) }
        coJustRun { scannerV2.setScannerLedStateDefault() }

        scannerWrapper.stopLiveFeedback()
    }

    @Test
    fun `should throw UnavailableVero2FeatureException when stopLiveFeedback is called and live feedback is not available`() = runTest {
        every { scannerWrapper.isLiveFeedbackAvailable() } returns false

        assertThrows<UnavailableVero2FeatureException> {
            scannerWrapper.stopLiveFeedback()
        }
    }

    @Test
    fun `test imageTransfer should be supported in v2 scanners`() {
        assertThat(scannerWrapper.isImageTransferSupported()).isTrue()
    }

    @Test
    fun `isConnected() correctly passes scanner connection status`() {
        coEvery { scannerV2.isConnected() } returns false
        assertThat(scannerWrapper.isConnected()).isFalse()

        coEvery { scannerV2.isConnected() } returns true
        assertThat(scannerWrapper.isConnected()).isTrue()
    }

    @Test
    fun `should set smile leds to flashing white when calling turnFlashingOrangeLeds`() = runTest {
        coJustRun { scannerV2.setSmileLedState(any()) }

        scannerWrapper.turnOnFlashingWhiteSmileLeds()

        coVerify { scannerV2.setSmileLedState(scannerUiHelper.whiteFlashingLedState()) }
    }

    @Test
    fun `should set smile leds to good scan  when calling setUiBadCapture`() = runTest {
        coJustRun { scannerV2.setSmileLedState(any()) }

        scannerWrapper.setUiGoodCapture()

        coVerify { scannerV2.setSmileLedState(scannerUiHelper.goodScanLedState()) }
    }

    @Test
    fun `should set smile leds to bad scan  when calling setUiBadCapture`() = runTest {
        coJustRun { scannerV2.setSmileLedState(any()) }

        scannerWrapper.setUiBadCapture()

        coVerify { scannerV2.setSmileLedState(scannerUiHelper.badScanLedState()) }
    }
}
