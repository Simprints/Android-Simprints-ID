package com.simprints.fingerprint.activities.collect

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.ExternalScope
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.core.tools.utils.EncodingUtilsImpl
import com.simprints.core.tools.utils.randomUUID
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintTemplateFormat
import com.simprints.fingerprint.activities.alert.FingerprintAlert
import com.simprints.fingerprint.activities.collect.domain.FingerPriorityDeterminer
import com.simprints.fingerprint.activities.collect.domain.StartingStateDeterminer
import com.simprints.fingerprint.activities.collect.state.*
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.eventData.model.FingerprintCaptureBiometricsEvent
import com.simprints.fingerprint.controllers.core.eventData.model.FingerprintCaptureEvent
import com.simprints.fingerprint.controllers.core.image.FingerprintImageManager
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.data.domain.fingerprint.FingerIdentifier
import com.simprints.fingerprint.data.domain.fingerprint.Fingerprint
import com.simprints.fingerprint.data.domain.fingerprint.toDomain
import com.simprints.fingerprint.data.domain.images.*
import com.simprints.fingerprint.exceptions.unexpected.FingerprintUnexpectedException
import com.simprints.fingerprint.scanner.ScannerManager
import com.simprints.fingerprint.scanner.domain.AcquireImageResponse
import com.simprints.fingerprint.scanner.domain.CaptureFingerprintResponse
import com.simprints.fingerprint.scanner.domain.ScannerTriggerListener
import com.simprints.fingerprint.scanner.exceptions.safe.NoFingerDetectedException
import com.simprints.fingerprint.scanner.exceptions.safe.ScannerDisconnectedException
import com.simprints.fingerprint.scanner.exceptions.safe.ScannerOperationInterruptedException
import com.simprints.fingerprint.scanner.wrapper.ScannerWrapper
import com.simprints.fingerprint.tools.livedata.postEvent
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.FingerprintConfiguration
import com.simprints.infra.logging.LoggingConstants.CrashReportTag
import com.simprints.infra.logging.Simber
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.concurrent.schedule
import kotlin.math.min

