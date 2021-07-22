package com.simprints.fingerprint.scanner.controllers.v2

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.scanner.adapters.v2.toScannerVersion
import com.simprints.fingerprint.scanner.data.local.FirmwareLocalDataSource
import com.simprints.fingerprint.scanner.domain.BatteryInfo
import com.simprints.fingerprint.scanner.domain.ota.AvailableOta
import com.simprints.fingerprint.scanner.domain.ota.DownloadableFirmwareVersion
import com.simprints.fingerprint.scanner.domain.versions.ScannerFirmwareVersions
import com.simprints.fingerprint.scanner.domain.versions.ScannerHardwareRevisions
import com.simprints.fingerprint.scanner.domain.versions.ScannerVersion
import com.simprints.fingerprint.scanner.exceptions.safe.OtaAvailableException
import com.simprints.fingerprint.tools.BatteryLevelChecker
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.Un20AppVersion
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.Un20ExtendedAppVersion
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.StmExtendedFirmwareVersion
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.StmFirmwareVersion
import com.simprints.fingerprintscanner.v2.domain.root.models.*
import com.simprints.fingerprintscanner.v2.scanner.Scanner
import com.simprints.testtools.common.reactive.advanceTime
import com.simprints.testtools.common.syntax.awaitAndAssertSuccess
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.TestScheduler
import org.junit.Before
import org.junit.Test

class ScannerInitialSetupHelperTest {

    private val scannerMock = mockk<Scanner>()
    private val connectionHelperMock = mockk<ConnectionHelper>()
    private val batteryLevelChecker = mockk<BatteryLevelChecker>()
    private val fingerprintPreferenceManager= mockk<FingerprintPreferencesManager>()
    private val firmwareLocalDataSource= mockk<FirmwareLocalDataSource>()
    private val testScheduler = TestScheduler()
    private val scannerInitialSetupHelper = ScannerInitialSetupHelper(
        connectionHelperMock,
        batteryLevelChecker,
        fingerprintPreferenceManager,
        firmwareLocalDataSource,
        testScheduler
    )

    @Before
    fun setup() {
        every { firmwareLocalDataSource.getAvailableScannerFirmwareVersions() } returns LOCAL_SCANNER_VERSION
        every { scannerMock.enterMainMode() } returns Completable.complete()
        coEvery {
            connectionHelperMock.reconnect(
                eq(scannerMock),
                any()
            )
        } answers {}
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
        every { fingerprintPreferenceManager.scannerHardwareRevisions } returns ScannerHardwareRevisions()
        every { batteryLevelChecker.isLowBattery() } returns false
        setupScannerWithBatteryInfo(HIGH_BATTERY_INFO)

        val testSubscriber =
            scannerInitialSetupHelper.setupScannerWithOtaCheck(scannerMock, MAC_ADDRESS, {}, {})
                .test()
        testScheduler.advanceTime()

        testSubscriber.awaitAndAssertSuccess()
        coVerify(exactly = 0) { connectionHelperMock.reconnect(any(), any()) }
    }

    @Test
    fun ifVersionsContainsUnknowns_throwsCorrectOtaAvailableException() {
        every { scannerMock.getVersionInformation() } returns Single.just(SCANNER_VERSION_LOW)
        every { fingerprintPreferenceManager.scannerHardwareRevisions } returns ScannerHardwareRevisions().apply {
            put(
                HARDWARE_VERSION,
                SCANNER_VERSION_HIGH.toScannerVersion().firmware.copy(
                    stm = ScannerFirmwareVersions.UNKNOWN_VERSION,
                    un20 = ScannerFirmwareVersions.UNKNOWN_VERSION
                )
            )
        }
        every { batteryLevelChecker.isLowBattery() } returns false
        setupScannerWithBatteryInfo(HIGH_BATTERY_INFO)

        val testSubscriber =
            scannerInitialSetupHelper.setupScannerWithOtaCheck(scannerMock, MAC_ADDRESS, {}, {})
                .test()
        testScheduler.advanceTime()

        testSubscriber.awaitTerminalEvent()
        testSubscriber.assertError { e ->
            e is OtaAvailableException && e.availableOtas.containsAll(listOf(AvailableOta.CYPRESS))
        }
    }

