package com.simprints.feature.troubleshooting.overview.usecase

import com.simprints.fingerprint.infra.scanner.ScannerManager
import com.simprints.infra.config.store.ConfigRepository
import javax.inject.Inject

internal class CollectScannerStateUseCase @Inject constructor(
    private val configRepository: ConfigRepository,
    private val scannerManager: ScannerManager,
) {
    suspend operator fun invoke(): String {
        try {
            val sdk = configRepository
                .getProjectConfiguration()
                .fingerprint
                ?.allowedSDKs
                ?.firstOrNull() // Just need any to trigger hardware version fetch
                ?: return "Fingerprint not configured"

            // Doing bare minimum connection to collect data from the scanner
            scannerManager.initScanner()
            scannerManager.scanner.connect()
            scannerManager.scanner.setScannerInfoAndCheckAvailableOta(sdk)
            val isConnected = scannerManager.isScannerConnected
            val scannerId = scannerManager.currentScannerId
            val scannerMac = scannerManager.currentMacAddress
            val version = scannerManager.scanner.versionInformation()
            val battery = scannerManager.scanner.batteryInformation()
            scannerManager.scanner.disconnect()

            return """
                Connected: $isConnected
                Scanner ID : $scannerId ($scannerMac)
                Version: ${version.generation} ${version.hardwareVersion}
                Firmware: cypress=${version.firmware.cypress} stm=${version.firmware.stm} un20=${version.firmware.un20}
                Battery: ${battery.charge}% ${battery.voltage}mV ${battery.current}mA
                """.trimIndent()
        } catch (e: Throwable) {
            return "Could not connect to scanner: $e"
        }
    }
}
