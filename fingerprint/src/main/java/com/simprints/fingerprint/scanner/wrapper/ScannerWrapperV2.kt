package com.simprints.fingerprint.scanner.wrapper

import com.simprints.fingerprint.scanner.domain.CaptureFingerprintResponse
import com.simprints.fingerprint.scanner.domain.ScannerTriggerListener
import com.simprints.fingerprint.scanner.domain.ScannerVersionInformation
import io.reactivex.Completable
import io.reactivex.Single

class ScannerWrapperV2: ScannerWrapper {

    override val versionInformation: ScannerVersionInformation
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun connect(): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun disconnect(): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sensorWakeUp(): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sensorShutDown(): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun captureFingerprint(timeOutMs: Int, qualityThreshold: Int): Single<CaptureFingerprintResponse> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setUiIdle(): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun registerTriggerListener(triggerListener: ScannerTriggerListener) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun unregisterTriggerListener(triggerListener: ScannerTriggerListener) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
