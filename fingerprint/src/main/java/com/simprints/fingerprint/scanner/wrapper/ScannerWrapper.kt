package com.simprints.fingerprint.scanner.wrapper

import com.simprints.fingerprint.scanner.domain.CaptureFingerprintResponse
import com.simprints.fingerprint.scanner.domain.ScannerTriggerListener
import com.simprints.fingerprint.scanner.domain.ScannerVersionInformation
import io.reactivex.Completable
import io.reactivex.Single

interface ScannerWrapper {

    fun connect(): Completable
    fun disconnect(): Completable

    fun sensorWakeUp(): Completable
    fun sensorShutDown(): Completable

    fun getVersionInformation(): Single<ScannerVersionInformation>

    fun captureFingerprint(timeOutMs: Int, qualityThreshold: Int): Single<CaptureFingerprintResponse>

    fun setUiIdle(): Completable

    fun registerTriggerListener(triggerListener: ScannerTriggerListener)
    fun unregisterTriggerListener(triggerListener: ScannerTriggerListener)
}
