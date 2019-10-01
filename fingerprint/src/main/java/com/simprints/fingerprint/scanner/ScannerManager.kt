package com.simprints.fingerprint.scanner

import com.simprints.fingerprint.activities.alert.FingerprintAlert
import com.simprints.fingerprint.scanner.wrapper.ScannerWrapper
import io.reactivex.Completable

interface ScannerManager {

    var scanner: ScannerWrapper
    var lastPairedScannerId: String

    fun initScanner(): Completable

    fun getAlertType(it: Throwable): FingerprintAlert
    fun checkBluetoothStatus(): Completable
}
