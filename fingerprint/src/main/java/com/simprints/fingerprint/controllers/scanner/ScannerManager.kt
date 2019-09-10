package com.simprints.fingerprint.controllers.scanner

import com.simprints.fingerprint.activities.alert.FingerprintAlert
import com.simprints.fingerprintscanner.v1.Scanner
import io.reactivex.Completable

interface ScannerManager {

    var scanner: Scanner?
    var lastPairedScannerId: String?

    fun start(): Completable
    fun disconnectVero(): Completable
    fun initVero(): Completable
    fun connectToVero(): Completable
    fun wakeUpVero(): Completable
    fun shutdownVero(): Completable
    fun resetVeroUI(): Completable
    fun getAlertType(it: Throwable): FingerprintAlert
    fun disconnectScannerIfNeeded()
    fun checkBluetoothStatus(): Completable
}
