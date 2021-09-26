package com.simprints.fingerprint.scanner.wrapper

import com.simprints.fingerprint.data.domain.fingerprint.CaptureFingerprintStrategy
import com.simprints.fingerprint.data.domain.images.SaveFingerprintImagesStrategy
import com.simprints.fingerprint.scanner.controllers.v2.*
import com.simprints.fingerprint.scanner.domain.*
import com.simprints.fingerprint.scanner.domain.ota.CypressOtaStep
import com.simprints.fingerprint.scanner.domain.ota.StmOtaStep
import com.simprints.fingerprint.scanner.domain.ota.Un20OtaStep
import com.simprints.fingerprint.scanner.domain.versions.ScannerFirmwareVersions
import com.simprints.fingerprint.scanner.domain.versions.ScannerVersion
import com.simprints.fingerprint.scanner.exceptions.safe.NoFingerDetectedException
import com.simprints.fingerprint.scanner.exceptions.safe.OtaFailedException
import com.simprints.fingerprint.scanner.exceptions.safe.ScannerDisconnectedException
import com.simprints.fingerprint.scanner.exceptions.unexpected.UnavailableVero2Feature
import com.simprints.fingerprint.scanner.exceptions.unexpected.UnavailableVero2FeatureException
import com.simprints.fingerprint.scanner.exceptions.unexpected.UnexpectedScannerException
import com.simprints.fingerprint.scanner.exceptions.unexpected.UnknownScannerIssueException
import com.simprints.fingerprint.scanner.ui.ScannerUiHelper
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.CaptureFingerprintResult
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.Dpi
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.ImageFormatData
import com.simprints.fingerprintscanner.v2.exceptions.state.NotConnectedException
import com.simprints.fingerprintscanner.v2.scanner.ScannerExtendedInfoReaderHelper
import com.simprints.infra.logging.Simber
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.Single
import io.reactivex.observers.DisposableObserver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.rx2.await
import java.io.IOException
import kotlin.coroutines.suspendCoroutine
import com.simprints.fingerprintscanner.v2.exceptions.ota.OtaFailedException as ScannerV2OtaFailedException
import com.simprints.fingerprintscanner.v2.scanner.Scanner as ScannerV2

