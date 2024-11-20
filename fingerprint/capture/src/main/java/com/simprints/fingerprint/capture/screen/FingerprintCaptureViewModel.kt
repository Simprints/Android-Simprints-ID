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
import com.simprints.core.tools.time.Timestamp
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
import com.simprints.fingerprint.capture.usecase.IsNoFingerDetectedLimitReachedUseCase
import com.simprints.fingerprint.capture.usecase.SaveImageUseCase
import com.simprints.fingerprint.infra.basebiosdk.exceptions.BioSdkException
import com.simprints.fingerprint.infra.biosdk.BioSdkWrapper
import com.simprints.fingerprint.infra.biosdk.ResolveBioSdkWrapperUseCase
import com.simprints.fingerprint.infra.scanner.ScannerManager
import com.simprints.fingerprint.infra.scanner.capture.FingerprintScanningStatusTracker
import com.simprints.fingerprint.infra.scanner.domain.ScannerGeneration
import com.simprints.fingerprint.infra.scanner.domain.ScannerTriggerListener
import com.simprints.fingerprint.infra.scanner.domain.fingerprint.AcquireFingerprintImageResponse
import com.simprints.fingerprint.infra.scanner.domain.fingerprint.AcquireFingerprintTemplateResponse
import com.simprints.fingerprint.infra.scanner.exceptions.safe.NoFingerDetectedException
import com.simprints.fingerprint.infra.scanner.exceptions.safe.ScannerDisconnectedException
import com.simprints.fingerprint.infra.scanner.exceptions.safe.ScannerOperationInterruptedException
import com.simprints.fingerprint.infra.scanner.wrapper.ScannerWrapper
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.config.store.models.Vero2Configuration.ImageSavingStrategy.EAGER
import com.simprints.infra.config.store.models.Vero2Configuration.ImageSavingStrategy.NEVER
import com.simprints.infra.config.store.models.Vero2Configuration.ImageSavingStrategy.ONLY_GOOD_SCAN
import com.simprints.infra.config.store.models.Vero2Configuration.ImageSavingStrategy.ONLY_USED_IN_REFERENCE
import com.simprints.infra.config.store.models.Vero2Configuration.LedsMode.LIVE_QUALITY_FEEDBACK
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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import kotlin.math.min

