package com.simprints.fingerprint.scanner.controllers.v2

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.scanner.adapters.v2.toScannerVersion
import com.simprints.fingerprint.scanner.data.local.FirmwareLocalDataSource
import com.simprints.fingerprint.scanner.domain.BatteryInfo
import com.simprints.fingerprint.scanner.domain.ota.AvailableOta
import com.simprints.fingerprint.scanner.domain.versions.ChipFirmwareVersion
import com.simprints.fingerprint.scanner.domain.versions.ScannerFirmwareVersions
import com.simprints.fingerprint.scanner.domain.versions.ScannerVersion
import com.simprints.fingerprint.scanner.exceptions.safe.OtaAvailableException
import com.simprints.fingerprint.tools.BatteryLevelChecker
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.Un20AppVersion
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.StmFirmwareVersion
import com.simprints.fingerprintscanner.v2.domain.root.models.CypressFirmwareVersion
import com.simprints.fingerprintscanner.v2.domain.root.models.UnifiedVersionInformation
import com.simprints.fingerprintscanner.v2.scanner.Scanner
import com.simprints.testtools.common.reactive.advanceTime
import com.simprints.testtools.common.syntax.awaitAndAssertSuccess
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.TestScheduler
import org.junit.Before
import org.junit.Test

class ScannerInitialSetupHelperTest {

    private val scannerMock = mockk<Scanner>()
    private val firmwareFileManagerMock = mockk<FirmwareLocalDataSource>()
    private val connectionHelperMock = mockk<ConnectionHelper>()
    private val batteryLevelChecker = mockk<BatteryLevelChecker>()
    private val testScheduler = TestScheduler()
    private val scannerInitialSetupHelper = ScannerInitialSetupHelper(firmwareFileManagerMock, connectionHelperMock, batteryLevelChecker, testScheduler)

    @Before
    fun setup() {
        every { scannerMock.enterMainMode() } returns Completable.complete()
        every { connectionHelperMock.reconnect(eq(scannerMock), any()) } returns Completable.complete()
    }

    private fun setupScannerWithBatteryInfo(batteryInfo: BatteryInfo) {
        every { scannerMock.getBatteryPercentCharge() } returns Single.just(batteryInfo.charge)
        every { scannerMock.getBatteryVoltageMilliVolts() } returns Single.just(batteryInfo.voltage)
        every { scannerMock.getBatteryCurrentMilliAmps() } returns Single.just(batteryInfo.current)
        every { scannerMock.getBatteryTemperatureDeciKelvin() } returns Single.just(batteryInfo.temperature)
    }

    @Test
    fun ifNoAvailableVersions_completesNormally() {
        every { scannerMock.getVersionInformation() } returns Single.just(SCANNER_VERSION_LOW)
        every { firmwareFileManagerMock.getAvailableScannerFirmwareVersions() } returns ScannerFirmwareVersions.UNKNOWN
        every { batteryLevelChecker.isLowBattery() } returns false
        setupScannerWithBatteryInfo(HIGH_BATTERY_INFO)

        val testSubscriber = scannerInitialSetupHelper.setupScannerWithOtaCheck(scannerMock, MAC_ADDRESS, {}, {}).test()
        testScheduler.advanceTime()

        testSubscriber.awaitAndAssertSuccess()
        verify(exactly = 0) { connectionHelperMock.reconnect(any(), any()) }
    }

    @Test
    fun ifVersionsContainsUnknowns_throwsCorrectOtaAvailableException() {
        every { scannerMock.getVersionInformation() } returns Single.just(SCANNER_VERSION_LOW)
        every { firmwareFileManagerMock.getAvailableScannerFirmwareVersions() } returns SCANNER_VERSION_HIGH.toScannerVersion().firmware.copy(
            stm = ChipFirmwareVersion.UNKNOWN, un20 = ChipFirmwareVersion.UNKNOWN
        )
        every { batteryLevelChecker.isLowBattery() } returns false
        setupScannerWithBatteryInfo(HIGH_BATTERY_INFO)

        val testSubscriber = scannerInitialSetupHelper.setupScannerWithOtaCheck(scannerMock, MAC_ADDRESS, {}, {}).test()
        testScheduler.advanceTime()

        testSubscriber.awaitTerminalEvent()
        testSubscriber.assertError { e ->
            e is OtaAvailableException && e.availableOtas.containsAll(listOf(AvailableOta.CYPRESS))
        }
    }

    @Test
    fun setupScannerWithOtaCheck_savesVersionAndBatteryInfo() {
        every { scannerMock.getVersionInformation() } returns Single.just(SCANNER_VERSION_LOW)
        every { firmwareFileManagerMock.getAvailableScannerFirmwareVersions() } returns ScannerFirmwareVersions.UNKNOWN
        every { batteryLevelChecker.isLowBattery() } returns false
        setupScannerWithBatteryInfo(HIGH_BATTERY_INFO)

        var version: ScannerVersion? = null
        var batteryInfo: BatteryInfo? = null

        val testSubscriber = scannerInitialSetupHelper.setupScannerWithOtaCheck(scannerMock, MAC_ADDRESS, { version = it }, { batteryInfo = it }).test()
        testScheduler.advanceTime()

        testSubscriber.awaitAndAssertSuccess()

        assertThat(version).isEqualTo(SCANNER_VERSION_LOW.toScannerVersion())
        assertThat(batteryInfo).isEqualTo(HIGH_BATTERY_INFO)
    }

