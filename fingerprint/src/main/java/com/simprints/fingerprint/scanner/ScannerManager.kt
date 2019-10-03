package com.simprints.fingerprint.scanner

import com.simprints.fingerprint.activities.alert.FingerprintAlert
import com.simprints.fingerprint.scanner.domain.CaptureFingerprintResponse
import com.simprints.fingerprint.scanner.domain.ScannerTriggerListener
import com.simprints.fingerprint.scanner.domain.ScannerVersionInformation
import com.simprints.fingerprint.scanner.wrapper.ScannerWrapper
import io.reactivex.Completable
import io.reactivex.Single

interface ScannerManager {

    var scanner: ScannerWrapper
    var lastPairedScannerId: String?
    var lastPairedMacAddress: String?

    fun initScanner(): Completable

    fun connect(): Completable
    fun disconnectIfNecessary(): Completable

    fun sensorWakeUp(): Completable
    fun sensorShutDown(): Completable

    fun captureFingerprint(timeOutMs: Int, qualityThreshold: Int): Single<CaptureFingerprintResponse>

    fun setUiIdle(): Completable

    fun registerTriggerListener(triggerListener: ScannerTriggerListener)
    fun unregisterTriggerListener(triggerListener: ScannerTriggerListener)
    val versionInformation: ScannerVersionInformation

    fun getAlertType(e: Throwable): FingerprintAlert
    fun checkBluetoothStatus(): Completable
}
