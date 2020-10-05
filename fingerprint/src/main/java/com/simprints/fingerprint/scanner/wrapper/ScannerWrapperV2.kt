package com.simprints.fingerprint.scanner.wrapper

import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportManager
import com.simprints.fingerprint.data.domain.fingerprint.CaptureFingerprintStrategy
import com.simprints.fingerprint.data.domain.images.SaveFingerprintImagesStrategy
import com.simprints.fingerprint.scanner.controllers.v2.*
import com.simprints.fingerprint.scanner.domain.*
import com.simprints.fingerprint.scanner.domain.ota.CypressOtaStep
import com.simprints.fingerprint.scanner.domain.ota.StmOtaStep
import com.simprints.fingerprint.scanner.domain.ota.Un20OtaStep
import com.simprints.fingerprint.scanner.domain.versions.ScannerApiVersions
import com.simprints.fingerprint.scanner.domain.versions.ScannerFirmwareVersions
import com.simprints.fingerprint.scanner.domain.versions.ScannerVersion
import com.simprints.fingerprint.scanner.exceptions.safe.NoFingerDetectedException
import com.simprints.fingerprint.scanner.exceptions.safe.OtaFailedException
import com.simprints.fingerprint.scanner.exceptions.safe.ScannerDisconnectedException
import com.simprints.fingerprint.scanner.exceptions.unexpected.UnexpectedScannerException
import com.simprints.fingerprint.scanner.exceptions.unexpected.UnknownScannerIssueException
import com.simprints.fingerprint.scanner.ui.ScannerUiHelper
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.CaptureFingerprintResult
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.Dpi
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.ImageFormatData
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.Single
import io.reactivex.observers.DisposableObserver
import timber.log.Timber
import java.io.IOException
import com.simprints.fingerprintscanner.v2.exceptions.ota.OtaFailedException as ScannerV2OtaFailedException
import com.simprints.fingerprintscanner.v2.scanner.Scanner as ScannerV2

class ScannerWrapperV2(private val scannerV2: ScannerV2,
                       private val scannerUiHelper: ScannerUiHelper,
                       private val macAddress: String,
                       private val scannerInitialSetupHelper: ScannerInitialSetupHelper,
                       private val connectionHelper: ConnectionHelper,
                       private val cypressOtaHelper: CypressOtaHelper,
                       private val stmOtaHelper: StmOtaHelper,
                       private val un20OtaHelper: Un20OtaHelper,
                       private val crashReportManager: FingerprintCrashReportManager) : ScannerWrapper {

    private var scannerVersion: ScannerVersion? = null
    private var batteryInfo: BatteryInfo? = null

    override fun versionInformation(): ScannerVersion =
        scannerVersion ?: ScannerVersion(
            ScannerGeneration.VERO_2,
            ScannerFirmwareVersions.UNKNOWN,
            ScannerApiVersions.UNKNOWN
        )

    override fun batteryInformation(): BatteryInfo = batteryInfo ?: BatteryInfo.UNKNOWN

    override fun connect(): Completable =
        connectionHelper.connectScanner(scannerV2, macAddress)
            .wrapErrorsFromScanner()

    override fun setup(): Completable =
        scannerInitialSetupHelper.setupScannerWithOtaCheck(scannerV2, macAddress,
            { scannerVersion = it }, { batteryInfo = it })
            .wrapErrorsFromScanner()

    override fun disconnect(): Completable =
        connectionHelper.disconnectScanner(scannerV2)
            .wrapErrorsFromScanner()

    override fun sensorWakeUp(): Completable =
        scannerV2
            .ensureUn20State(true)
            .wrapErrorsFromScanner()

    override fun sensorShutDown(): Completable =
        scannerV2
            .ensureUn20State(false)
            .wrapErrorsFromScanner()

    override fun startLiveFeedback() : Completable =
            scannerV2.setScannerLedStateOn()
                //todo record state of un20 led locally
                .andThen(
                    scannerV2
                        .getImageQualityPreview()
                        .flatMapCompletable { quality ->
                            scannerV2.setSmileLedState(scannerUiHelper.deduceLedStateFromQualityForLiveFeedback(quality))
                        }
                        .repeat()
                )

    override fun stopLiveFeedback() : Completable =
            scannerV2.setSmileLedState(scannerUiHelper.idleLedState())
                .andThen(scannerV2.setScannerLedStateDefault())


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

    override fun performCypressOta(): Observable<CypressOtaStep> =
        cypressOtaHelper.performOtaSteps(scannerV2, macAddress)
            .wrapErrorsFromScanner()

    override fun performStmOta(): Observable<StmOtaStep> =
        stmOtaHelper.performOtaSteps(scannerV2, macAddress)
            .wrapErrorsFromScanner()

    override fun performUn20Ota(): Observable<Un20OtaStep> =
        un20OtaHelper.performOtaSteps(scannerV2, macAddress)
            .wrapErrorsFromScanner()

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
            SaveFingerprintImagesStrategy.WSQ_15,
            SaveFingerprintImagesStrategy.WSQ_15_EAGER -> ImageFormatData.WSQ(15)
        }

    private fun Completable.wrapErrorsFromScanner() =
        onErrorResumeNext { Completable.error(wrapErrorFromScanner(it)) }

    private fun <T> Single<T>.wrapErrorsFromScanner() =
        onErrorResumeNext { Single.error(wrapErrorFromScanner(it)) }

    private fun <T> Observable<T>.wrapErrorsFromScanner() =
        onErrorResumeNext { e: Throwable -> Observable.error(wrapErrorFromScanner(e)) }

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
        is ScannerV2OtaFailedException -> { // Wrap the OTA failed exception to fingerprint domain exception
            OtaFailedException("Wrapped OTA failed exception from scanner", e)
        }
        else -> { // Propagate error
            e
        }
    }

    companion object {
        private const val NO_FINGER_IMAGE_QUALITY_THRESHOLD = 10 // The image quality at which we decide a fingerprint wasn't detected
    }
}
