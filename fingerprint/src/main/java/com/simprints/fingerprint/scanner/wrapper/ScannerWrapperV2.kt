package com.simprints.fingerprint.scanner.wrapper

import com.simprints.fingerprint.scanner.domain.CaptureFingerprintResponse
import com.simprints.fingerprint.scanner.domain.ScannerTriggerListener
import com.simprints.fingerprint.scanner.domain.ScannerVersionInformation
import com.simprints.fingerprint.scanner.ui.ScannerUiHelper
import com.simprints.fingerprintscanner.component.bluetooth.BluetoothComponentSocket
import com.simprints.fingerprintscanner.v2.tools.primitives.unsignedToInt
import io.reactivex.Completable
import io.reactivex.Observer
import io.reactivex.Single
import io.reactivex.observers.DisposableObserver
import com.simprints.fingerprintscanner.v2.scanner.Scanner as ScannerV2

class ScannerWrapperV2(private val scannerV2: ScannerV2,
                       private val scannerUiHelper: ScannerUiHelper,
                       private val socket: BluetoothComponentSocket) : ScannerWrapper {

    override val versionInformation: ScannerVersionInformation by lazy {
        TODO()
    }

    override fun connect(): Completable = Completable.fromAction {
        scannerV2.connect(socket)
    }

    override fun disconnect(): Completable = Completable.fromAction {
        scannerV2.disconnect()
    }

    override fun sensorWakeUp(): Completable = scannerV2.turnUn20OnAndAwaitStateChangeEvent()

    override fun sensorShutDown(): Completable = scannerV2.turnUn20OffAndAwaitStateChangeEvent()

    override fun captureFingerprint(timeOutMs: Int, qualityThreshold: Int): Single<CaptureFingerprintResponse> =
        scannerV2.captureFingerprint()
            .andThen(scannerV2.getImageQuality())
            .flatMap {
                imageQuality -> scannerV2.acquireTemplate().map { template -> CaptureFingerprintResponse(template, imageQuality.unsignedToInt()) }
            }

    override fun setUiIdle(): Completable = scannerV2.setSmileLedState(scannerUiHelper.idleLedState())

    private val triggerListenerToObserverMap = mutableMapOf<ScannerTriggerListener, Observer<Unit>>()

    override fun registerTriggerListener(triggerListener: ScannerTriggerListener) {
        triggerListenerToObserverMap[triggerListener] = object : DisposableObserver<Unit>() {
            override fun onComplete() {}
            override fun onNext(t: Unit) { triggerListener.onTrigger() }
            override fun onError(e: Throwable) { throw e }
        }.also { scannerV2.triggerButtonListeners.add(it) }
    }

    override fun unregisterTriggerListener(triggerListener: ScannerTriggerListener) {
        triggerListenerToObserverMap[triggerListener]?.let {
            scannerV2.triggerButtonListeners.remove(it)
        }
    }
}
