package com.simprints.fingerprint.scanner.wrapper

import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportManager
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
import timber.log.Timber
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import com.simprints.fingerprintscanner.v2.scanner.Scanner as ScannerV2

class ScannerWrapperV2(private val scannerV2: ScannerV2,
                       private val scannerUiHelper: ScannerUiHelper,
                       private val macAddress: String,
                       private val bluetoothAdapter: BluetoothComponentAdapter,
                       private val crashReportManager: FingerprintCrashReportManager) : ScannerWrapper {

    private lateinit var socket: BluetoothComponentSocket

    private var unifiedVersionInformation: UnifiedVersionInformation? = null

    override fun versionInformation(): ScannerVersionInformation =
        unifiedVersionInformation?.let {
            ScannerVersionInformation(2, it.masterFirmwareVersion,
                (it.un20AppVersion.apiMajorVersion.toLong() shl 48) or
                    (it.un20AppVersion.apiMinorVersion.toLong() shl 32) or
                    (it.un20AppVersion.firmwareMajorVersion.toLong() shl 16) or
                    (it.un20AppVersion.firmwareMinorVersion.toLong()))
        } ?: ScannerVersionInformation(2, -1, -1)

    override fun connect(): Completable {
        if (bluetoothAdapter.isNull()) throw BluetoothNotSupportedException()
        if (!bluetoothAdapter.isEnabled()) throw BluetoothNotEnabledException()

        val device = bluetoothAdapter.getRemoteDevice(macAddress)

        if (!device.isBonded()) throw ScannerNotPairedException()

        return Single.fromCallable {
            try {
                Timber.d("Attempting connect...")
                socket = device.createRfcommSocketToServiceRecord(DEFAULT_UUID)
                bluetoothAdapter.cancelDiscovery()
                socket.connect()
                socket
            } catch (e: IOException) {
                throw ScannerDisconnectedException()
            }
        }.retry(CONNECT_MAX_RETRIES)
            .flatMapCompletable { socket ->
                Timber.d("Socket connected. Setting up scanner...")
                scannerV2.connect(socket.getInputStream(), socket.getOutputStream())
                    .delay(100, TimeUnit.MILLISECONDS) // Speculatively needed
                    .andThen(scannerV2.getVersionInformation())
                    .map {
                        unifiedVersionInformation = it
                    }
                    .ignoreElement()
                    .andThen(scannerV2.enterMainMode())
                    .delay(100, TimeUnit.MILLISECONDS) // Speculatively needed
            }.wrapErrorsFromScanner()
    }

    override fun disconnect(): Completable =
        scannerV2.disconnect()
            .doOnComplete { socket.close() }
            .wrapErrorsFromScanner()

    override fun sensorWakeUp(): Completable =
        scannerV2
            .ensureUn20State(true)
            .wrapErrorsFromScanner()

    override fun sensorShutDown(): Completable =
        scannerV2
            .ensureUn20State(false)
            .wrapErrorsFromScanner()

    private fun ScannerV2.ensureUn20State(desiredState: Boolean): Completable =
        getUn20Status().flatMapCompletable { actualState ->
            when {
                desiredState && !actualState -> turnUn20OnAndAwaitStateChangeEvent()
                !desiredState && actualState -> turnUn20OffAndAwaitStateChangeEvent()
                else -> Completable.complete()
            }
        }

    override fun captureFingerprint(captureFingerprintStrategy: CaptureFingerprintStrategy, timeOutMs: Int, qualityThreshold: Int): Single<CaptureFingerprintResponse> =
        scannerV2
            .captureFingerprint(captureFingerprintStrategy.deduceCaptureDpi())
            .ensureCaptureResultOkOrError()
            .andThen(scannerV2.getImageQualityScore())
            .switchIfEmpty(Single.error(NoFingerDetectedException()))
            .setLedStateBasedOnQualityScoreOrInterpretAsNoFingerDetected(qualityThreshold)
            .acquireTemplateAndAssembleResponse()
            .switchIfEmpty(Single.error(NoFingerDetectedException()))
            .ifNoFingerDetectedThenSetBadScanLedState()
            .wrapErrorsFromScanner()

    private fun Single<CaptureFingerprintResult>.ensureCaptureResultOkOrError() =
        flatMapCompletable {
            when (it) {
                CaptureFingerprintResult.OK -> Completable.complete()
                CaptureFingerprintResult.FINGERPRINT_NOT_FOUND -> Completable.error(NoFingerDetectedException())
                CaptureFingerprintResult.DPI_UNSUPPORTED -> Completable.error(UnexpectedScannerException("Capture fingerprint DPI unsupported"))
                CaptureFingerprintResult.UNKNOWN_ERROR -> Completable.error(UnknownScannerIssueException("Unknown error when capturing fingerprint"))
            }
        }

    private fun Single<Int>.setLedStateBasedOnQualityScoreOrInterpretAsNoFingerDetected(qualityThreshold: Int) =
        flatMap { qualityScore ->
            if (qualityScore > NO_FINGER_IMAGE_QUALITY_THRESHOLD) {
                val ledState = if (qualityScore >= qualityThreshold) {
                    scannerUiHelper.goodScanLedState()
                } else {
                    scannerUiHelper.badScanLedState()
                }
                scannerV2.setSmileLedState(ledState)
                    .andThen(Single.just(qualityScore))
            } else {
                Single.error(NoFingerDetectedException())
            }
        }

    private fun Single<Int>.acquireTemplateAndAssembleResponse() =
        flatMapMaybe { imageQuality ->
            scannerV2.acquireTemplate()
                .map { templateData ->
                    CaptureFingerprintResponse(templateData.template, imageQuality)
                }
        }

    private fun Single<CaptureFingerprintResponse>.ifNoFingerDetectedThenSetBadScanLedState() =
        onErrorResumeNext {
            if (it is NoFingerDetectedException) {
                scannerV2.setSmileLedState(scannerUiHelper.badScanLedState())
                    .andThen(Single.error(it))
            } else {
                Single.error(it)
            }
        }

    override fun acquireImage(saveFingerprintImagesStrategy: SaveFingerprintImagesStrategy): Single<AcquireImageResponse> =
        saveFingerprintImagesStrategy
            .deduceImageAcquisitionFormat()?.let {
                scannerV2.acquireImage(it)
                    .map { imageBytes ->
                        AcquireImageResponse(imageBytes.image)
                    }
            }?.switchIfEmpty(Single.error(NoFingerDetectedException()))
            ?.wrapErrorsFromScanner()
            ?: Single.error(
                IllegalArgumentException("Fingerprint strategy $saveFingerprintImagesStrategy should not call acquireImage in ScannerWrapper")
            )

    override fun setUiIdle(): Completable =
        scannerV2
            .setSmileLedState(scannerUiHelper.idleLedState())
            .wrapErrorsFromScanner()

    private val triggerListenerToObserverMap = mutableMapOf<ScannerTriggerListener, Observer<Unit>>()

    override fun registerTriggerListener(triggerListener: ScannerTriggerListener) {
        triggerListenerToObserverMap[triggerListener] = object : DisposableObserver<Unit>() {
            override fun onComplete() {}
            override fun onNext(t: Unit) {
                triggerListener.onTrigger()
            }

            override fun onError(e: Throwable) {
                throw wrapErrorFromScanner(e)
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
            CaptureFingerprintStrategy.SECUGEN_ISO_500_DPI -> Dpi(500)
            CaptureFingerprintStrategy.SECUGEN_ISO_1000_DPI -> Dpi(1000)
            CaptureFingerprintStrategy.SECUGEN_ISO_1300_DPI -> Dpi(1300)
            CaptureFingerprintStrategy.SECUGEN_ISO_1700_DPI -> Dpi(1700)
        }

    private fun SaveFingerprintImagesStrategy.deduceImageAcquisitionFormat(): ImageFormatData? =
        when (this) {
            SaveFingerprintImagesStrategy.NEVER -> null
            SaveFingerprintImagesStrategy.WSQ_15 -> ImageFormatData.WSQ(15)
        }

    private fun Completable.wrapErrorsFromScanner() =
        onErrorResumeNext { Completable.error(wrapErrorFromScanner(it)) }

    private fun <T> Single<T>.wrapErrorsFromScanner() =
        onErrorResumeNext { Single.error(wrapErrorFromScanner(it)) }

    private fun wrapErrorFromScanner(e: Throwable): Throwable = when (e) {
        is IOException -> { // Disconnected or timed-out communications with Scanner
            Timber.d(e, "IOException in ScannerWrapperV2, transformed to ScannerDisconnectedException")
            ScannerDisconnectedException()
        }
        is IllegalStateException, // We're calling scanner methods out of order somehow
        is IllegalArgumentException -> { // We've received unexpected/invalid bytes from the scanner
            Timber.e(e)
            crashReportManager.logExceptionOrSafeException(e)
            UnexpectedScannerException(e)
        }
        else -> { // Propagate error
            e
        }
    }

    companion object {
        private val DEFAULT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
        private const val CONNECT_MAX_RETRIES = 1L
        private const val NO_FINGER_IMAGE_QUALITY_THRESHOLD = 10 // The image quality at which we decide a fingerprint wasn't detected
    }
}
