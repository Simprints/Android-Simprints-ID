package com.simprints.fingerprint.capture.screen

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.ExternalScope
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.core.tools.extentions.updateOnIndex
import com.simprints.core.tools.time.TimeHelper
import com.simprints.fingerprint.capture.FingerprintCaptureResult
import com.simprints.fingerprint.capture.extensions.isEager
import com.simprints.fingerprint.capture.extensions.isImageTransferRequired
import com.simprints.fingerprint.capture.extensions.toInt
import com.simprints.fingerprint.capture.models.CaptureId
import com.simprints.fingerprint.capture.state.CaptureState
import com.simprints.fingerprint.capture.state.CaptureState.NotCollected.toNotCollected
import com.simprints.fingerprint.capture.state.CollectFingerprintsState
import com.simprints.fingerprint.capture.state.FingerState
import com.simprints.fingerprint.capture.state.LiveFeedbackState
import com.simprints.fingerprint.capture.state.ScanResult
import com.simprints.fingerprint.capture.usecase.AddCaptureEventsUseCase
import com.simprints.fingerprint.capture.usecase.GetNextFingerToAddUseCase
import com.simprints.fingerprint.capture.usecase.GetStartStateUseCase
import com.simprints.fingerprint.capture.usecase.SaveImageUseCase
import com.simprints.fingerprint.infra.biosdk.BioSdkWrapper
import com.simprints.fingerprint.infra.scanner.ScannerManager
import com.simprints.fingerprint.infra.scanner.domain.ScannerGeneration
import com.simprints.fingerprint.infra.scanner.domain.ScannerTriggerListener
import com.simprints.fingerprint.infra.scanner.domain.fingerprint.AcquireFingerprintImageResponse
import com.simprints.fingerprint.infra.scanner.domain.fingerprint.AcquireFingerprintTemplateResponse
import com.simprints.fingerprint.infra.scanner.exceptions.safe.NoFingerDetectedException
import com.simprints.fingerprint.infra.scanner.exceptions.safe.ScannerDisconnectedException
import com.simprints.fingerprint.infra.scanner.exceptions.safe.ScannerOperationInterruptedException
import com.simprints.fingerprint.infra.scanner.wrapper.ScannerWrapper
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.images.model.Path
import com.simprints.infra.images.model.SecuredImageRef
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.FINGER_CAPTURE
import com.simprints.infra.logging.Simber
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import kotlin.math.min

