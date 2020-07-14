package com.simprints.fingerprint.scanner

import com.simprints.fingerprint.activities.alert.FingerprintAlert
import com.simprints.fingerprint.scanner.wrapper.ScannerWrapper
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface ScannerManager {

    var scanner: ScannerWrapper?
    var currentScannerId: String?
    var currentMacAddress: String?

    fun <T> onScanner(method: ScannerWrapper.() -> T): T
    fun scanner(method: ScannerWrapper.() -> Completable): Completable
    fun <T> scanner(method: ScannerWrapper.() -> Single<T>): Single<T>
    fun <T> scannerObservable(method: ScannerWrapper.() -> Observable<T>): Observable<T>

    fun initScanner(): Completable

    fun getAlertType(e: Throwable): FingerprintAlert
    fun checkBluetoothStatus(): Completable
}
