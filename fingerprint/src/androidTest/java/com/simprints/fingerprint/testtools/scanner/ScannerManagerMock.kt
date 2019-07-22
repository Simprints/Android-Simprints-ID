package com.simprints.fingerprint.testtools.scanner

import com.simprints.fingerprint.activities.alert.FingerprintAlert
import com.simprints.fingerprint.controllers.scanner.ScannerManager
import com.simprints.fingerprintscanner.Scanner
import com.simprints.fingerprintscannermock.MockScannerManager
import io.reactivex.Completable

class ScannerManagerMock(override var scanner: Scanner?,
                         override var macAddress: String? = MockScannerManager.DEFAULT_MAC_ADDRESS) : ScannerManager {

    override fun start(): Completable = Completable.complete()

    override fun disconnectVero(): Completable = Completable.complete()

    override fun initVero(): Completable = Completable.complete()

    override fun connectToVero(): Completable = Completable.complete()

    override fun wakeUpVero(): Completable = Completable.complete()

    override fun shutdownVero(): Completable = Completable.complete()

    override fun resetVeroUI(): Completable = Completable.complete()

    override fun getAlertType(it: Throwable): FingerprintAlert {
        throw UnsupportedOperationException("Haven't implemented getAlertType in ScannerManagerMock")
    }

    override fun disconnectScannerIfNeeded() = Unit

    override fun checkBluetoothStatus(): Completable = Completable.complete()
}
