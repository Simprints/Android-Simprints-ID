package com.simprints.fingerprint.scanner.wrapper

import com.simprints.fingerprint.data.domain.fingerprint.CaptureFingerprintStrategy
import com.simprints.fingerprint.data.domain.images.SaveFingerprintImagesStrategy
import com.simprints.fingerprint.scanner.domain.AcquireImageResponse
import com.simprints.fingerprint.scanner.domain.CaptureFingerprintResponse
import com.simprints.fingerprint.scanner.domain.ScannerTriggerListener
import com.simprints.fingerprint.scanner.domain.ScannerVersionInformation
import com.simprints.fingerprint.scanner.exceptions.safe.BluetoothNotEnabledException
import com.simprints.fingerprint.scanner.exceptions.safe.NoFingerDetectedException
import com.simprints.fingerprint.scanner.exceptions.safe.ScannerDisconnectedException
import com.simprints.fingerprint.scanner.exceptions.safe.ScannerNotPairedException
import com.simprints.fingerprint.scanner.exceptions.unexpected.BluetoothNotSupportedException
import com.simprints.fingerprint.scanner.exceptions.unexpected.UnexpectedScannerException
import com.simprints.fingerprint.scanner.exceptions.unexpected.UnknownScannerIssueException
import com.simprints.fingerprint.scanner.ui.ScannerUiHelper
import com.simprints.fingerprintscanner.component.bluetooth.BluetoothComponentAdapter
import com.simprints.fingerprintscanner.component.bluetooth.BluetoothComponentSocket
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.CaptureFingerprintResult
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.Dpi
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.ImageFormatData
import com.simprints.fingerprintscanner.v2.domain.root.models.UnifiedVersionInformation
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

    private var unifiedVersionInformation: UnifiedVersionInformation? = null

    override fun versionInformation(): ScannerVersionInformation =
        unifiedVersionInformation?.let {
            ScannerVersionInformation(2, it.masterFirmwareVersion.toInt(), it.un20AppVersion.firmwareMajorVersion.toInt())
        } ?: ScannerVersionInformation(2, -1, -1)

    override fun connect(): Completable {
        if (bluetoothAdapter.isNull()) throw BluetoothNotSupportedException()
        if (!bluetoothAdapter.isEnabled()) throw BluetoothNotEnabledException()

        val device = bluetoothAdapter.getRemoteDevice(macAddress)

        if (!device.isBonded()) throw ScannerNotPairedException()

        return Single.fromCallable {
            try {
                socket = device.createRfcommSocketToServiceRecord(DEFAULT_UUID)
                bluetoothAdapter.cancelDiscovery()
                socket.connect()
                socket
            } catch (e: IOException) {
                throw ScannerDisconnectedException()
            }
        }.retry(CONNECT_MAX_RETRIES)
            .flatMapCompletable { socket ->
                scannerV2.connect(socket.getInputStream(), socket.getOutputStream())
                    .andThen(scannerV2.getVersionInformation())
                    .map {
                        unifiedVersionInformation = it
                    }
                    .ignoreElement()
                    .andThen(scannerV2.enterMainMode())
            }
    }

    override fun disconnect(): Completable =
        scannerV2.disconnect()
            .doOnComplete { socket.close() }

    override fun sensorWakeUp(): Completable = scannerV2.turnUn20OnAndAwaitStateChangeEvent()

    override fun sensorShutDown(): Completable = scannerV2.turnUn20OffAndAwaitStateChangeEvent()

    override fun captureFingerprint(captureFingerprintStrategy: CaptureFingerprintStrategy, timeOutMs: Int, qualityThreshold: Int): Single<CaptureFingerprintResponse> =
        scannerV2.captureFingerprint(captureFingerprintStrategy.deduceCaptureDpi()).flatMapCompletable {
            when (it) {
                CaptureFingerprintResult.OK -> Completable.complete()
                CaptureFingerprintResult.FINGERPRINT_NOT_FOUND -> Completable.error(NoFingerDetectedException())
                CaptureFingerprintResult.DPI_UNSUPPORTED -> Completable.error(UnexpectedScannerException("Capture fingerprint DPI unsupported"))
                CaptureFingerprintResult.UNKNOWN_ERROR -> Completable.error(UnknownScannerIssueException("Unknown error when capturing fingerprint"))
            }
        }
            .andThen(scannerV2.getImageQualityScore())
            .flatMap { qualityScore ->
                val ledState = if (qualityScore >= qualityThreshold) {
                    scannerUiHelper.goodScanLedState()
                } else {
                    scannerUiHelper.badScanLedState()
                }
                scannerV2.setSmileLedState(ledState)
                    .andThen(Single.just(qualityScore))
            }
            .flatMap { imageQuality ->
                scannerV2.acquireTemplate()
                    .map { templateData ->
                        CaptureFingerprintResponse(templateData.template, imageQuality)
                    }
            }

    override fun acquireImage(saveFingerprintImagesStrategy: SaveFingerprintImagesStrategy): Single<AcquireImageResponse> =
        saveFingerprintImagesStrategy.deduceImageAcquisitionFormat()?.let {
            scannerV2.acquireImage(it)
                .map { imageBytes ->
                    AcquireImageResponse(imageBytes.image)
                }
        } ?: TODO("Convert getImage to use Maybe instead of Single")

    override fun setUiIdle(): Completable = scannerV2.setSmileLedState(scannerUiHelper.idleLedState())

    private val triggerListenerToObserverMap = mutableMapOf<ScannerTriggerListener, Observer<Unit>>()

    override fun registerTriggerListener(triggerListener: ScannerTriggerListener) {
        triggerListenerToObserverMap[triggerListener] = object : DisposableObserver<Unit>() {
            override fun onComplete() {}
            override fun onNext(t: Unit) {
                triggerListener.onTrigger()
            }

            override fun onError(e: Throwable) {
                throw e
            }
        }.also { scannerV2.triggerButtonListeners.add(it) }
    }

    override fun unregisterTriggerListener(triggerListener: ScannerTriggerListener) {
        triggerListenerToObserverMap[triggerListener]?.let {
            scannerV2.triggerButtonListeners.remove(it)
        }
    }

    private fun CaptureFingerprintStrategy.deduceCaptureDpi(): Dpi =
        when (this) {
            CaptureFingerprintStrategy.SECUGEN_ISO_1700_DPI -> Dpi(1700)
        }

    private fun SaveFingerprintImagesStrategy.deduceImageAcquisitionFormat(): ImageFormatData? =
        when (this) {
            SaveFingerprintImagesStrategy.NEVER -> null
            SaveFingerprintImagesStrategy.WSQ_15 -> ImageFormatData.WSQ(15)
        }

    companion object {
        private val DEFAULT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
        private const val CONNECT_MAX_RETRIES = 3L
    }
}