    @Test
    fun setupScannerWithOtaCheck_savesVersionAndBatteryInfo() {
        every { scannerMock.getVersionInformation() } returns Single.just(SCANNER_VERSION_LOW)
        every { fingerprintPreferenceManager.scannerHardwareRevisions } returns ScannerHardwareRevisions().apply {
            put(HARDWARE_VERSION, ScannerFirmwareVersions.UNKNOWN)
        }
        every { batteryLevelChecker.isLowBattery() } returns false
        setupScannerWithBatteryInfo(HIGH_BATTERY_INFO)

        var version: ScannerVersion? = null
        var batteryInfo: BatteryInfo? = null

        val testSubscriber = scannerInitialSetupHelper.setupScannerWithOtaCheck(
            scannerMock,
            MAC_ADDRESS,
            { version = it },
            { batteryInfo = it }).test()
        testScheduler.advanceTime()

        testSubscriber.awaitAndAssertSuccess()

        assertThat(version).isEqualTo(SCANNER_VERSION_LOW.toScannerVersion())
        assertThat(batteryInfo).isEqualTo(HIGH_BATTERY_INFO)
    }

    @Test
    fun ifAvailableVersionMatchesExistingVersion_completesNormally() {
        every { scannerMock.getVersionInformation() } returns Single.just(SCANNER_VERSION_LOW)
        every { fingerprintPreferenceManager.scannerHardwareRevisions } returns ScannerHardwareRevisions().apply {
            put(HARDWARE_VERSION, SCANNER_VERSION_LOW.toScannerVersion().firmware)
        }
        every { batteryLevelChecker.isLowBattery() } returns false
        setupScannerWithBatteryInfo(HIGH_BATTERY_INFO)

        val testSubscriber =
            scannerInitialSetupHelper.setupScannerWithOtaCheck(scannerMock, MAC_ADDRESS, {}, {})
                .test()
        testScheduler.advanceTime()

        testSubscriber.awaitAndAssertSuccess()
    }

    @Test
    fun ifAvailableVersionGreaterThanExistingVersion_throwsOtaAvailableExceptionAndReconnects() {
        every { scannerMock.getVersionInformation() } returns Single.just(SCANNER_VERSION_LOW)
        every { fingerprintPreferenceManager.scannerHardwareRevisions } returns ScannerHardwareRevisions().apply {
            put(HARDWARE_VERSION, SCANNER_VERSION_HIGH.toScannerVersion().firmware)
        }

        every { batteryLevelChecker.isLowBattery() } returns false
        setupScannerWithBatteryInfo(HIGH_BATTERY_INFO)

        val testSubscriber =
            scannerInitialSetupHelper.setupScannerWithOtaCheck(scannerMock, MAC_ADDRESS, {}, {})
                .test()
        testScheduler.advanceTime()

        testSubscriber.awaitTerminalEvent()
        testSubscriber.assertError { e ->
            e is OtaAvailableException && e.availableOtas.containsAll(
                listOf(
                    AvailableOta.CYPRESS,
                    AvailableOta.UN20
                )
            )
        }
        coVerify { connectionHelperMock.reconnect(eq(scannerMock), any()) }
    }

    @Test
    fun ifAvailableVersionGreaterThanExistingVersion_noLocalDownloadedFirmwares_completesNormally() {
        every { firmwareLocalDataSource.getAvailableScannerFirmwareVersions() } returns mapOf()

        every { scannerMock.getVersionInformation() } returns Single.just(SCANNER_VERSION_LOW)
        every { fingerprintPreferenceManager.scannerHardwareRevisions } returns ScannerHardwareRevisions().apply {
            put(HARDWARE_VERSION, SCANNER_VERSION_HIGH.toScannerVersion().firmware)
        }

        every { batteryLevelChecker.isLowBattery() } returns false
        setupScannerWithBatteryInfo(HIGH_BATTERY_INFO)

        val testSubscriber =
            scannerInitialSetupHelper.setupScannerWithOtaCheck(scannerMock, MAC_ADDRESS, {}, {})
                .test()
        testScheduler.advanceTime()

        testSubscriber.awaitAndAssertSuccess()

    }
    @Test
    fun ifAvailableVersionGreaterThanExistingVersion_lowScannerBattery_completesNormally() {
        every { scannerMock.getVersionInformation() } returns Single.just(SCANNER_VERSION_LOW)
        every { fingerprintPreferenceManager.scannerHardwareRevisions } returns ScannerHardwareRevisions().apply {
            put(HARDWARE_VERSION, SCANNER_VERSION_HIGH.toScannerVersion().firmware)
        }
        every { batteryLevelChecker.isLowBattery() } returns false
        setupScannerWithBatteryInfo(LOW_BATTERY_INFO)

        val testSubscriber =
            scannerInitialSetupHelper.setupScannerWithOtaCheck(scannerMock, MAC_ADDRESS, {}, {})
                .test()
        testScheduler.advanceTime()

        testSubscriber.awaitAndAssertSuccess()
    }

