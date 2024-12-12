package com.simprints.fingerprint.infra.scanner

import com.simprints.fingerprint.infra.scanner.component.bluetooth.ComponentBluetoothAdapter
import com.simprints.fingerprint.infra.scanner.data.FirmwareRepository
import com.simprints.fingerprint.infra.scanner.exceptions.safe.BluetoothNotEnabledException
import com.simprints.fingerprint.infra.scanner.exceptions.unexpected.NullScannerException
import com.simprints.fingerprint.infra.scanner.tools.SerialNumberConverter
import com.simprints.fingerprint.infra.scanner.wrapper.ScannerFactory
import com.simprints.fingerprint.infra.scanner.wrapper.ScannerOtaOperationsWrapper
import com.simprints.fingerprint.infra.scanner.wrapper.ScannerWrapper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScannerManagerImpl @Inject constructor(
    private val bluetoothAdapter: ComponentBluetoothAdapter,
    private val scannerFactory: ScannerFactory,
    private val pairingManager: ScannerPairingManager,
    private val serialNumberConverter: SerialNumberConverter,
    private val firmwareRepository: FirmwareRepository,
) : ScannerManager {
    private var _scanner: ScannerWrapper? = null

    private var _otaOperationsWrapper: ScannerOtaOperationsWrapper? = null

    override val scanner: ScannerWrapper
        get() {
            return _scanner ?: throw NullScannerException()
        }
    override val otaOperationsWrapper: ScannerOtaOperationsWrapper
        get() = _otaOperationsWrapper ?: throw NullScannerException()

    override val isScannerConnected: Boolean get() = _scanner?.isConnected() ?: false
    override var currentScannerId: String? = null
    override var currentMacAddress: String? = null

    override suspend fun initScanner() {
        val macAddress = pairingManager.getPairedScannerAddressToUse()
        scannerFactory.initScannerOperationWrappers(macAddress)
        _scanner = scannerFactory.scannerWrapper
        _otaOperationsWrapper = scannerFactory.scannerOtaOperationsWrapper
        currentMacAddress = macAddress
        currentScannerId = serialNumberConverter.convertMacAddressToSerialNumber(macAddress)
    }

    override suspend fun checkBluetoothStatus() {
        if (!bluetoothIsEnabled()) {
            throw BluetoothNotEnabledException()
        }
    }

    private fun bluetoothIsEnabled() = bluetoothAdapter.isEnabled()

    override suspend fun deleteFirmwareFiles() {
        firmwareRepository.deleteAllFirmwareFiles()
    }
}
