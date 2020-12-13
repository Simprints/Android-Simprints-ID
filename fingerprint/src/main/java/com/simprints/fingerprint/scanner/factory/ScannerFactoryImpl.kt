package com.simprints.fingerprint.scanner.factory

import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportManager
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.scanner.controllers.v2.*
import com.simprints.fingerprint.scanner.domain.ScannerGeneration
import com.simprints.fingerprint.scanner.tools.ScannerGenerationDeterminer
import com.simprints.fingerprint.scanner.tools.SerialNumberConverter
import com.simprints.fingerprint.scanner.ui.ScannerUiHelper
import com.simprints.fingerprint.scanner.wrapper.ScannerWrapper
import com.simprints.fingerprint.scanner.wrapper.ScannerWrapperV1
import com.simprints.fingerprint.scanner.wrapper.ScannerWrapperV2
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothAdapter
import com.simprints.fingerprintscanner.v2.scanner.create
import timber.log.Timber
import com.simprints.fingerprintscanner.v1.Scanner as ScannerV1
import com.simprints.fingerprintscanner.v2.scanner.Scanner as ScannerV2

class ScannerFactoryImpl(private val bluetoothAdapter: ComponentBluetoothAdapter,
                         private val preferencesManager: FingerprintPreferencesManager,
                         private val crashReportManager: FingerprintCrashReportManager,
                         private val scannerUiHelper: ScannerUiHelper,
                         private val serialNumberConverter: SerialNumberConverter,
                         private val scannerGenerationDeterminer: ScannerGenerationDeterminer,
                         private val scannerInitialSetupHelper: ScannerInitialSetupHelper,
                         private val connectionHelper: ConnectionHelper,
                         private val cypressOtaHelper: CypressOtaHelper,
                         private val stmOtaHelper: StmOtaHelper,
                         private val un20OtaHelper: Un20OtaHelper) : ScannerFactory {

    override fun create(macAddress: String): ScannerWrapper {
        val availableScannerGenerations = preferencesManager.scannerGenerations

        val scannerGenerationToUse = when (availableScannerGenerations.size) {
            1 -> availableScannerGenerations.single()
            else -> scannerGenerationDeterminer.determineScannerGenerationFromSerialNumber(
                serialNumberConverter.convertMacAddressToSerialNumber(macAddress)
            )
        }.also {
            Timber.i("Using scanner generation $it")
        }

        return when (scannerGenerationToUse) {
            ScannerGeneration.VERO_1 -> createScannerV1(macAddress)
            ScannerGeneration.VERO_2 -> createScannerV2(macAddress)
        }
    }

    fun createScannerV1(macAddress: String): ScannerWrapper =
        ScannerWrapperV1(
            ScannerV1(macAddress, bluetoothAdapter)
        )

    fun createScannerV2(macAddress: String): ScannerWrapper =
        ScannerWrapperV2(
            ScannerV2.create(),
            scannerUiHelper,
            macAddress,
            scannerInitialSetupHelper,
            connectionHelper,
            cypressOtaHelper,
            stmOtaHelper,
            un20OtaHelper,
            crashReportManager
        )
}
