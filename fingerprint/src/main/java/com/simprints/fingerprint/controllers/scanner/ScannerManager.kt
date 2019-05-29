package com.simprints.fingerprint.controllers.scanner

import com.simprints.fingerprint.activities.alert.FingerprintAlert
import com.simprints.fingerprintscanner.Scanner
import io.reactivex.Completable

interface ScannerManager {

    var scanner: Scanner?
    var macAddress: String?
    var scannerId: String?
    var hardwareVersion: String?

    fun start(): Completable
    fun disconnectVero(): Completable
    fun initVero(): Completable
    fun connectToVero(): Completable
    fun wakeUpVero(): Completable
    fun shutdownVero(): Completable
    fun resetVeroUI(): Completable
    fun getAlertType(it: Throwable): FingerprintAlert
    fun disconnectScannerIfNeeded()
}