@HiltViewModel
class CollectFingerprintsViewModel(
    private val scannerManager: ScannerManager,
    private val configManager: ConfigManager,
    private val imageManager: FingerprintImageManager,
    private val timeHelper: FingerprintTimeHelper,
    private val sessionEventsManager: FingerprintSessionEventsManager,
    private val fingerPriorityDeterminer: FingerPriorityDeterminer,
    private val startingStateDeterminer: StartingStateDeterminer,
    private val encoder: EncodingUtils,
    private val externalScope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    @Inject
    constructor(
        scannerManager: ScannerManager,
        configManager: ConfigManager,
        imageManager: FingerprintImageManager,
        timeHelper: FingerprintTimeHelper,
        sessionEventsManager: FingerprintSessionEventsManager,
        fingerPriorityDeterminer: FingerPriorityDeterminer,
        startingStateDeterminer: StartingStateDeterminer,
        @ExternalScope externalScope: CoroutineScope,
        dispatcherProvider: DispatcherProvider
    ) : this(
        scannerManager,
        configManager,
        imageManager,
        timeHelper,
        sessionEventsManager,
        fingerPriorityDeterminer,
        startingStateDeterminer,
        EncodingUtilsImpl,
        externalScope,
        dispatcherProvider
    )

    lateinit var configuration: FingerprintConfiguration

    val state = MutableLiveData<CollectFingerprintsState>()
    fun state() = state.value
        ?: throw IllegalStateException("No state available in CollectFingerprintsViewModel")

    private fun updateState(block: CollectFingerprintsState.() -> Unit) {
        state.postValue(state.value?.apply { block() })
    }

    private fun updateFingerState(block: FingerState.() -> Unit) {
        updateState {
            fingerStates = fingerStates.toMutableList().also {
                it[currentFingerIndex] = it[currentFingerIndex].apply { block() }
            }.toList()
        }
    }

    private fun updateCaptureState(block: CaptureState.() -> CaptureState) {
        updateFingerState {
            captures = captures.toMutableList().also {
                it[currentCaptureIndex] = it[currentCaptureIndex].run { block() }
            }.toList()
        }
    }

    val vibrate = MutableLiveData<LiveDataEvent>()
    val noFingersScannedToast = MutableLiveData<LiveDataEvent>()
    val launchAlert = MutableLiveData<LiveDataEventWithContent<FingerprintAlert>>()
    val launchReconnect = MutableLiveData<LiveDataEvent>()
    val finishWithFingerprints = MutableLiveData<LiveDataEventWithContent<List<Fingerprint>>>()

    private lateinit var originalFingerprintsToCapture: List<FingerIdentifier>
    private val captureEventIds: MutableMap<CaptureId, String> = mutableMapOf()
    private val imageRefs: MutableMap<CaptureId, FingerprintImageRef?> = mutableMapOf()
    private var lastCaptureStartedAt: Long = 0
    private var scanningTask: Job? = null
    private var imageTransferTask: Job? = null
    private var liveFeedbackTask: Job? = null
    private var stopLiveFeedbackTask: Job? = null
    var liveFeedbackState: LiveFeedbackState? = null

    private data class CaptureId(val finger: FingerIdentifier, val captureIndex: Int)

    private val scannerTriggerListener = ScannerTriggerListener {
        viewModelScope.launch(context = dispatcherProvider.main()) {
            if (state().isShowingConfirmDialog) {
                logScannerMessageForCrashReport("Scanner trigger clicked for confirm dialog")
                handleConfirmFingerprintsAndContinue()
            } else {
                logScannerMessageForCrashReport("Scanner trigger clicked for scanning")
                handleScanButtonPressed()
            }
        }
    }

    fun start(fingerprintsToCapture: List<FingerIdentifier>) {
        viewModelScope.launch {
            configuration = configManager.getProjectConfiguration().fingerprint!!
            originalFingerprintsToCapture = fingerprintsToCapture
            setStartingState()
            startObserverForLiveFeedback()
        }
    }

    private fun startObserverForLiveFeedback() {
        state.observeForever {
            if (!scannerManager.isScannerAvailable) return@observeForever launchReconnect.postEvent()

            when (it.currentCaptureState()) {
                CaptureState.NotCollected, CaptureState.Skipped, is CaptureState.NotDetected, is CaptureState.Collected -> {
                    if (it.isShowingConfirmDialog) stopLiveFeedback(scannerManager.scanner)
                    else startLiveFeedback(scannerManager.scanner)
                }
                is CaptureState.Scanning, is CaptureState.TransferringImage -> pauseLiveFeedback()
            }
        }
    }

    private fun shouldWeDoLiveFeedback(scanner: ScannerWrapper): Boolean =
        scanner.isLiveFeedbackAvailable() && configuration.vero2?.displayLiveFeedback == true


    private fun startLiveFeedback(scanner: ScannerWrapper) {
        if (liveFeedbackState != LiveFeedbackState.START && shouldWeDoLiveFeedback(scanner)) {
            logScannerMessageForCrashReport("startLiveFeedback")
            liveFeedbackState = LiveFeedbackState.START
            stopLiveFeedbackTask?.cancel()
            liveFeedbackTask = viewModelScope.launch(dispatcherProvider.io()) {
                scannerManager.scanner.startLiveFeedback()
            }
        }
    }

    private fun pauseLiveFeedback() {
        logScannerMessageForCrashReport("pauseLiveFeedback")
        liveFeedbackState = LiveFeedbackState.PAUSE
        liveFeedbackTask?.cancel()
    }

    private fun stopLiveFeedback(scanner: ScannerWrapper) {
        if (liveFeedbackState != LiveFeedbackState.STOP && shouldWeDoLiveFeedback(scanner)) {
            logScannerMessageForCrashReport("stopLiveFeedback")
            liveFeedbackState = LiveFeedbackState.STOP
            liveFeedbackTask?.cancel()
            stopLiveFeedbackTask = viewModelScope.launch(dispatcherProvider.io()) {
                scannerManager.scanner.stopLiveFeedback()
            }
        }
    }

    private fun setStartingState() {
        state.value = CollectFingerprintsState(
            startingStateDeterminer.determineStartingFingerStates(originalFingerprintsToCapture)
        )
    }

    fun isImageTransferRequired(): Boolean =
        configuration.vero2?.imageSavingStrategy?.toDomain()?.isImageTransferRequired() ?: false &&
            scannerManager.scanner.isImageTransferSupported()

    fun updateSelectedFinger(index: Int) {
        viewModelScope.launch(dispatcherProvider.io()) {
            scannerManager.scanner.setUiIdle()
        }
        updateState {
            isAskingRescan = false
            isShowingSplashScreen = false
            currentFingerIndex = index
        }
    }

    private fun nudgeToNextFinger() {
        with(state()) {
            if (currentFingerIndex < fingerStates.size - 1) {
                timeHelper.newTimer().schedule(AUTO_SWIPE_DELAY) {
                    updateSelectedFinger(currentFingerIndex + 1)
                }
            }
        }
    }

    fun handleScanButtonPressed() {
        val fingerState = state().currentCaptureState()
        if (fingerState is CaptureState.Collected && fingerState.scanResult.isGoodScan() && !state().isAskingRescan) {
            updateState { isAskingRescan = true }
        } else {
            updateState { isAskingRescan = false }
            if (!isBusyForScanning()) {
                toggleScanning()
            }
        }
    }

    private fun isBusyForScanning(): Boolean = with(state()) {
        currentCaptureState() is CaptureState.TransferringImage || isShowingConfirmDialog || isShowingSplashScreen
    }

    private fun toggleScanning() {
        when (state().currentCaptureState()) {
            is CaptureState.Scanning -> cancelScanning()
            is CaptureState.TransferringImage -> { /* do nothing */
            }
            is CaptureState.NotCollected, is CaptureState.Skipped, is CaptureState.NotDetected, is CaptureState.Collected -> startScanning()
        }
    }

    private fun cancelScanning() {
        updateCaptureState { toNotCollected() }
        scanningTask?.cancel()
        imageTransferTask?.cancel()
    }

    private fun startScanning() {
        updateCaptureState { toScanning() }
        lastCaptureStartedAt = timeHelper.now()
        scanningTask?.cancel()

        scanningTask = viewModelScope.launch(dispatcherProvider.io()) {
            try {
                scannerManager.scanner.setUiIdle()
                val capturedFingerprint = scannerManager.scanner.captureFingerprint(
                    configuration.vero2?.captureStrategy?.toDomain(),
                    scanningTimeoutMs.toInt(),
                    configuration.qualityThreshold
                )

                handleCaptureSuccess(capturedFingerprint)
            } catch (ex: CancellationException) {
                // ignore cancellation exception, but log behaviour
                Simber.d("Fingerprint scanning was cancelled")
            } catch (ex: Throwable) {
                handleScannerCommunicationsError(ex)
            }
        }
    }

    private fun handleCaptureSuccess(captureFingerprintResponse: CaptureFingerprintResponse) {
        val scanResult = ScanResult(
            captureFingerprintResponse.imageQualityScore,
            captureFingerprintResponse.template,
            null,
            configuration.qualityThreshold
        )
        vibrate.postEvent()
        if (shouldProceedToImageTransfer(scanResult.qualityScore)) {
            updateCaptureState { toTransferringImage(scanResult) }
            proceedToImageTransfer()
        } else {
            updateCaptureState { toCollected(scanResult) }
            handleCaptureFinished()
        }
    }

    private fun shouldProceedToImageTransfer(quality: Int) =
        isImageTransferRequired() && (quality >= configuration.qualityThreshold || tooManyBadScans(
            state().currentCaptureState(),
            plusBadScan = true
        ) || configuration.vero2?.imageSavingStrategy?.toDomain()?.isEager() ?: false)

    private fun proceedToImageTransfer() {
        imageTransferTask?.cancel()

        imageTransferTask = viewModelScope.launch(dispatcherProvider.io()) {
            try {
                val acquiredImage = scannerManager.scanner.acquireImage(
                    configuration.vero2?.imageSavingStrategy?.toDomain()

                )
                handleImageTransferSuccess(acquiredImage)
            } catch (ex: Throwable) {
                handleScannerCommunicationsError(ex)
            }
        }
    }

    private fun handleImageTransferSuccess(acquireImageResponse: AcquireImageResponse) {
        vibrate.postEvent()
        updateCaptureState { toCollected(acquireImageResponse.imageBytes) }
        handleCaptureFinished()
    }

    private fun handleCaptureFinished() {
        with(state()) {
            logUiMessageForCrashReport("Finger scanned - ${currentFingerState().id} - ${currentFingerState()}")
            addCaptureAndBiometricEventsInSession()
            saveCurrentImageIfEager()
            if (captureHasSatisfiedTerminalCondition(state().currentCaptureState())) {
                if (fingerHasSatisfiedTerminalCondition(state().currentFingerState())) {
                    resolveFingerTerminalConditionTriggered()
                } else {
                    goToNextCaptureForSameFinger()
                }
            }
        }
    }

    private fun addCaptureAndBiometricEventsInSession() {
        val payloadId = randomUUID()
        with(state().currentFingerState()) {
            val captureEvent = FingerprintCaptureEvent(
                lastCaptureStartedAt,
                timeHelper.now(),
                id,
                configuration.qualityThreshold,
                FingerprintCaptureEvent.buildResult(currentCapture()),
                (currentCapture() as? CaptureState.Collected)?.scanResult?.let {
                    FingerprintCaptureEvent.Fingerprint(
                        id, it.qualityScore, FingerprintTemplateFormat.ISO_19794_2
                    )
                },
                payloadId = payloadId
            )
            val fingerprintCaptureBiometricsEvent =
                if (captureEvent.result == FingerprintCaptureEvent.Result.GOOD_SCAN ||
                    tooManyBadScans(currentCapture(), plusBadScan = false)
                )
                    FingerprintCaptureBiometricsEvent(
                        createdAt = lastCaptureStartedAt,
                        fingerprint = (currentCapture() as CaptureState.Collected).scanResult.let {
                            FingerprintCaptureBiometricsEvent.Fingerprint(
                                finger = id,
                                quality = it.qualityScore,
                                template = encoder.byteArrayToBase64(it.template)
                            )
                        },
                        payloadId = payloadId
                    ) else null

            captureEventIds[CaptureId(id, currentCaptureIndex)] = captureEvent.id

            //It can not be done in background because then SID won't find the last capture event id
            runBlocking {
                sessionEventsManager.addEvent(captureEvent)
                // Because we don't need biometric data that is not used for matching
                fingerprintCaptureBiometricsEvent?.let { sessionEventsManager.addEvent(it) }
            }
        }
    }

    private fun saveCurrentImageIfEager() {
        if (configuration.vero2?.imageSavingStrategy?.toDomain()
                ?.isEager() == true
        ) {
            with(state().currentFingerState()) {
                (currentCapture() as? CaptureState.Collected)?.let { capture ->
                    runBlocking {
                        saveImageIfExists(CaptureId(id, currentCaptureIndex), capture)
                    }
                }
            }
        }
    }

    private fun goToNextCaptureForSameFinger() {
        with(state().currentFingerState()) {
            if (currentCaptureIndex < captures.size - 1) {
                timeHelper.newTimer().schedule(AUTO_SWIPE_DELAY) {
                    updateFingerState { currentCaptureIndex += 1 }
                    viewModelScope.launch(dispatcherProvider.io()) {
                        scannerManager.scanner.setUiIdle()
                    }
                }
            }
        }
    }

    private fun resolveFingerTerminalConditionTriggered() {
        with(state()) {
            if (isScanningEndStateAchieved()) {
                logUiMessageForCrashReport("Confirm fingerprints dialog shown")
                updateState { isShowingConfirmDialog = true }
            } else if (currentCaptureState().let {
                    it is CaptureState.Collected && it.scanResult.isGoodScan()
                }) {
                nudgeToNextFinger()
            } else {
                if (haveNotExceedMaximumNumberOfFingersToAutoAdd()) {
                    showSplashAndNudge(addNewFinger = true)
                } else if (!isOnLastFinger()) {
                    showSplashAndNudge(addNewFinger = false)
                }
            }
        }
    }

    private fun showSplashAndNudge(addNewFinger: Boolean) {
        updateState { isShowingSplashScreen = true }
        timeHelper.newTimer().schedule(TRY_DIFFERENT_FINGER_SPLASH_DELAY) {
            if (addNewFinger) handleAutoAddFinger()
            nudgeToNextFinger()
        }
    }

    private fun handleAutoAddFinger() {
        updateState {
            val nextPriorityFingerId =
                fingerPriorityDeterminer.determineNextPriorityFinger(fingerStates.map { it.id })
            if (nextPriorityFingerId != null) {
                fingerStates = fingerStates + listOf(
                    FingerState(
                        nextPriorityFingerId, listOf(CaptureState.NotCollected)
                    )
                )
            }
        }
    }

    private fun handleScannerCommunicationsError(e: Throwable) {
        when (e) {
            is ScannerOperationInterruptedException -> {
                updateCaptureState { toNotCollected() }
            }
            is ScannerDisconnectedException -> {
                updateCaptureState { toNotCollected() }
                launchReconnect.postEvent()
            }
            is NoFingerDetectedException -> handleNoFingerDetected()
            else -> {
                updateCaptureState { toNotCollected() }
                Simber.e(e)
                launchAlert.postEvent(FingerprintAlert.UNEXPECTED_ERROR)
            }
        }
    }

    private fun handleNoFingerDetected() {
        vibrate.postEvent()
        updateCaptureState { toNotDetected() }
        addCaptureAndBiometricEventsInSession()
    }

    fun handleMissingFingerButtonPressed() {
        if (!state().isShowingSplashScreen) {
            updateCaptureState { toSkipped() }
            lastCaptureStartedAt = timeHelper.now()
            addCaptureAndBiometricEventsInSession()
            resolveFingerTerminalConditionTriggered()
        }
    }

    private fun isScanningEndStateAchieved(): Boolean = with(state()) {
        if (everyActiveFingerHasSatisfiedTerminalCondition()) {
            if (weHaveTheMinimumNumberOfAnyQualityScans() || weHaveTheMinimumNumberOfGoodScans()) {
                return true
            }
        }
        return false
    }

    private fun CollectFingerprintsState.everyActiveFingerHasSatisfiedTerminalCondition(): Boolean =
        fingerStates.all { captureHasSatisfiedTerminalCondition(it.currentCapture()) }

    private fun tooManyBadScans(fingerState: CaptureState, plusBadScan: Boolean): Boolean =
        when (fingerState) {
            is CaptureState.Scanning -> fingerState.numberOfBadScans
            is CaptureState.TransferringImage -> fingerState.numberOfBadScans
            is CaptureState.NotDetected -> fingerState.numberOfBadScans
            is CaptureState.Collected -> fingerState.numberOfBadScans
            else -> 0
        } >= numberOfBadScansRequiredToAutoAddNewFinger - if (plusBadScan) 1 else 0

    private fun CollectFingerprintsState.haveNotExceedMaximumNumberOfFingersToAutoAdd() =
        fingerStates.size < maximumTotalNumberOfFingersForAutoAdding

    private fun CollectFingerprintsState.weHaveTheMinimumNumberOfGoodScans(): Boolean =
        fingerStates.filter {
            val currentCapture = it.currentCapture()
            currentCapture is CaptureState.Collected && currentCapture.scanResult.isGoodScan()
        }.size >= min(targetNumberOfGoodScans, numberOfOriginalFingers())

    private fun CollectFingerprintsState.weHaveTheMinimumNumberOfAnyQualityScans() =
        fingerStates.filter {
            captureHasSatisfiedTerminalCondition(it.currentCapture())
        }.size >= maximumTotalNumberOfFingersForAutoAdding

    private fun numberOfOriginalFingers() = originalFingerprintsToCapture.toSet().size

    private fun captureHasSatisfiedTerminalCondition(captureState: CaptureState) =
        captureState is CaptureState.Collected && (tooManyBadScans(
            captureState, plusBadScan = false
        ) || captureState.scanResult.isGoodScan()) || captureState is CaptureState.Skipped

    private fun fingerHasSatisfiedTerminalCondition(fingerState: FingerState) =
        fingerState.captures.all { captureHasSatisfiedTerminalCondition(it) }

    fun handleConfirmFingerprintsAndContinue() {
        val collectedFingers = state().fingerStates.flatMap {
            it.captures.mapIndexedNotNull { index, capture ->
                if (capture is CaptureState.Collected) Pair(
                    CaptureId(it.id, index), capture
                ) else null
            }
        }

        if (collectedFingers.isEmpty()) {
            noFingersScannedToast.postEvent()
            handleRestart()
        } else {
            if (configuration.vero2?.imageSavingStrategy?.toDomain()
                    ?.isEager() != true
            ) {
                saveImages(collectedFingers)
            }
            proceedToFinish(collectedFingers)
        }
    }

    private fun saveImages(collectedFingers: List<Pair<CaptureId, CaptureState.Collected>>) {
        runBlocking {
            collectedFingers.map { (id, collectedFinger) ->
                saveImageIfExists(id, collectedFinger)
            }
        }
    }

    private fun proceedToFinish(collectedFingers: List<Pair<CaptureId, CaptureState.Collected>>) {
        val domainFingerprints = collectedFingers.map { (id, collectedFinger) ->
            Fingerprint(id.finger, collectedFinger.scanResult.template).also {
                it.imageRef = imageRefs[id]
            }
        }
        finishWithFingerprints.postEvent(domainFingerprints)
    }

    private suspend fun saveImageIfExists(id: CaptureId, collectedFinger: CaptureState.Collected) {
        val captureEventId = captureEventIds[id]

        val imageRef = if (collectedFinger.scanResult.image != null && captureEventId != null) {
            imageManager.save(
                collectedFinger.scanResult.image,
                captureEventId,
                configuration.vero2!!.imageSavingStrategy.toDomain()
                    .deduceFileExtension()
            )
        } else if (collectedFinger.scanResult.image != null && captureEventId == null) {
            Simber.e(FingerprintUnexpectedException("Could not save fingerprint image because of null capture ID"))
            null
        } else null

        imageRefs[id] = imageRef
    }

    fun handleRestart() {
        setStartingState()
    }

    fun handleOnResume() {
        updateState { /* refresh */ }
        runOnScannerOrReconnectScanner { registerTriggerListener(scannerTriggerListener) }
    }

    fun handleOnPause() {
        // Don't try to reconnect scanner in onPause, if scanner is null,
        // reconnection of null scanner will be handled in onResume
        if (scannerManager.isScannerAvailable) {
            scannerManager.scanner.let { scanner ->
                viewModelScope.launch {
                    stopLiveFeedback(scanner)
                }
                scanner.unregisterTriggerListener(scannerTriggerListener)
            }
        }
    }

    fun handleOnBackPressed() {
        if (state().currentCaptureState().isCommunicating()) {
            cancelScanning()
        }
    }

    override fun onCleared() {
        super.onCleared()
        cancelScanning()
        externalScope.launch {
            scannerManager.scanner.disconnect()
        }
    }

    fun logUiMessageForCrashReport(message: String) {
        Simber.tag(CrashReportTag.FINGER_CAPTURE.name).i(message)
    }

    private fun logScannerMessageForCrashReport(message: String) {
        Simber.tag(CrashReportTag.FINGER_CAPTURE.name).i(message)
    }

    private fun <T> runOnScannerOrReconnectScanner(block: ScannerWrapper.() -> T) {
        if (scannerManager.isScannerAvailable) scannerManager.scanner.block()
        else launchReconnect.postEvent()
    }

    companion object {
        const val targetNumberOfGoodScans = 2
        const val maximumTotalNumberOfFingersForAutoAdding = 4
        const val numberOfBadScansRequiredToAutoAddNewFinger = 3

        const val scanningTimeoutMs = 3000L
        const val imageTransferTimeoutMs = 3000L

        const val AUTO_SWIPE_DELAY: Long = 500

        const val TRY_DIFFERENT_FINGER_SPLASH_DELAY: Long = 2000
    }
}
