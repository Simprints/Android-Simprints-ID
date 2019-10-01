package com.simprints.fingerprint.scanner.factory

import com.simprints.fingerprint.scanner.wrapper.ScannerWrapper
import com.simprints.fingerprint.scanner.wrapper.ScannerWrapperV1
import com.simprints.fingerprintscanner.api.bluetooth.BluetoothComponentAdapter
import com.simprints.fingerprintscanner.v1.Scanner as ScannerV1

class ScannerFactoryImpl(val bluetoothAdapter: BluetoothComponentAdapter) : ScannerFactory {

    override fun create(macAddress: String): ScannerWrapper {
        // TODO : Determine whether to create a ScannerV1 or a ScannerV2
        return ScannerWrapperV1(
            ScannerV1(macAddress, bluetoothAdapter)
        )
    }
}
