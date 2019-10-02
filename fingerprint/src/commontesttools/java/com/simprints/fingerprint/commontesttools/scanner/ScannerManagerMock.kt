package com.simprints.fingerprint.commontesttools.scanner

import com.simprints.fingerprint.activities.alert.FingerprintAlert
import com.simprints.fingerprint.scanner.ScannerManager
import com.simprints.fingerprint.scanner.factory.ScannerFactory
import com.simprints.fingerprint.scanner.wrapper.ScannerWrapper
import com.simprints.fingerprintscanner.api.bluetooth.BluetoothComponentAdapter
import io.reactivex.Completable

class ScannerManagerMock(val bluetoothAdapter: BluetoothComponentAdapter,
                         scannerFactory: ScannerFactory,
                         override var lastPairedScannerId: String = DEFAULT_SCANNER_ID,
                         override var lastPairedMacAddress: String = DEFAULT_MAC_ADDRESS) : ScannerManager {

    override var scanner: ScannerWrapper = scannerFactory.create(lastPairedMacAddress)

    override fun initScanner(): Completable = Completable.complete()

    override fun getAlertType(it: Throwable): FingerprintAlert {
        throw UnsupportedOperationException("Haven't mocked getAlertType for $it in ScannerManagerMock")
    }

    override fun checkBluetoothStatus(): Completable = Completable.fromAction {
        if (blu)
    }
}
