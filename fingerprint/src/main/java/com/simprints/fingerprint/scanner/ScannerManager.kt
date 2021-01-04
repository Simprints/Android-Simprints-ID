package com.simprints.fingerprint.scanner

import com.simprints.fingerprint.scanner.exceptions.safe.BluetoothNotEnabledException
import com.simprints.fingerprint.scanner.exceptions.safe.MultiplePossibleScannersPairedException
import com.simprints.fingerprint.scanner.exceptions.safe.ScannerNotPairedException
import com.simprints.fingerprint.scanner.wrapper.ScannerWrapper
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

/**
 * Acts as a holder for a [ScannerWrapper]. Designed to be passed around via dependency injection as
 * a singleton. The various helper methods are for accessing methods on [scanner] without the null-
 * check.
 */
interface ScannerManager {

    var scanner: ScannerWrapper?
    var currentScannerId: String?
    var currentMacAddress: String?

    /** Helper function for accessing [scanner] method without null-check */
    fun <T> onScanner(method: ScannerWrapper.() -> T): T
    /** Helper function for accessing [scanner] Completable without null-check */
    fun scanner(method: ScannerWrapper.() -> Completable): Completable
    /** Helper function for accessing [scanner] Single without null-check */
    fun <T> scanner(method: ScannerWrapper.() -> Single<T>): Single<T>
    /** Helper function for accessing [scanner] Observable without null-check */
    fun <T> scannerObservable(method: ScannerWrapper.() -> Observable<T>): Observable<T>

    /**
     * Instantiates [scanner] based on currently paired MAC addresses. Does not connect to the
     * scanner.
     *
     * @throws ScannerNotPairedException if there is no valid paired MAC address corresponding to a Vero
     * @throws MultiplePossibleScannersPairedException if there are more than one paired MAC address corresponding to valid Veros
     */
    fun initScanner(): Completable

    /** @throws BluetoothNotEnabledException */
    fun checkBluetoothStatus(): Completable
}