class ScannerWrapperV2(
    private val scannerV2: ScannerV2,
    private val scannerUiHelper: ScannerUiHelper,
    private val macAddress: String,
    private val scannerInitialSetupHelper: ScannerInitialSetupHelper,
    private val connectionHelper: ConnectionHelper,
    private val cypressOtaHelper: CypressOtaHelper,
    private val stmOtaHelper: StmOtaHelper,
    private val un20OtaHelper: Un20OtaHelper
): ScannerWrapper {

    private var scannerVersion: ScannerVersion? = null
    private var batteryInfo: BatteryInfo? = null


    // TODO remove this before PR
    fun setVersion(scannerVersion: ScannerVersion? = null, batteryInfo: BatteryInfo? = null) {
        this.scannerVersion = scannerVersion
        this.batteryInfo = batteryInfo
    }

    /**
     * This function returns the already set scanner version info, or returns a default of UNKNOWN
     * values if the version info hasn't been set.
     *
     * @see setScannerInfoAndCheckAvailableOta
     */
    override fun versionInformation(): ScannerVersion =
        scannerVersion ?: ScannerVersion(
            hardwareVersion = ScannerExtendedInfoReaderHelper.UNKNOWN_HARDWARE_VERSION,
            generation = ScannerGeneration.VERO_2,
            firmware = ScannerFirmwareVersions.UNKNOWN,
        )
    override fun batteryInformation(): BatteryInfo = batteryInfo ?: BatteryInfo.UNKNOWN

    override fun isImageTransferSupported(): Boolean= true

    override suspend fun connect() =
        connectionHelper.connectScanner(scannerV2, macAddress)
            .mapPotentialErrorFromScanner()
            .collect()


    /**
     * This function runs check of available firmware updates, and in turn reads and sets the
     * scanner's firmware versions and battery information.
     *
     * @throws ScannerDisconnectedException
     * @throws UnexpectedScannerException
     * @throws OtaFailedException
     */
    override suspend fun setScannerInfoAndCheckAvailableOta() {
        try {
            scannerInitialSetupHelper.setupScannerWithOtaCheck(
                scannerV2,
                macAddress,
                { scannerVersion = it },
                { batteryInfo = it }
            )
        } catch (ex: Throwable) {
            throw wrapErrorFromScanner(ex)
        }
    }

    override suspend fun disconnect() {
        try {
            connectionHelper.disconnectScanner(scannerV2)
        } catch (ex: Throwable) {
            throw wrapErrorFromScanner(ex)
        }
    }

    /**
     * This function turns on the Un20 sensor (fingerprint sensor), by specifying what state it
     * expects the sensor to be in, represented as a boolean value (true | false -> on | off)
     *
     * @see ensureUn20State
     *
     * @throws ScannerDisconnectedException
     * @throws UnexpectedScannerException
     */
    override suspend fun sensorWakeUp()  {
        try {
            scannerV2.ensureUn20State(true)
        } catch (ex: Throwable) {
            throw wrapErrorFromScanner(ex)
        }
    }


    /**
     * This function turns off the Un20 sensor (fingerprint sensor), by specifying what state it
     * expects the sensor to be in, represented as a boolean value (true | false -> on | off)
     *
     * @see ensureUn20State
     *
     * @throws ScannerDisconnectedException
     * @throws UnexpectedScannerException
     * @throws NotConnectedException
     */
    override suspend fun sensorShutDown() {
        try {
            scannerV2.ensureUn20State(false)
        } catch (ex: Throwable) {
            throw wrapErrorFromScanner(ex)
        }
    }

    override fun isLiveFeedbackAvailable(): Boolean = true

    override suspend fun startLiveFeedback() =
        (if (isLiveFeedbackAvailable()) {
            scannerV2.setScannerLedStateOn()
                .andThen(getImageQualityWhileSettingLEDState())
        } else {
            Completable.error(UnavailableVero2FeatureException(UnavailableVero2Feature.LIVE_FEEDBACK))
        }).await()


    private fun getImageQualityWhileSettingLEDState() =
        scannerV2.getImageQualityPreview().flatMapCompletable { quality ->
            scannerV2.setSmileLedState(scannerUiHelper.deduceLedStateFromQualityForLiveFeedback(quality))
        }.repeat()

    override suspend fun stopLiveFeedback()=
        suspendCoroutine<Unit> {
            if (isLiveFeedbackAvailable()) {
                scannerV2.setSmileLedState(scannerUiHelper.idleLedState())
                    .andThen(scannerV2.setScannerLedStateDefault())
            } else {
                Completable.error(UnavailableVero2FeatureException(UnavailableVero2Feature.LIVE_FEEDBACK))
            }
        }


    private fun ScannerV2.ensureUn20State(desiredState: Boolean): Completable =
        getUn20Status().flatMapCompletable { actualState ->
            when {
                desiredState && !actualState -> turnUn20OnAndAwaitStateChangeEvent()
                !desiredState && actualState -> turnUn20OffAndAwaitStateChangeEvent()
                else -> Completable.complete()
            }
        }

    override suspend fun captureFingerprint(
        captureFingerprintStrategy: CaptureFingerprintStrategy,
        timeOutMs: Int,
        qualityThreshold: Int
    ): CaptureFingerprintResponse =
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
            .await()

    private fun Single<CaptureFingerprintResult>.ensureCaptureResultOkOrError() =
        flatMapCompletable {
            when (it) {
                CaptureFingerprintResult.OK -> Completable.complete()
                CaptureFingerprintResult.FINGERPRINT_NOT_FOUND -> Completable.error(
                    NoFingerDetectedException()
                )
                CaptureFingerprintResult.DPI_UNSUPPORTED -> Completable.error(
                    UnexpectedScannerException("Capture fingerprint DPI unsupported")
                )
                CaptureFingerprintResult.UNKNOWN_ERROR -> Completable.error(
                    UnknownScannerIssueException("Unknown error when capturing fingerprint")
                )
            }
        }

    private fun Single<Int>.setLedStateBasedOnQualityScoreOrInterpretAsNoFingerDetected(
        qualityThreshold: Int
    ) =
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
                    CaptureFingerprintResponse(
                        templateData.template,
                        imageQuality
                    )
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

    override suspend fun acquireImage(saveFingerprintImagesStrategy: SaveFingerprintImagesStrategy): AcquireImageResponse =
        (saveFingerprintImagesStrategy
            .deduceImageAcquisitionFormat()?.let {
                scannerV2.acquireImage(it)
                    .map { imageBytes ->
                        AcquireImageResponse(imageBytes.image)
                    }
            }?.switchIfEmpty(Single.error(NoFingerDetectedException()))
            ?.wrapErrorsFromScanner()
            ?: Single.error(
                IllegalArgumentException("Fingerprint strategy $saveFingerprintImagesStrategy should not call acquireImage in ScannerWrapper")
            )).await()

    override suspend fun setUiIdle() {
            scannerV2
                .setSmileLedState(scannerUiHelper.idleLedState())
                .wrapErrorsFromScanner().await()
    }

    private val triggerListenerToObserverMap =
        mutableMapOf<ScannerTriggerListener, Observer<Unit>>()

    override fun registerTriggerListener(triggerListener: ScannerTriggerListener) {
        triggerListenerToObserverMap[triggerListener] = object: DisposableObserver<Unit>() {
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

    override suspend fun performCypressOta(firmwareVersion: String): Flow<CypressOtaStep> =
            cypressOtaHelper.performOtaSteps(scannerV2, macAddress, firmwareVersion)
                .mapPotentialErrorFromScanner()

    override suspend fun performStmOta(firmwareVersion: String): Flow<StmOtaStep> =
            stmOtaHelper.performOtaSteps(scannerV2, macAddress, firmwareVersion)
                .mapPotentialErrorFromScanner()

    override suspend fun performUn20Ota(firmwareVersion: String): Flow<Un20OtaStep> =
             un20OtaHelper.performOtaSteps(scannerV2, macAddress, firmwareVersion)
                .mapPotentialErrorFromScanner()


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


    private fun <T> Flow<T>.mapPotentialErrorFromScanner() =
        catch { ex -> throw wrapErrorFromScanner(ex) }

    private fun wrapErrorFromScanner(e: Throwable): Throwable = when (e) {
        is IOException -> { // Disconnected or timed-out communications with Scanner
            Simber.d(
                e,
                "IOException in ScannerWrapperV2, transformed to ScannerDisconnectedException"
            )
            ScannerDisconnectedException()
        }
        is IllegalStateException, // We're calling scanner methods out of order somehow
        is IllegalArgumentException -> { // We've received unexpected/invalid bytes from the scanner
            Simber.e(e)
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
        private const val NO_FINGER_IMAGE_QUALITY_THRESHOLD =
            10 // The image quality at which we decide a fingerprint wasn't detected
    }
}
