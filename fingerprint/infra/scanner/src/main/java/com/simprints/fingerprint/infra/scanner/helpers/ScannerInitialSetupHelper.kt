package com.simprints.fingerprint.infra.scanner.helpers

import com.simprints.fingerprint.infra.scanner.data.local.FirmwareLocalDataSource
import com.simprints.fingerprint.infra.scanner.domain.BatteryInfo
import com.simprints.fingerprint.infra.scanner.domain.ota.AvailableOta
import com.simprints.fingerprint.infra.scanner.domain.ota.DownloadableFirmwareVersion.Chip
import com.simprints.fingerprint.infra.scanner.domain.versions.ScannerFirmwareVersions
import com.simprints.fingerprint.infra.scanner.domain.versions.ScannerVersion
import com.simprints.fingerprint.infra.scanner.exceptions.safe.OtaAvailableException
import com.simprints.fingerprint.infra.scanner.tools.BatteryLevelChecker
import com.simprints.fingerprint.infra.scanner.v2.scanner.Scanner
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.config.store.models.Vero2Configuration
import com.simprints.infra.config.sync.ConfigManager
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * For handling the initial setup to the scanner upon connection, such as retrieving and checking
 * the firmware version and battery level, and determining whether OTA is necessary.
 */
internal class ScannerInitialSetupHelper @Inject constructor(
    private val connectionHelper: ConnectionHelper,
    private val batteryLevelChecker: BatteryLevelChecker,
    private val configManager: ConfigManager,
    private val firmwareLocalDataSource: FirmwareLocalDataSource,
) {
    private lateinit var scannerVersion: ScannerVersion

    /**
     * This method is responsible for checking if any firmware updates are available, the current
     * scanner firmware version [ScannerVersion] and the current battery information [BatteryInfo].
     *
     * @param scanner  the scanner object to read battery and version info from
     * @param macAddress  the mac address of the scanner, for establishing connection if disconnected
     * @param withBatteryInfo  the callback that receives the retrieved [BatteryInfo] to the calling function
     * @param withScannerVersion  the callback that receives the retrieved [ScannerVersion] to the calling function
     *
     * @throws OtaAvailableException If an OTA update is available and the battery is sufficiently charged.
     */
    suspend fun setupScannerWithOtaCheck(
        fingerprintSdk: FingerprintConfiguration.BioSdk,
        scanner: Scanner,
        macAddress: String,
        withScannerVersion: (ScannerVersion) -> Unit,
        withBatteryInfo: (BatteryInfo) -> Unit,
    ) {
        delay(100) // Speculatively needed
        val unifiedVersionInfo = scanner.getVersionInformation()
        unifiedVersionInfo.toScannerVersion().also {
            withScannerVersion(it)
            scannerVersion = it
        }

        scanner.enterMainMode()
        delay(100) // Speculatively needed
        val batteryInfo = getBatteryInfo(scanner, withBatteryInfo)
        ifAvailableOtasPrepareScannerThenThrow(
            fingerprintSdk,
            scannerVersion.hardwareVersion,
            scanner,
            macAddress,
            batteryInfo,
        )
    }

    private suspend fun getBatteryInfo(
        scanner: Scanner,
        withBatteryInfo: (BatteryInfo) -> Unit,
    ): BatteryInfo {
        val batteryPercent = scanner.getBatteryPercentCharge()
        val batteryVoltage = scanner.getBatteryVoltageMilliVolts()
        val batteryMilliAmps = scanner.getBatteryCurrentMilliAmps()
        val batteryTemperature = scanner.getBatteryTemperatureDeciKelvin()

        return BatteryInfo(
            batteryPercent,
            batteryVoltage,
            batteryMilliAmps,
            batteryTemperature,
        ).also {
            withBatteryInfo(it)
        }
    }

    private suspend fun ifAvailableOtasPrepareScannerThenThrow(
        fingerprintSdk: FingerprintConfiguration.BioSdk,
        hardwareVersion: String,
        scanner: Scanner,
        macAddress: String,
        batteryInfo: BatteryInfo,
    ) {
        val configuredVersions = configManager
            .getProjectConfiguration()
            .fingerprint
            ?.getSdkConfiguration(fingerprintSdk)
            ?.vero2
            ?.firmwareVersions
            ?.get(hardwareVersion)
        val availableOtas = determineAvailableOtas(scannerVersion.firmware, configuredVersions)
        val requiresOtaUpdate = availableOtas.isNotEmpty() &&
            !batteryInfo.isLowBattery() &&
            !batteryLevelChecker.isLowBattery()

        if (requiresOtaUpdate) {
            connectionHelper.reconnect(scanner, macAddress)
            throw OtaAvailableException(availableOtas)
        }
    }

    private suspend fun determineAvailableOtas(
        current: ScannerFirmwareVersions,
        configured: Vero2Configuration.Vero2FirmwareVersions?,
    ): List<AvailableOta> {
        if (configured == null) {
            return emptyList()
        }
        val localFiles = firmwareLocalDataSource.getAvailableScannerFirmwareVersions()
        return listOfNotNull(
            if (
                localFiles[Chip.CYPRESS]?.contains(configured.cypress) == true &&
                current.cypress != configured.cypress
            ) {
                AvailableOta.CYPRESS
            } else {
                null
            },
            if (localFiles[Chip.STM]?.contains(configured.stm) == true &&
                current.stm != configured.stm
            ) {
                AvailableOta.STM
            } else {
                null
            },
            if (localFiles[Chip.UN20]?.contains(configured.un20) == true &&
                current.un20 != configured.un20
            ) {
                AvailableOta.UN20
            } else {
                null
            },
        )
    }
}
