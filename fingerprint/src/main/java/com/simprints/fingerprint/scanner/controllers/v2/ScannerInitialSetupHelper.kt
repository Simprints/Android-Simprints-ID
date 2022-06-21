package com.simprints.fingerprint.scanner.controllers.v2

import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.scanner.adapters.v2.toScannerVersion
import com.simprints.fingerprint.scanner.data.local.FirmwareLocalDataSource
import com.simprints.fingerprint.scanner.domain.BatteryInfo
import com.simprints.fingerprint.scanner.domain.ota.AvailableOta
import com.simprints.fingerprint.scanner.domain.ota.DownloadableFirmwareVersion.Chip
import com.simprints.fingerprint.scanner.domain.versions.ScannerFirmwareVersions
import com.simprints.fingerprint.scanner.domain.versions.ScannerVersion
import com.simprints.fingerprint.scanner.exceptions.safe.OtaAvailableException
import com.simprints.fingerprint.tools.BatteryLevelChecker
import com.simprints.fingerprintscanner.v2.scanner.Scanner
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * For handling the initial setup to the scanner upon connection, such as retrieving and checking
 * the firmware version and battery level, and determining whether OTA is necessary.
 */
class ScannerInitialSetupHelper(
    private val connectionHelper: ConnectionHelper,
    private val batteryLevelChecker: BatteryLevelChecker,
    private val fingerprintPreferenceManager: FingerprintPreferencesManager,
    private val firmwareLocalDataSource: FirmwareLocalDataSource,
    private val timeScheduler: Scheduler = Schedulers.io()
) {

    private lateinit var scannerVersion: ScannerVersion

    /**
     * @throws OtaAvailableException If an OTA update is available and the battery is sufficiently charged.
     */
    fun setupScannerWithOtaCheck(
        scanner: Scanner,
        macAddress: String,
        withScannerVersion: (ScannerVersion) -> Unit,
        withBatteryInfo: (BatteryInfo) -> Unit
    ): Completable =
        Completable.complete()
            .delay(100, TimeUnit.MILLISECONDS, timeScheduler) // Speculatively needed
            .andThen(scanner.getVersionInformation().doOnSuccess { scannerVersionInfo ->
                scannerVersionInfo.toScannerVersion().also {
                    withScannerVersion(it)
                    scannerVersion = it
                }
            })
            .flatMapCompletable { scanner.enterMainMode() }
            .delay(100, TimeUnit.MILLISECONDS, timeScheduler) // Speculatively needed
            .andThen(getBatteryInfo(scanner, withBatteryInfo))
            .flatMapCompletable { ifAvailableOtasPrepareScannerThenThrow(scannerVersion.hardwareVersion, scanner, macAddress, it) }

    private fun getBatteryInfo(scanner: Scanner, withBatteryInfo: (BatteryInfo) -> Unit): Single<BatteryInfo> =
        Singles.zip(
            scanner.getBatteryPercentCharge(),
            scanner.getBatteryVoltageMilliVolts(),
            scanner.getBatteryCurrentMilliAmps(),
            scanner.getBatteryTemperatureDeciKelvin()
        ) { charge, voltage, current, temperature ->
            BatteryInfo(charge, voltage, current, temperature).also { withBatteryInfo(it) }
        }

    private fun ifAvailableOtasPrepareScannerThenThrow(hardwareVersion: String, scanner: Scanner, macAddress: String, batteryInfo: BatteryInfo): Completable {
        val availableVersions = fingerprintPreferenceManager.scannerHardwareRevisions[hardwareVersion]
        val availableOtas = determineAvailableOtas(scannerVersion.firmware, availableVersions)
        return if (availableOtas.isEmpty() || batteryInfo.isLowBattery() || batteryLevelChecker.isLowBattery()) {
            Completable.complete()
        } else {
            connectionHelper.reconnect(scanner, macAddress)
                .andThen(Completable.error(OtaAvailableException(availableOtas)))
        }
    }

    private fun determineAvailableOtas(
        current: ScannerFirmwareVersions,
        available: ScannerFirmwareVersions?
    ): List<AvailableOta> {
        if (available == null) {
            return emptyList()
        }
        val localFiles = firmwareLocalDataSource.getAvailableScannerFirmwareVersions()
        return listOfNotNull(
            if (
                localFiles[Chip.CYPRESS]?.contains(available.cypress) == true
                && current.cypress != available.cypress
            ) AvailableOta.CYPRESS else null,
            if (localFiles[Chip.STM]?.contains(available.stm) == true &&
                current.stm != available.stm
            ) AvailableOta.STM else null,
            if (localFiles[Chip.UN20]?.contains(available.un20) == true &&
                current.un20 != available.un20
            ) AvailableOta.UN20 else null
        )
    }
}