    @Test
    fun ifAvailableVersionMatchesExistingVersion_completesNormally() {
        every { scannerMock.getVersionInformation() } returns Single.just(SCANNER_VERSION_LOW)
        every { firmwareFileManagerMock.getAvailableScannerFirmwareVersions() } returns SCANNER_VERSION_LOW.toScannerVersion().firmware
        every { batteryLevelChecker.isLowBattery() } returns false
        setupScannerWithBatteryInfo(HIGH_BATTERY_INFO)

        val testSubscriber = scannerInitialSetupHelper.setupScannerWithOtaCheck(scannerMock, MAC_ADDRESS, {}, {}).test()
        testScheduler.advanceTime()

        testSubscriber.awaitAndAssertSuccess()
    }

    @Test
    fun ifAvailableVersionGreaterThanExistingVersion_throwsOtaAvailableExceptionAndReconnects() {
        every { scannerMock.getVersionInformation() } returns Single.just(SCANNER_VERSION_LOW)
        every { firmwareFileManagerMock.getAvailableScannerFirmwareVersions() } returns SCANNER_VERSION_HIGH.toScannerVersion().firmware
        every { batteryLevelChecker.isLowBattery() } returns false
        setupScannerWithBatteryInfo(HIGH_BATTERY_INFO)

        val testSubscriber = scannerInitialSetupHelper.setupScannerWithOtaCheck(scannerMock, MAC_ADDRESS, {}, {}).test()
        testScheduler.advanceTime()

        testSubscriber.awaitTerminalEvent()
        testSubscriber.assertError { e ->
            e is OtaAvailableException && e.availableOtas.containsAll(listOf(AvailableOta.CYPRESS, AvailableOta.UN20))
        }
        verify { connectionHelperMock.reconnect(eq(scannerMock), any()) }
    }

    @Test
    fun ifAvailableVersionGreaterThanExistingVersion_lowScannerBattery_completesNormally() {
        every { scannerMock.getVersionInformation() } returns Single.just(SCANNER_VERSION_LOW)
        every { firmwareFileManagerMock.getAvailableScannerFirmwareVersions() } returns SCANNER_VERSION_HIGH.toScannerVersion().firmware
        every { batteryLevelChecker.isLowBattery() } returns false
        setupScannerWithBatteryInfo(LOW_BATTERY_INFO)

        val testSubscriber = scannerInitialSetupHelper.setupScannerWithOtaCheck(scannerMock, MAC_ADDRESS, {}, {}).test()
        testScheduler.advanceTime()

        testSubscriber.awaitAndAssertSuccess()
    }

    @Test
    fun ifAvailableVersionGreaterThanExistingVersion_lowPhoneBattery_completesNormally() {
        every { scannerMock.getVersionInformation() } returns Single.just(SCANNER_VERSION_LOW)
        every { firmwareFileManagerMock.getAvailableScannerFirmwareVersions() } returns SCANNER_VERSION_HIGH.toScannerVersion().firmware
        every { batteryLevelChecker.isLowBattery() } returns true
        setupScannerWithBatteryInfo(HIGH_BATTERY_INFO)

        val testSubscriber = scannerInitialSetupHelper.setupScannerWithOtaCheck(scannerMock, MAC_ADDRESS, {}, {}).test()
        testScheduler.advanceTime()

        testSubscriber.awaitAndAssertSuccess()
    }

    @Test
    fun ifAvailableVersionGreaterThanExistingVersion_stillSavesVersionAndBatteryInfo() {
        every { scannerMock.getVersionInformation() } returns Single.just(SCANNER_VERSION_LOW)
        every { firmwareFileManagerMock.getAvailableScannerFirmwareVersions() } returns SCANNER_VERSION_HIGH.toScannerVersion().firmware
        every { batteryLevelChecker.isLowBattery() } returns false
        setupScannerWithBatteryInfo(HIGH_BATTERY_INFO)

        var version: ScannerVersion? = null
        var batteryInfo: BatteryInfo? = null

        val testSubscriber = scannerInitialSetupHelper.setupScannerWithOtaCheck(scannerMock, MAC_ADDRESS, { version = it }, { batteryInfo = it }).test()
        testScheduler.advanceTime()

        testSubscriber.awaitTerminalEvent()
        testSubscriber.assertError { e ->
            e is OtaAvailableException && e.availableOtas.containsAll(listOf(AvailableOta.CYPRESS, AvailableOta.UN20))
        }
        verify { connectionHelperMock.reconnect(eq(scannerMock), any()) }

        assertThat(version).isEqualTo(SCANNER_VERSION_LOW.toScannerVersion())
        assertThat(batteryInfo).isEqualTo(HIGH_BATTERY_INFO)
    }

    companion object {

        const val MAC_ADDRESS = "mac address"

        val HIGH_BATTERY_INFO = BatteryInfo(80, 2, 3, 4)
        val LOW_BATTERY_INFO = BatteryInfo(10, 2, 3, 4)

        val SCANNER_VERSION_LOW = UnifiedVersionInformation(10L,
            CypressFirmwareVersion(12, 13, 14, 15),
            StmFirmwareVersion(1, 2, 3, 4),
            Un20AppVersion(5, 6, 7, 8))

        val SCANNER_VERSION_HIGH = UnifiedVersionInformation(10L,
            CypressFirmwareVersion(12, 13, 14, 16),
            StmFirmwareVersion(1, 2, 3, 4),
            Un20AppVersion(5, 6, 8, 8))
    }
}
