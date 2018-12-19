package com.simprints.id.scanner

import com.simprints.id.domain.ALERT_TYPE
import com.simprints.libscanner.Scanner
import io.reactivex.Completable

interface ScannerManager {

    var scanner: Scanner?

    fun start(): Completable
    fun disconnectVero(): Completable
    fun initVero(): Completable
    fun connectToVero(): Completable
    fun wakeUpVero(): Completable
    fun shutdownVero(): Completable
    fun resetVeroUI(): Completable
    fun getAlertType(it: Throwable): ALERT_TYPE
    fun disconnectScannerIfNeeded()
}
