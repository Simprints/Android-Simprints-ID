package com.simprints.fingerprint.controllers.scanner.wrapper

import com.simprints.fingerprint.controllers.scanner.wrapper.domain.CaptureFingerprintResponse
import com.simprints.fingerprint.controllers.scanner.wrapper.domain.ScannerTriggerListener
import com.simprints.fingerprint.controllers.scanner.wrapper.domain.ScannerVersionInformation
import io.reactivex.Completable
import io.reactivex.Single

interface ScannerWrapper {

    fun connect(): Completable
    fun disconnect(): Completable

    fun un20WakeUp(): Completable
    fun un20ShutDown(): Completable

    fun captureFingerprint(): Single<CaptureFingerprintResponse>

    fun setUiIdle(): Completable
    fun setUiScanning(): Completable
    fun setUiGoodScan(): Completable
    fun setUiBadScan(): Completable

    fun getVersionInformation(): Single<ScannerVersionInformation>

    fun registerTriggerListener(triggerListener: ScannerTriggerListener)
    fun unregisterTriggerListener(triggerListener: ScannerTriggerListener)
}
