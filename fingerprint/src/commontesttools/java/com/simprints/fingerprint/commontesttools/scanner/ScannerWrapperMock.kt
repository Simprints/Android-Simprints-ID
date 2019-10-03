package com.simprints.fingerprint.commontesttools.scanner

import com.simprints.fingerprint.scanner.domain.CaptureFingerprintResponse
import com.simprints.fingerprint.scanner.domain.ScannerTriggerListener
import com.simprints.fingerprint.scanner.domain.ScannerVersionInformation
import com.simprints.fingerprint.scanner.wrapper.ScannerWrapper
import com.simprints.testtools.common.syntax.mock
import io.reactivex.Completable
import io.reactivex.Single

class ScannerWrapperMock : ScannerWrapper {

    override fun connect(): Completable = Completable.complete()

    override fun disconnect(): Completable = Completable.complete()

    override fun sensorWakeUp(): Completable = Completable.complete()

    override fun sensorShutDown(): Completable = Completable.complete()

    override fun captureFingerprint(timeOutMs: Int, qualityThreshold: Int): Single<CaptureFingerprintResponse> =
        Single.just(mock())

    override fun setUiIdle(): Completable  = Completable.complete()

    override fun registerTriggerListener(triggerListener: ScannerTriggerListener) {
    }

    override fun unregisterTriggerListener(triggerListener: ScannerTriggerListener) {
    }

    override val versionInformation: ScannerVersionInformation
        get() = mock()
}