    @Test
    fun ifAvailableVersionGreaterThanExistingVersion_lowPhoneBattery_completesNormally() {
        every { scannerMock.getVersionInformation() } returns Single.just(SCANNER_VERSION_LOW)
        every { fingerprintPreferenceManager.scannerHardwareRevisions } returns ScannerHardwareRevisions().apply {
            put(HARDWARE_VERSION, SCANNER_VERSION_HIGH.toScannerVersion().firmware)
        }
        every { batteryLevelChecker.isLowBattery() } returns true
        setupScannerWithBatteryInfo(HIGH_BATTERY_INFO)

        val testSubscriber =
            scannerInitialSetupHelper.setupScannerWithOtaCheck(scannerMock, MAC_ADDRESS, {}, {})
                .test()
        testScheduler.advanceTime()

        testSubscriber.awaitAndAssertSuccess()
    }

    @Test
    fun ifAvailableVersionGreaterThanExistingVersion_stillSavesVersionAndBatteryInfo() {
        every { scannerMock.getVersionInformation() } returns Single.just(SCANNER_VERSION_LOW)
        every { fingerprintPreferenceManager.scannerHardwareRevisions } returns ScannerHardwareRevisions().apply {
            put(HARDWARE_VERSION, SCANNER_VERSION_HIGH.toScannerVersion().firmware)
        }
        every { batteryLevelChecker.isLowBattery() } returns false
        setupScannerWithBatteryInfo(HIGH_BATTERY_INFO)

        var version: ScannerVersion? = null
        var batteryInfo: BatteryInfo? = null

        val testSubscriber = scannerInitialSetupHelper.setupScannerWithOtaCheck(
            scannerMock,
            MAC_ADDRESS,
            { version = it },
            { batteryInfo = it }).test()
        testScheduler.advanceTime()

        testSubscriber.awaitTerminalEvent()
        testSubscriber.assertError { e ->
            e is OtaAvailableException && e.availableOtas.containsAll(
                listOf(
                    AvailableOta.CYPRESS,
                    AvailableOta.UN20
                )
            )
        }
        coVerify { connectionHelperMock.reconnect(eq(scannerMock), any()) }

        assertThat(version).isEqualTo(SCANNER_VERSION_LOW.toScannerVersion())
        assertThat(batteryInfo).isEqualTo(HIGH_BATTERY_INFO)
    }

    companion object {

        const val MAC_ADDRESS = "mac address"
        private const val HARDWARE_VERSION = "E-1"

        val HIGH_BATTERY_INFO = BatteryInfo(80, 2, 3, 4)
        val LOW_BATTERY_INFO = BatteryInfo(10, 2, 3, 4)

        private val STM_VERSION_STRING = ( "14.E-1.16")
        private val CYPRESS_VERSION_STRING = ( "3.E-1.4")
        private val UN20_VERSION_STRING = ("7.E-1.8")

        private val STM_VERSION = StmExtendedFirmwareVersion( STM_VERSION_STRING)
        private val CYPRESS_VERSION = CypressExtendedFirmwareVersion( CYPRESS_VERSION_STRING)
        private val UN20_VERSION = Un20ExtendedAppVersion(UN20_VERSION_STRING)


        val SCANNER_VERSION_LOW = ScannerInformation(
            hardwareVersion = HARDWARE_VERSION,
            firmwareVersions = UnifiedVersionInformation(
                10L,
                CypressFirmwareVersion(12, 13, 14, 15),
                StmFirmwareVersion(1, 2, 3, 2),
                Un20AppVersion(5, 6, 7, 2)
            ).toExtendedVersionInfo()
        )


        val LOCAL_SCANNER_VERSION =  mapOf(
            DownloadableFirmwareVersion.Chip.CYPRESS to setOf(CYPRESS_VERSION_STRING),
            DownloadableFirmwareVersion.Chip.STM to setOf(STM_VERSION_STRING),
            DownloadableFirmwareVersion.Chip.UN20 to setOf(UN20_VERSION_STRING)
        )
        val SCANNER_VERSION_HIGH = ScannerInformation(
            hardwareVersion = HARDWARE_VERSION,
            firmwareVersions = ExtendedVersionInformation(
                CYPRESS_VERSION,
                STM_VERSION,
                UN20_VERSION
            )
        )
    }
}