@HiltViewModel
internal class FingerprintCaptureViewModel @Inject constructor(
    private val scannerManager: ScannerManager,
    private val configManager: ConfigManager,
    private val timeHelper: TimeHelper,
    private val resolveBioSdkWrapperUseCase: ResolveBioSdkWrapperUseCase,
    private val saveImage: SaveImageUseCase,
    private val getNextFingerToAdd: GetNextFingerToAddUseCase,
    private val getStartState: GetStartStateUseCase,
    private val addCaptureEvents: AddCaptureEventsUseCase,
    private val tracker: FingerprintScanningStatusTracker,
    private val isNoFingerDetectedLimitReachedUseCase: IsNoFingerDetectedLimitReachedUseCase,
    @ExternalScope private val externalScope: CoroutineScope,
) : ViewModel() {

    lateinit var configuration: FingerprintConfiguration
    private lateinit var bioSdkConfiguration: FingerprintConfiguration.FingerprintSdkConfiguration

    private lateinit var bioSdkWrapper: BioSdkWrapper
    private var state: CollectFingerprintsState = CollectFingerprintsState.EMPTY
        set(value) {
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

    val invalidLicense: LiveData<LiveDataEvent>
        get() = _invalidLicense
    private val _invalidLicense = MutableLiveData<LiveDataEvent>()

    val finishWithFingerprints: LiveData<LiveDataEventWithContent<FingerprintCaptureResult>>
        get() = _finishWithFingerprints
    private val _finishWithFingerprints =
        MutableLiveData<LiveDataEventWithContent<FingerprintCaptureResult>>()

    private lateinit var originalFingerprintsToCapture: List<IFingerIdentifier>
    private val captureEventIds: MutableMap<CaptureId, String> = mutableMapOf()
    private val imageRefs: MutableMap<CaptureId, SecuredImageRef?> = mutableMapOf()
    private var lastCaptureStartedAt: Timestamp = Timestamp(0L)
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


    private fun start(
        fingerprintsToCapture: List<IFingerIdentifier>,
        fingerprintSdk: FingerprintConfiguration.BioSdk,
    ) {
        if (!hasStarted) {
            hasStarted = true

            runBlocking {
                // Configuration must be initialised when start returns for UI to be initialised correctly,
                // and since fetching happens on IO thread execution must be suspended until it is available
                configuration = configManager.getProjectConfiguration().fingerprint!!
                initBioSdk(fingerprintSdk)
            }

            originalFingerprintsToCapture = fingerprintsToCapture
            setStartingState(fingerprintsToCapture)
            startObserverForLiveFeedback()
            tracker.resetToIdle()
        }
    }

    private suspend fun initBioSdk(fingerprintSdk: FingerprintConfiguration.BioSdk) {
        try {
            bioSdkWrapper = resolveBioSdkWrapperUseCase(fingerprintSdk)
            bioSdkWrapper.initialize()
            bioSdkConfiguration = configuration.getSdkConfiguration(fingerprintSdk)!!
        } catch (e: BioSdkException.BioSdkInitializationException) {
            Simber.e(e)
            _invalidLicense.send()
        }
    }

    private fun launchReconnect() {
        tracker.resetToIdle()
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
                is CaptureState.ScanProcess.Scanning,
                is CaptureState.ScanProcess.TransferringImage,
                -> pauseLiveFeedback()

                CaptureState.NotCollected,
                CaptureState.Skipped,
                is CaptureState.ScanProcess.NotDetected,
                is CaptureState.ScanProcess.Collected,
                -> {
                    if (it.isShowingConfirmDialog) stopLiveFeedback()
                    else startLiveFeedback(scannerManager.scanner)
                }
            }
        }
    }

    private fun shouldWeDoLiveFeedback(scanner: ScannerWrapper): Boolean =
        scanner.isLiveFeedbackAvailable() && bioSdkConfiguration.vero2?.ledsMode == LIVE_QUALITY_FEEDBACK


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
        // if live feedback is not supported, or if it is already paused, do nothing
        if (liveFeedbackState == null || liveFeedbackState == LiveFeedbackState.PAUSE) return

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

    /**
     * Every bio sdk has a different timeout for scanning and image transfer
     * This function returns the timeout for scanning plus the timeout for image transfer if it is required
     * */
    fun progressBarTimeout() =
        bioSdkWrapper.scanningTimeoutMs +
                if (isImageTransferRequired()) bioSdkWrapper.imageTransferTimeoutMs else 0

    private fun isImageTransferRequired(): Boolean =
        bioSdkConfiguration.vero2?.imageSavingStrategy?.isImageTransferRequired() ?: false &&
                scannerManager.scanner.isImageTransferSupported()

    fun updateSelectedFinger(index: Int) {
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
        if (fingerState is CaptureState.ScanProcess.Collected && fingerState.scanResult.isGoodScan() && state.isAskingRescan.not()) {
            updateState { it.copy(isAskingRescan = true) }
        } else {
            updateState { it.copy(isAskingRescan = false) }
            if (!isBusyForScanning()) {
                toggleScanning()
            }
        }
    }

    private fun isBusyForScanning(): Boolean = with(state) {
        currentCaptureState() is CaptureState.ScanProcess.TransferringImage || isShowingConfirmDialog || isShowingSplashScreen
    }

    private fun toggleScanning() {
        when (state.currentCaptureState()) {
            is CaptureState.ScanProcess.Scanning -> cancelScanning()
            is CaptureState.ScanProcess.TransferringImage -> { /* do nothing */
            }

            is CaptureState.NotCollected, is CaptureState.Skipped, is CaptureState.ScanProcess.NotDetected, is CaptureState.ScanProcess.Collected -> startScanning()
        }
    }

    private fun cancelScanning() {
        tracker.resetToIdle()
        updateCaptureState(CaptureState::toNotCollected)
        scanningTask?.cancel()
        imageTransferTask?.cancel()
    }

    private fun startScanning() {
        tracker.startScanning()
        updateCaptureState(CaptureState::toScanning)
        lastCaptureStartedAt = timeHelper.now()
        scanningTask?.cancel()

        scanningTask = viewModelScope.launch {
            try {
                val capturedFingerprint = bioSdkWrapper.acquireFingerprintTemplate(
                    capturingResolution = bioSdkConfiguration.vero2?.captureStrategy?.toInt(),
                    timeOutMs = bioSdkWrapper.scanningTimeoutMs.toInt(),
                    qualityThreshold = qualityThreshold(),
                    // is this is the last bad scan, we allow low quality extraction
                    allowLowQualityExtraction = isTooManyBadScans(state.currentCaptureState(), plusBadScan = true)
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
            qualityScore = acquireFingerprintTemplateResponse.imageQualityScore,
            template = acquireFingerprintTemplateResponse.template,
            templateFormat = acquireFingerprintTemplateResponse.templateFormat,
            image = null,
            qualityThreshold = qualityThreshold()
        )
        _vibrate.send()

        if (shouldProceedToImageTransfer(scanResult.qualityScore)) {
            updateCaptureState { it.toTransferringImage(scanResult) }
            proceedToImageTransfer()
        } else {
            tracker.setImageQualityCheckingResult(scanResult.qualityScore >= qualityThreshold())
            updateCaptureState { it.toCollected(scanResult) }
            handleCaptureFinished()
        }
    }

    private fun shouldProceedToImageTransfer(quality: Int): Boolean {
        val isGoodScan = quality >= qualityThreshold()
        val isTooManyBadScans = isTooManyBadScans(state.currentCaptureState(), plusBadScan = true)

        val shouldUploadImage = when (bioSdkConfiguration.vero2?.imageSavingStrategy) {
            NEVER, null -> false
            EAGER -> true
            ONLY_GOOD_SCAN -> isGoodScan
            ONLY_USED_IN_REFERENCE -> isGoodScan || isTooManyBadScans
        }
        return isImageTransferRequired() && shouldUploadImage
    }

    private fun proceedToImageTransfer() {
        imageTransferTask?.cancel()

        imageTransferTask = viewModelScope.launch {
            try {
                val acquiredImage = bioSdkWrapper.acquireFingerprintImage()
                if (isActive) {
                    handleImageTransferSuccess(acquiredImage)
                }
            } catch (_: CancellationException) {
                // No-op - This is expected when job is cancelled manually, i.e. by back press
            } catch (ex: Throwable) {
                handleScannerCommunicationsError(ex)
            }
        }
    }

    private fun handleImageTransferSuccess(acquireFingerprintImageResponse: AcquireFingerprintImageResponse) {
        _vibrate.send()
        updateCaptureState {
            it.toCollected(acquireFingerprintImageResponse.imageBytes).also { captureState ->
                tracker.setImageQualityCheckingResult(captureState.scanResult.qualityScore >= qualityThreshold())
            }
        }
        handleCaptureFinished()
    }

    private fun handleCaptureFinished() = with(state) {
        Simber.tag(FINGER_CAPTURE.name)
            .i("Finger scanned - ${currentFingerState().id}")
        tracker.resetToIdle()
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
                isTooManyBadScans(captureState, plusBadScan = false)
            )
        }
        captureEventIds[CaptureId(fingerState.id, fingerState.currentCaptureIndex)] = payloadId
    }

    private fun saveCurrentImageIfEager() {
        if (bioSdkConfiguration.vero2?.imageSavingStrategy?.isEager() == true) {
            with(state.currentFingerState()) {
                (currentCapture() as? CaptureState.ScanProcess.Collected)?.let { capture ->
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
            }
        }
    }

    private fun resolveFingerTerminalConditionTriggered() = with(state) {
        if (isScanningEndStateAchieved()) {
            Simber.tag(FINGER_CAPTURE.name).i("Confirm fingerprints dialog shown")
            updateState { it.copy(isShowingConfirmDialog = true) }
        } else if (currentCaptureState().let { it is CaptureState.ScanProcess.Collected && it.scanResult.isGoodScan() }) {
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

            is NoFingerDetectedException -> {
                Simber.i(e)
                handleNoFingerDetected()
            }

            else -> {
                updateCaptureState { toNotCollected() }
                Simber.e(e)
                _launchAlert.send()
            }
        }
    }

    private fun handleNoFingerDetected() {
        _vibrate.send()
        tracker.setImageQualityCheckingResult(false)
        updateCaptureState(CaptureState::toNotDetected)
        addCaptureAndBiometricEventsInSession()
        tracker.resetToIdle()
        if (isNoFingerDetectedLimitReachedUseCase(state.currentCaptureState(), bioSdkConfiguration)) {
            handleCaptureFinished()
        }
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

    private fun isTooManyBadScans(fingerState: CaptureState, plusBadScan: Boolean): Boolean {
        val isNumberOfBadScansReached = isNumberOfBadScansReached(fingerState, plusBadScan)
        val isNumberOfNoFingerDetectedReached =
            isNoFingerDetectedLimitReachedUseCase(fingerState, bioSdkConfiguration)
        return isNumberOfBadScansReached || isNumberOfNoFingerDetectedReached
    }

    private fun isNumberOfBadScansReached(fingerState: CaptureState, plusBadScan: Boolean) =
        when (fingerState) {
            is CaptureState.ScanProcess -> fingerState.numberOfBadScans
            else -> 0
        } >= numberOfBadScansRequiredToAutoAddNewFinger - if (plusBadScan) 1 else 0

    private fun CollectFingerprintsState.haveNotExceedMaximumNumberOfFingersToAutoAdd() =
        fingerStates.size < maximumTotalNumberOfFingersForAutoAdding

    private fun CollectFingerprintsState.weHaveTheMinimumNumberOfGoodScans(): Boolean =
        fingerStates.filter {
            val currentCapture = it.currentCapture()
            currentCapture is CaptureState.ScanProcess.Collected && currentCapture.scanResult.isGoodScan()
        }.size >= min(targetNumberOfGoodScans, numberOfOriginalFingers())

    private fun CollectFingerprintsState.weHaveTheMinimumNumberOfAnyQualityScans() =
        fingerStates.filter {
            captureHasSatisfiedTerminalCondition(it.currentCapture())
        }.size >= maximumTotalNumberOfFingersForAutoAdding

    private fun numberOfOriginalFingers() = originalFingerprintsToCapture.toSet().size

    private fun captureHasSatisfiedTerminalCondition(captureState: CaptureState): Boolean {
        val isCollected = captureState is CaptureState.ScanProcess.Collected && (isTooManyBadScans(
            captureState, plusBadScan = false
        ) || captureState.scanResult.isGoodScan())
        val isSkipped = captureState is CaptureState.Skipped
        val isNotDetected = isNoFingerDetectedLimitReachedUseCase(captureState, bioSdkConfiguration)
        return isCollected || isSkipped || isNotDetected
    }

    private fun fingerHasSatisfiedTerminalCondition(fingerState: FingerState) =
        fingerState.captures.all { captureHasSatisfiedTerminalCondition(it) }

    fun handleConfirmFingerprintsAndContinue() {
        val collectedFingers = state.fingerStates.flatMap {
            it.captures.mapIndexedNotNull { index, capture ->
                if (capture is CaptureState.ScanProcess.Collected) Pair(
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

    private fun saveImages(collectedFingers: List<Pair<CaptureId, CaptureState.ScanProcess.Collected>>) {
        runBlocking {
            collectedFingers.map { (id, collectedFinger) ->
                saveImageIfExists(id, collectedFinger)
            }
        }
    }

    private fun proceedToFinish(collectedFingers: List<Pair<CaptureId, CaptureState.ScanProcess.Collected>>) {
        val resultItems = collectedFingers.map { (captureId, collectedFinger) ->
            FingerprintCaptureResult.Item(
                identifier = captureId.finger,
                captureEventId = captureEventIds[captureId],
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

    private suspend fun saveImageIfExists(
        id: CaptureId,
        collectedFinger: CaptureState.ScanProcess.Collected
    ) {
        val captureEventId = captureEventIds[id]
        val imageRef = saveImage(
            vero2Configuration = bioSdkConfiguration.vero2!!,
            finger = id.finger,
            captureEventId = captureEventId,
            collectedFinger = collectedFinger,
        )
        imageRefs[id] = imageRef
    }

    fun handleRestart() {
        setStartingState(originalFingerprintsToCapture)
        tracker.resetToIdle()
    }

    fun handleOnViewCreated(
        fingerprintsToCapture: List<IFingerIdentifier>,
        fingerprintSdk: FingerprintConfiguration.BioSdk,
    ) {
        updateState {
            it.copy(
                isShowingConnectionScreen = false
            )
        }
        start(fingerprintsToCapture, fingerprintSdk)
    }

    fun handleOnResume() {
        updateState {
            /* refresh */
            it.copy(
                isShowingSplashScreen = false,
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

    companion object {

        const val targetNumberOfGoodScans = 2
        const val maximumTotalNumberOfFingersForAutoAdding = 4
        const val numberOfBadScansRequiredToAutoAddNewFinger = 3
        const val AUTO_SWIPE_DELAY: Long = 500

        const val TRY_DIFFERENT_FINGER_SPLASH_DELAY: Long = 2000
    }
}
