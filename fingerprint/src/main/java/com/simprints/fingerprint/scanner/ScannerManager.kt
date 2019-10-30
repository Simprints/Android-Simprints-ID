package com.simprints.fingerprint.scanner

import com.simprints.fingerprint.activities.alert.FingerprintAlert
import com.simprints.fingerprint.scanner.wrapper.ScannerWrapper
import io.reactivex.Completable
import io.reactivex.Single

interface ScannerManager {

    var scanner: ScannerWrapper?
    var lastPairedScannerId: String?
    var lastPairedMacAddress: String?

    fun <T> onScanner(method: ScannerWrapper.() -> T): T
    fun scanner(method: ScannerWrapper.() -> Completable): Completable
    fun <T> scanner(method: ScannerWrapper.() -> Single<T>): Single<T>

    fun initScanner(): Completable

    fun getAlertType(e: Throwable): FingerprintAlert
    fun checkBluetoothStatus(): Completable
}