@HiltViewModel
internal class FingerprintCaptureViewModel @Inject constructor(
    private val scannerManager: ScannerManager,
    private val configManager: ConfigManager,
    private val timeHelper: TimeHelper,
    private val bioSdk: BioSdkWrapper,
    private val saveImage: SaveImageUseCase,
    private val getNextFingerToAdd: GetNextFingerToAddUseCase,
    private val getStartState: GetStartStateUseCase,
    private val addCaptureEvents: AddCaptureEventsUseCase,
    @ExternalScope private val externalScope: CoroutineScope,
) : ViewModel() {

    private var captureStatusChecked = false

    lateinit var configuration: FingerprintConfiguration
    private lateinit var bioSdkConfiguration: FingerprintConfiguration.FingerprintSdkConfiguration

    private var state: CollectFingerprintsState = CollectFingerprintsState.EMPTY
        private set(value) {
            field = value
            _stateLiveData.postValue(value)
        }
    private val _stateLiveData = MutableLiveData<CollectFingerprintsState>()
    val stateLiveData: LiveData<CollectFingerprintsState> = _stateLiveData

    private fun updateState(state: (CollectFingerprintsState) -> CollectFingerprintsState) {
        this.state = state(this.state)
    }

    private fun updateFingerState(newFingerState: (FingerState) -> FingerState) = updateState {
        it.copy(
            fingerStates = it.fingerStates.updateOnIndex(
                index = it.currentFingerIndex,
                newItem = newFingerState
            )
        )
    }

    private fun updateCaptureState(newCaptureState: (CaptureState) -> CaptureState) =
        updateFingerState {
            it.copy(
                captures = it.captures.updateOnIndex(
                    index = it.currentCaptureIndex,
                    newItem = newCaptureState
                )
            )
        }

    val vibrate: LiveData<LiveDataEvent>
        get() = _vibrate
    private val _vibrate = MutableLiveData<LiveDataEvent>()

    val noFingersScannedToast: LiveData<LiveDataEvent>
        get() = _noFingersScannedToast
    private val _noFingersScannedToast = MutableLiveData<LiveDataEvent>()

    val launchAlert: LiveData<LiveDataEvent>
        get() = _launchAlert
    private val _launchAlert = MutableLiveData<LiveDataEvent>()

    val launchReconnect: LiveData<LiveDataEvent>
        get() = _launchReconnect
    private val _launchReconnect = MutableLiveData<LiveDataEvent>()

    val finishWithFingerprints: LiveData<LiveDataEventWithContent<FingerprintCaptureResult>>
        get() = _finishWithFingerprints
    private val _finishWithFingerprints =
        MutableLiveData<LiveDataEventWithContent<FingerprintCaptureResult>>()

    private lateinit var originalFingerprintsToCapture: List<IFingerIdentifier>
    private val captureEventIds: MutableMap<CaptureId, String> = mutableMapOf()
    private val imageRefs: MutableMap<CaptureId, SecuredImageRef?> = mutableMapOf()
    private var lastCaptureStartedAt: Long = 0
    private var hasStarted: Boolean = false

    @VisibleForTesting
    internal var liveFeedbackState: LiveFeedbackState? = null

    private var scanningTask: Job? = null
    private var imageTransferTask: Job? = null
    private var liveFeedbackTask: Job? = null
    private var stopLiveFeedbackTask: Job? = null

    private val scannerTriggerListener = ScannerTriggerListener {
        viewModelScope.launch {
            if (state.isShowingConfirmDialog) {
                Simber.tag(FINGER_CAPTURE.name).i("Scanner trigger clicked for confirm dialog")
                handleConfirmFingerprintsAndContinue()
            } else {
                Simber.tag(FINGER_CAPTURE.name).i("Scanner trigger clicked for scanning")
                handleScanButtonPressed()
            }
        }
    }


    fun start(fingerprintsToCapture: List<IFingerIdentifier>) {
        if (!hasStarted) {
            hasStarted = true

            runBlocking {
                bioSdk.initialize()
                // Configuration must be initialised when start returns for UI to be initialised correctly,
                // and since fetching happens on IO thread execution must be suspended until it is available
                configuration = configManager.getProjectConfiguration().fingerprint!!
                bioSdkConfiguration = configuration.bioSdkConfiguration
            }

            originalFingerprintsToCapture = fingerprintsToCapture
            setStartingState(fingerprintsToCapture)
            startObserverForLiveFeedback()
        }
    }

    fun checkScannerConnectionStatus(): ScannerConnectionStatus {
        if (captureStatusChecked) return ScannerConnectionStatus.Started
        captureStatusChecked = true

        return if (scannerManager.isScannerConnected) {
            ScannerConnectionStatus.Connected
        } else {
            ScannerConnectionStatus.NotConnected
        }
    }

    private fun launchReconnect() {
        if (!state.isShowingConnectionScreen) {
            updateState {
                it.copy(isShowingConnectionScreen = true)
            }
            _launchReconnect.send()
        }
    }

    private fun startObserverForLiveFeedback() {
        stateLiveData.observeForever {
            if (!scannerManager.isScannerConnected) {
                launchReconnect()
                return@observeForever
            }

            when (it.currentCaptureState()) {
                is CaptureState.Scanning,
                is CaptureState.TransferringImage -> pauseLiveFeedback()

                CaptureState.NotCollected,
                CaptureState.Skipped,
                is CaptureState.NotDetected,
                is CaptureState.Collected -> {
                    if (it.isShowingConfirmDialog) stopLiveFeedback()
                    else startLiveFeedback(scannerManager.scanner)
                }
            }
        }
    }

    private fun shouldWeDoLiveFeedback(scanner: ScannerWrapper): Boolean =
        scanner.isLiveFeedbackAvailable() && bioSdkConfiguration.vero2?.displayLiveFeedback == true


    private fun startLiveFeedback(scanner: ScannerWrapper) {
        if (liveFeedbackState != LiveFeedbackState.START && shouldWeDoLiveFeedback(scanner)) {
            Simber.tag(FINGER_CAPTURE.name).i("startLiveFeedback")
            liveFeedbackState = LiveFeedbackState.START
            stopLiveFeedbackTask?.cancel()
            liveFeedbackTask = viewModelScope.launch {
                scannerManager.scanner.startLiveFeedback()
            }
        }
    }

    private fun pauseLiveFeedback() {
        Simber.tag(FINGER_CAPTURE.name).i("pauseLiveFeedback")
        liveFeedbackState = LiveFeedbackState.PAUSE
        liveFeedbackTask?.cancel()
    }

    private fun stopLiveFeedback() {
        if (liveFeedbackState != null && liveFeedbackState != LiveFeedbackState.STOP) {
            Simber.tag(FINGER_CAPTURE.name).i("stopLiveFeedback")
            liveFeedbackState = LiveFeedbackState.STOP
            liveFeedbackTask?.cancel()
            stopLiveFeedbackTask = viewModelScope.launch {
                scannerManager.scanner.stopLiveFeedback()
            }
        }
    }

    private fun setStartingState(fingerprintsToCapture: List<IFingerIdentifier>) {
        val initialState = CollectFingerprintsState.EMPTY.copy(
            fingerStates = getStartState(fingerprintsToCapture)
        )
        state = initialState
        _stateLiveData.value = initialState
    }

    fun isImageTransferRequired(): Boolean =
        bioSdkConfiguration.vero2?.imageSavingStrategy?.isImageTransferRequired() ?: false &&
            scannerManager.scanner.isImageTransferSupported()

    fun updateSelectedFinger(index: Int) {
        viewModelScope.launch {
            try {
                scannerManager.scanner.setUiIdle()
            } catch (ex: Exception) {
                handleScannerCommunicationsError(ex)
            }
        }
        updateState {
            it.copy(
                isAskingRescan = false,
                isShowingSplashScreen = false,
                currentFingerIndex = index
            )
        }
    }

    private fun nudgeToNextFinger() = with(state) {
        if (currentFingerIndex < fingerStates.size - 1) {
            viewModelScope.launch {
                delay(AUTO_SWIPE_DELAY)
                updateSelectedFinger(currentFingerIndex + 1)
            }
        }
    }

    fun handleScanButtonPressed() {
        val state = state
        val fingerState = this.state.currentCaptureState()
        if (fingerState is CaptureState.Collected && fingerState.scanResult.isGoodScan() && state.isAskingRescan.not()) {
            updateState { it.copy(isAskingRescan = true) }
        } else {
            updateState { it.copy(isAskingRescan = false) }
            if (!isBusyForScanning()) {
                toggleScanning()
            }
        }
    }

    private fun isBusyForScanning(): Boolean = with(state) {
        currentCaptureState() is CaptureState.TransferringImage || isShowingConfirmDialog || isShowingSplashScreen
    }

    private fun toggleScanning() {
        when (state.currentCaptureState()) {
            is CaptureState.Scanning -> cancelScanning()
            is CaptureState.TransferringImage -> { /* do nothing */
            }

            is CaptureState.NotCollected, is CaptureState.Skipped, is CaptureState.NotDetected, is CaptureState.Collected -> startScanning()
        }
    }

    private fun cancelScanning() {
        updateCaptureState(CaptureState::toNotCollected)
        scanningTask?.cancel()
        imageTransferTask?.cancel()
    }

    private fun startScanning() {
        updateCaptureState(CaptureState::toScanning)
        lastCaptureStartedAt = timeHelper.now()
        scanningTask?.cancel()

        scanningTask = viewModelScope.launch {
            try {
                scannerManager.scanner.setUiIdle()
                val capturedFingerprint = bioSdk.acquireFingerprintTemplate(
                    bioSdkConfiguration.vero2?.captureStrategy?.toInt(),
                    scanningTimeoutMs.toInt(),
                    qualityThreshold()
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

    private fun handleCaptureSuccess(acquireFingerprintTemplateResponse: AcquireFingerprintTemplateResponse) {
        val scanResult = ScanResult(
            acquireFingerprintTemplateResponse.imageQualityScore,
            acquireFingerprintTemplateResponse.template,
            acquireFingerprintTemplateResponse.templateFormat,
            null,
            qualityThreshold()
        )
        _vibrate.send()
        if (shouldProceedToImageTransfer(scanResult.qualityScore)) {
            updateCaptureState { it.toTransferringImage(scanResult) }
            proceedToImageTransfer()
        } else {
            updateCaptureState { it.toCollected(scanResult) }
            handleCaptureFinished()
        }
    }

    private fun shouldProceedToImageTransfer(quality: Int) = isImageTransferRequired() &&
        (quality >= qualityThreshold() ||
            tooManyBadScans(state.currentCaptureState(), plusBadScan = true) ||
            bioSdkConfiguration.vero2?.imageSavingStrategy?.isEager() ?: false)

    private fun proceedToImageTransfer() {
        imageTransferTask?.cancel()

        imageTransferTask = viewModelScope.launch {
            try {
                val acquiredImage = bioSdk.acquireFingerprintImage()
                handleImageTransferSuccess(acquiredImage)
            } catch (ex: Throwable) {
                handleScannerCommunicationsError(ex)
            }
        }
    }

    private fun handleImageTransferSuccess(acquireFingerprintImageResponse: AcquireFingerprintImageResponse) {
        _vibrate.send()
        updateCaptureState { it.toCollected(acquireFingerprintImageResponse.imageBytes) }
        handleCaptureFinished()
    }

    private fun handleCaptureFinished() = with(state) {
        Simber.tag(FINGER_CAPTURE.name)
            .i("Finger scanned - ${currentFingerState().id} - ${currentFingerState()}")
        addCaptureAndBiometricEventsInSession()
        saveCurrentImageIfEager()
        if (captureHasSatisfiedTerminalCondition(currentCaptureState())) {
            if (fingerHasSatisfiedTerminalCondition(currentFingerState())) {
                resolveFingerTerminalConditionTriggered()
            } else {
                goToNextCaptureForSameFinger()
            }
        }
    }

    private fun addCaptureAndBiometricEventsInSession() {
        val fingerState = state.currentFingerState()
        val captureState = fingerState.currentCapture()

        //It can not be done in background because then SID won't find the last capture event id
        val payloadId = runBlocking {
            addCaptureEvents(
                lastCaptureStartedAt,
                fingerState,
                qualityThreshold(),
                tooManyBadScans(captureState, plusBadScan = false)
            )
        }
        captureEventIds[CaptureId(fingerState.id, fingerState.currentCaptureIndex)] = payloadId
    }

    private fun saveCurrentImageIfEager() {
        if (bioSdkConfiguration.vero2?.imageSavingStrategy?.isEager() == true) {
            with(state.currentFingerState()) {
                (currentCapture() as? CaptureState.Collected)?.let { capture ->
                    runBlocking {
                        saveImageIfExists(CaptureId(id, currentCaptureIndex), capture)
                    }
                }
            }
        }
    }

    private fun goToNextCaptureForSameFinger() = with(state.currentFingerState()) {
        if (currentCaptureIndex < captures.size - 1) {
            viewModelScope.launch {
                delay(AUTO_SWIPE_DELAY)
                updateFingerState { it.copy(currentCaptureIndex = currentCaptureIndex + 1) }
                try {
                    scannerManager.scanner.setUiIdle()
                } catch (ex: Exception) {
                    handleScannerCommunicationsError(ex)
                }
            }
        }
    }

    private fun resolveFingerTerminalConditionTriggered() = with(state) {
        if (isScanningEndStateAchieved()) {
            Simber.tag(FINGER_CAPTURE.name).i("Confirm fingerprints dialog shown")
            updateState { it.copy(isShowingConfirmDialog = true) }
        } else if (currentCaptureState().let { it is CaptureState.Collected && it.scanResult.isGoodScan() }) {
            nudgeToNextFinger()
        } else {
            if (haveNotExceedMaximumNumberOfFingersToAutoAdd()) {
                showSplashAndNudge(addNewFinger = true)
            } else if (!isOnLastFinger()) {
                showSplashAndNudge(addNewFinger = false)
            }
        }
    }

    private fun showSplashAndNudge(addNewFinger: Boolean) {
        updateState { it.copy(isShowingSplashScreen = true) }
        viewModelScope.launch {
            delay(TRY_DIFFERENT_FINGER_SPLASH_DELAY)
            if (addNewFinger) handleAutoAddFinger()
            nudgeToNextFinger()
        }
    }

    private fun handleAutoAddFinger() = updateState { state ->
        when (val nextPriorityFingerId = getNextFingerToAdd(state.fingerStates.map { it.id })) {
            null -> state
            else -> {
                val newFingerState = FingerState(
                    id = nextPriorityFingerId,
                    captures = listOf(CaptureState.NotCollected)
                )
                state.copy(fingerStates = state.fingerStates + newFingerState)
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
                launchReconnect()
            }

            is NoFingerDetectedException -> handleNoFingerDetected()
            else -> {
                updateCaptureState { toNotCollected() }
                Simber.e(e)
                _launchAlert.send()
            }
        }
    }

    private fun handleNoFingerDetected() {
        _vibrate.send()
        updateCaptureState(CaptureState::toNotDetected)
        addCaptureAndBiometricEventsInSession()
    }

    fun handleMissingFingerButtonPressed() {
        if (state.isShowingSplashScreen.not()) {
            updateCaptureState(CaptureState::toSkipped)
            lastCaptureStartedAt = timeHelper.now()
            addCaptureAndBiometricEventsInSession()
            resolveFingerTerminalConditionTriggered()
        }
    }

    private fun isScanningEndStateAchieved(): Boolean = with(state) {
        return everyActiveFingerHasSatisfiedTerminalCondition() && (weHaveTheMinimumNumberOfAnyQualityScans() || weHaveTheMinimumNumberOfGoodScans())
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
        val collectedFingers = state.fingerStates.flatMap {
            it.captures.mapIndexedNotNull { index, capture ->
                if (capture is CaptureState.Collected) Pair(
                    CaptureId(it.id, index),
                    capture
                ) else null
            }
        }

        if (collectedFingers.isEmpty()) {
            _noFingersScannedToast.send()
            handleRestart()
        } else {
            if (bioSdkConfiguration.vero2?.imageSavingStrategy?.let { !it.isEager() && it.isImageTransferRequired() } == true) {
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
        val resultItems = collectedFingers.map { (captureId, collectedFinger) ->
            FingerprintCaptureResult.Item(
                identifier = captureId.finger,
                sample = FingerprintCaptureResult.Sample(
                    fingerIdentifier = captureId.finger,
                    template = collectedFinger.scanResult.template,
                    templateQualityScore = collectedFinger.scanResult.qualityScore,
                    imageRef = imageRefs[captureId]?.let { SecuredImageRef(Path(it.relativePath.parts)) },
                    format = collectedFinger.scanResult.templateFormat
                )
            )
        }
        _finishWithFingerprints.send(FingerprintCaptureResult(resultItems))
    }

    private suspend fun saveImageIfExists(id: CaptureId, collectedFinger: CaptureState.Collected) {
        val captureEventId = captureEventIds[id]
        val imageRef = saveImage(bioSdkConfiguration.vero2!!, captureEventId, collectedFinger)
        imageRefs[id] = imageRef
    }

    fun handleRestart() {
        setStartingState(originalFingerprintsToCapture)
    }

    fun handleOnResume() {
        updateState {
            /* refresh */
            it.copy(
                isShowingSplashScreen = false,
                isShowingConnectionScreen = false
            )
        }
        runOnScannerOrReconnectScanner { registerTriggerListener(scannerTriggerListener) }
    }

    fun handleOnPause() {
        // Don't try to reconnect scanner in onPause, if scanner is null,
        // reconnection of null scanner will be handled in onResume
        if (scannerManager.isScannerConnected) {
            stopLiveFeedback()
            scannerManager.scanner.unregisterTriggerListener(scannerTriggerListener)
        }
    }

    fun handleOnBackPressed() {
        if (state.currentCaptureState().isCommunicating()) {
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

    private fun <T> runOnScannerOrReconnectScanner(block: ScannerWrapper.() -> T) {
        if (scannerManager.isScannerConnected) scannerManager.scanner.block()
        else launchReconnect()
    }

    private fun qualityThreshold(): Int =
        if (scannerManager.scanner.versionInformation().generation == ScannerGeneration.VERO_1)
            bioSdkConfiguration.vero1!!.qualityThreshold
        else
            bioSdkConfiguration.vero2!!.qualityThreshold


    enum class ScannerConnectionStatus {
        NotConnected, Connected, Started;
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
