package com.simprints.fingerprint.scanner.wrapper

import com.simprints.fingerprint.scanner.domain.AcquireImageResponse
import com.simprints.fingerprint.scanner.domain.CaptureFingerprintResponse
import com.simprints.fingerprint.scanner.domain.ScannerTriggerListener
import com.simprints.fingerprint.scanner.domain.ScannerVersionInformation
import com.simprints.fingerprint.scanner.exceptions.safe.BluetoothNotEnabledException
import com.simprints.fingerprint.scanner.exceptions.safe.ScannerDisconnectedException
import com.simprints.fingerprint.scanner.exceptions.safe.ScannerNotPairedException
import com.simprints.fingerprint.scanner.exceptions.unexpected.BluetoothNotSupportedException
import com.simprints.fingerprint.scanner.ui.ScannerUiHelper
import com.simprints.fingerprintscanner.component.bluetooth.BluetoothComponentAdapter
import com.simprints.fingerprintscanner.component.bluetooth.BluetoothComponentSocket
import io.reactivex.Completable
import io.reactivex.Observer
import io.reactivex.Single
import io.reactivex.observers.DisposableObserver
import java.io.IOException
import java.util.*
import com.simprints.fingerprintscanner.v2.scanner.Scanner as ScannerV2

class ScannerWrapperV2(private val scannerV2: ScannerV2,
                       private val scannerUiHelper: ScannerUiHelper,
                       private val macAddress: String,
                       private val bluetoothAdapter: BluetoothComponentAdapter) : ScannerWrapper {

    private lateinit var socket: BluetoothComponentSocket

    override val versionInformation: ScannerVersionInformation by lazy {
        ScannerVersionInformation(2, 7, 2) // TODO : Implement version fetching
    }

    override fun connect(): Completable = Completable.fromAction {
        if (bluetoothAdapter.isNull()) throw BluetoothNotSupportedException()
        if (!bluetoothAdapter.isEnabled()) throw BluetoothNotEnabledException()

        val device = bluetoothAdapter.getRemoteDevice(macAddress)

        if (!device.isBonded()) throw ScannerNotPairedException()

        try {
            socket = device.createRfcommSocketToServiceRecord(DEFAULT_UUID)
            bluetoothAdapter.cancelDiscovery()
            socket.connect()
            scannerV2.connect(socket.getInputStream(), socket.getOutputStream())
        } catch (e: IOException) {
            throw ScannerDisconnectedException()
        }
    }

    override fun disconnect(): Completable = Completable.fromAction {
        scannerV2.disconnect()
        socket.close()
    }

    override fun sensorWakeUp(): Completable = scannerV2.turnUn20OnAndAwaitStateChangeEvent()

    override fun sensorShutDown(): Completable = scannerV2.turnUn20OffAndAwaitStateChangeEvent()

    override fun captureFingerprint(timeOutMs: Int, qualityThreshold: Int): Single<CaptureFingerprintResponse> =
        scannerV2.captureFingerprint().ignoreElement() // TODO : Add in error propagation for no finger detected etc.
            .andThen(scannerV2.acquireTemplate())
            .map { templateData ->
                CaptureFingerprintResponse(templateData.template, templateData.quality)
            }

    override fun acquireImage(): Single<AcquireImageResponse> =
        scannerV2.acquireImage()
            .map { imageBytes ->
                AcquireImageResponse(imageBytes)
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

    companion object {
        private val DEFAULT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
    }
}
