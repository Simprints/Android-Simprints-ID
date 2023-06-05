package com.simprints.fingerprint.scanner.factory

import com.simprints.core.DispatcherIO
import com.simprints.fingerprint.scanner.controllers.v2.*
import com.simprints.fingerprint.scanner.tools.ScannerGenerationDeterminer
import com.simprints.fingerprint.scanner.tools.SerialNumberConverter
import com.simprints.fingerprint.scanner.ui.ScannerUiHelper
import com.simprints.fingerprint.scanner.wrapper.ScannerWrapper
import com.simprints.fingerprint.scanner.wrapper.ScannerWrapperV1
import com.simprints.fingerprint.scanner.wrapper.ScannerWrapperV2
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothAdapter
import com.simprints.fingerprintscanner.v2.scanner.create
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.FingerprintConfiguration
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject
import com.simprints.fingerprintscanner.v1.Scanner as ScannerV1
import com.simprints.fingerprintscanner.v2.scanner.Scanner as ScannerV2

class ScannerFactoryImpl @Inject constructor(
    private val bluetoothAdapter: ComponentBluetoothAdapter,
    private val configManager: ConfigManager,
    private val scannerUiHelper: ScannerUiHelper,
    private val serialNumberConverter: SerialNumberConverter,
    private val scannerGenerationDeterminer: ScannerGenerationDeterminer,
    private val scannerInitialSetupHelper: ScannerInitialSetupHelper,
    private val connectionHelper: ConnectionHelper,
    private val cypressOtaHelper: CypressOtaHelper,
    private val stmOtaHelper: StmOtaHelper,
    private val un20OtaHelper: Un20OtaHelper,
    @DispatcherIO private val ioDispatcher: CoroutineDispatcher,
) : ScannerFactory {

    override suspend fun create(macAddress: String): ScannerWrapper {
        val availableScannerGenerations =
            configManager.getProjectConfiguration().fingerprint?.allowedVeroGenerations ?: listOf()

        val scannerGenerationToUse = when (availableScannerGenerations.size) {
            1 -> availableScannerGenerations.single()
            else -> scannerGenerationDeterminer.determineScannerGenerationFromSerialNumber(
                serialNumberConverter.convertMacAddressToSerialNumber(macAddress)
            )
        }.also {
            Simber.i("Using scanner generation $it")
        }

        return when (scannerGenerationToUse) {
            FingerprintConfiguration.VeroGeneration.VERO_1 -> createScannerV1(macAddress)
            FingerprintConfiguration.VeroGeneration.VERO_2 -> createScannerV2(macAddress)
        }
    }

    private fun createScannerV1(macAddress: String): ScannerWrapper =
        ScannerWrapperV1(
            ScannerV1(macAddress, bluetoothAdapter),
            ioDispatcher,
        )

    private fun createScannerV2(macAddress: String): ScannerWrapper =
        ScannerWrapperV2(
            ScannerV2.create(),
            scannerUiHelper,
            macAddress,
            scannerInitialSetupHelper,
            connectionHelper,
            cypressOtaHelper,
            stmOtaHelper,
            un20OtaHelper,
            ioDispatcher,
        )
}
