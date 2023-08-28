package com.simprints.fingerprint.scanner.wrapper

import android.annotation.SuppressLint
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.CaptureFingerprintResult
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Dpi
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.ImageFormatData
import com.simprints.fingerprint.infra.scanner.v2.exceptions.state.NotConnectedException
import com.simprints.fingerprint.infra.scanner.v2.scanner.ScannerExtendedInfoReaderHelper
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
import com.simprints.infra.logging.Simber
import io.reactivex.Completable
import io.reactivex.Observer
import io.reactivex.Single
import io.reactivex.observers.DisposableObserver
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import java.io.IOException
import com.simprints.fingerprint.infra.scanner.v2.exceptions.ota.OtaFailedException as ScannerV2OtaFailedException
import com.simprints.fingerprint.infra.scanner.v2.scanner.Scanner as ScannerV2

class ScannerWrapperV2(
    private val scannerV2: ScannerV2,
    private val scannerUiHelper: ScannerUiHelper,
    private val macAddress: String,
    private val scannerInitialSetupHelper: ScannerInitialSetupHelper,
    private val connectionHelper: ConnectionHelper,
    private val cypressOtaHelper: CypressOtaHelper,
    private val stmOtaHelper: StmOtaHelper,
    private val un20OtaHelper: Un20OtaHelper,
    private val ioDispatcher: CoroutineDispatcher,
) : ScannerWrapper {

    private var scannerVersion: ScannerVersion? = null
    private var batteryInfo: BatteryInfo? = null

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

    override fun isImageTransferSupported(): Boolean = true

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
    override suspend fun setScannerInfoAndCheckAvailableOta() = withContext(ioDispatcher) {
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

    override suspend fun disconnect() = withContext(ioDispatcher) {
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
    override suspend fun sensorWakeUp() = withContext(ioDispatcher) {
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
    override suspend fun sensorShutDown() = withContext(ioDispatcher) {
        try {
            scannerV2.ensureUn20State(false)
        } catch (ex: Throwable) {
            throw wrapErrorFromScanner(ex)
        }
    }

    override fun isLiveFeedbackAvailable(): Boolean = true

    override suspend fun startLiveFeedback() = withContext(ioDispatcher) {
        (if (isLiveFeedbackAvailable()) {
            scannerV2.setScannerLedStateOn()
                .andThen(getImageQualityWhileSettingLEDState())
                .onErrorComplete()
        } else {
            Completable.error(UnavailableVero2FeatureException(UnavailableVero2Feature.LIVE_FEEDBACK))
        })
            .await()
    }

    private fun getImageQualityWhileSettingLEDState() =
        scannerV2.getImageQualityPreview().flatMapCompletable { quality ->
            scannerV2.setSmileLedState(scannerUiHelper.deduceLedStateFromQualityForLiveFeedback(quality))
        }.repeat()

    @SuppressLint("CheckResult")
    override suspend fun stopLiveFeedback(): Unit = withContext(ioDispatcher) {
        if (isLiveFeedbackAvailable()) {
            scannerV2
                .setSmileLedState(scannerUiHelper.idleLedState())
                .onErrorComplete()
            scannerV2
                .setScannerLedStateDefault()
                .onErrorComplete()
        } else {
            throw UnavailableVero2FeatureException(UnavailableVero2Feature.LIVE_FEEDBACK)
        }
    }


    private suspend fun ScannerV2.ensureUn20State(desiredState: Boolean) = withContext(ioDispatcher) {
        getUn20Status().flatMapCompletable { actualState ->
            when {
                desiredState && !actualState -> turnUn20OnAndAwaitStateChangeEvent()
                !desiredState && actualState -> turnUn20OffAndAwaitStateChangeEvent()
                else -> Completable.complete()
            }
        }.await()
    }

    override suspend fun captureFingerprint(
        captureDpi: Dpi?,
        timeOutMs: Int,
        qualityThreshold: Int
    ): CaptureFingerprintResponse = withContext(ioDispatcher) {
        scannerV2
            .captureFingerprint(captureDpi!!)
            .ensureCaptureResultOkOrError()
            .andThen(scannerV2.getImageQualityScore())
            .switchIfEmpty(Single.error(NoFingerDetectedException()))
            .setLedStateBasedOnQualityScoreOrInterpretAsNoFingerDetected(qualityThreshold)
            .acquireTemplateAndAssembleResponse()
            .switchIfEmpty(Single.error(NoFingerDetectedException()))
            .ifNoFingerDetectedThenSetBadScanLedState()
            .wrapErrorsFromScanner()
            .await()
    }

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
                        templateData.template, templateFormat, imageQuality
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

    override suspend fun acquireImage(): AcquireImageResponse {
        return withContext(ioDispatcher) {
            scannerV2.acquireImage(IMAGE_FORMAT).map { imageBytes ->
                AcquireImageResponse(imageBytes.image)
            }.switchIfEmpty(Single.error(NoFingerDetectedException())).wrapErrorsFromScanner()
                .await()
        }
    }

    override suspend fun setUiIdle() = withContext(ioDispatcher) {
        scannerV2
            .setSmileLedState(scannerUiHelper.idleLedState())
            .wrapErrorsFromScanner()
            .await()
    }

    private val triggerListenerToObserverMap =
        mutableMapOf<ScannerTriggerListener, Observer<Unit>>()

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

    override fun performCypressOta(firmwareVersion: String): Flow<CypressOtaStep> =
        cypressOtaHelper.performOtaSteps(scannerV2, macAddress, firmwareVersion)
            .mapPotentialErrorFromScanner()
            .flowOn(ioDispatcher)

    override fun performStmOta(firmwareVersion: String): Flow<StmOtaStep> =
        stmOtaHelper.performOtaSteps(scannerV2, macAddress, firmwareVersion)
            .mapPotentialErrorFromScanner()
            .flowOn(ioDispatcher)

    override fun performUn20Ota(firmwareVersion: String): Flow<Un20OtaStep> =
        un20OtaHelper.performOtaSteps(scannerV2, macAddress, firmwareVersion)
            .mapPotentialErrorFromScanner()
            .flowOn(ioDispatcher)


    private fun Completable.wrapErrorsFromScanner() =
        onErrorResumeNext { Completable.error(wrapErrorFromScanner(it)) }

    private fun <T> Single<T>.wrapErrorsFromScanner() =
        onErrorResumeNext { Single.error(wrapErrorFromScanner(it)) }


    private fun <T> Flow<T>.mapPotentialErrorFromScanner() =
        catch { ex -> throw wrapErrorFromScanner(ex) }

    private fun wrapErrorFromScanner(e: Throwable): Throwable = when (e) {
        is NotConnectedException,
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
        private val IMAGE_FORMAT = ImageFormatData.WSQ(15)
    }
}
