package com.simprints.fingerprint.scanner

import com.simprints.fingerprint.infra.scanner.component.bluetooth.ComponentBluetoothAdapter
import com.simprints.fingerprint.infra.scanner.exceptions.unexpected.NullScannerException
import com.simprints.fingerprint.scanner.exceptions.safe.BluetoothNotEnabledException
import com.simprints.fingerprint.scanner.factory.ScannerFactory
import com.simprints.fingerprint.scanner.pairing.ScannerPairingManager
import com.simprints.fingerprint.scanner.tools.SerialNumberConverter
import com.simprints.fingerprint.scanner.wrapper.ScannerOtaOperationsWrapper
import com.simprints.fingerprint.scanner.wrapper.ScannerWrapper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScannerManagerImpl @Inject  internal constructor(
    private val bluetoothAdapter: ComponentBluetoothAdapter,
    private val scannerFactory: ScannerFactory,
    private val pairingManager: ScannerPairingManager,
    private val serialNumberConverter: SerialNumberConverter
) : ScannerManager {

    private var _scanner: ScannerWrapper? = null

    private var _otaOperationsWrapper: ScannerOtaOperationsWrapper? = null

    override val scanner: ScannerWrapper
        get() {
            return _scanner ?: throw NullScannerException()
        }
    override val otaOperationsWrapper: ScannerOtaOperationsWrapper
        get() = _otaOperationsWrapper ?: throw NullScannerException()

    override val isScannerAvailable: Boolean get() = _scanner != null
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
}
