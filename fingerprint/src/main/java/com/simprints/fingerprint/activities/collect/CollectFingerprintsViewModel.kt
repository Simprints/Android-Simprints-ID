package com.simprints.fingerprint.activities.collect

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.fingerprint.activities.collect.domain.Finger
import com.simprints.fingerprint.activities.collect.old.CollectFingerprintsPresenter
import com.simprints.fingerprint.activities.collect.old.models.FingerScanConfig
import com.simprints.fingerprint.activities.collect.result.CollectFingerprintsTaskResult
import com.simprints.fingerprint.activities.collect.state.CollectFingerprintsState
import com.simprints.fingerprint.activities.collect.state.FingerCollectionState
import com.simprints.fingerprint.activities.collect.state.FingerScanResult
import com.simprints.fingerprint.controllers.core.image.FingerprintImageManager
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.data.domain.fingerprint.FingerIdentifier
import com.simprints.fingerprint.data.domain.fingerprint.Fingerprint
import com.simprints.fingerprint.data.domain.images.SaveFingerprintImagesStrategy
import com.simprints.fingerprint.data.domain.images.deduceFileExtension
import com.simprints.fingerprint.exceptions.unexpected.FingerprintUnexpectedException
import com.simprints.fingerprint.scanner.ScannerManager
import com.simprints.fingerprint.scanner.domain.AcquireImageResponse
import com.simprints.fingerprint.scanner.domain.CaptureFingerprintResponse
import com.simprints.fingerprint.scanner.exceptions.safe.NoFingerDetectedException
import com.simprints.fingerprint.scanner.exceptions.safe.ScannerDisconnectedException
import com.simprints.fingerprint.scanner.exceptions.safe.ScannerOperationInterruptedException
import com.simprints.fingerprint.tools.livedata.postEvent
import com.simprints.fingerprint.tools.mapNotNullValues
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.runBlocking
import kotlin.math.min

class CollectFingerprintsViewModel(
    private val scannerManager: ScannerManager,
    private val fingerprintPreferencesManager: FingerprintPreferencesManager,
    private val imageManager: FingerprintImageManager
) : ViewModel() {

    val state = MutableLiveData<CollectFingerprintsState>()
    fun state() = state.value ?: TODO("Oops")

    private fun updateState(block: CollectFingerprintsState.() -> Unit) {
        state.postValue(state.value?.apply { block() })
    }

    private fun updateFingerState(block: FingerCollectionState.() -> FingerCollectionState) {
        updateState { fingerStates[currentFinger()] = currentFingerState().run { block() } }
    }

    val vibrate = MutableLiveData<LiveDataEvent>()
    val moveToNextFinger = MutableLiveData<LiveDataEvent>()
    val noFingersScannedToast = MutableLiveData<LiveDataEvent>()

    private lateinit var fingerprintsToCapture: List<FingerIdentifier>
    private var scanningTask: Disposable? = null
    private var imageTransferTask: Disposable? = null

    fun start(fingerprintsToCapture: List<FingerIdentifier>) {
        this.fingerprintsToCapture = fingerprintsToCapture
        setStartingState()
    }

    private fun setStartingState() {
        state.value = CollectFingerprintsState(fingerprintsToCapture.map {
            Finger(it, FingerScanConfig.DEFAULT.getPriority(it), FingerScanConfig.DEFAULT.getOrder(it))
        }.associateWith { FingerCollectionState.NotCollected }.toMutableMap())
    }

    fun isImageTransferRequired(): Boolean =
        when (fingerprintPreferencesManager.saveFingerprintImagesStrategy) {
            SaveFingerprintImagesStrategy.NEVER -> false
            SaveFingerprintImagesStrategy.WSQ_15 -> true
        }

    fun updateSelectedFingerIfNotBusy(index: Int) {
        updateState { currentFingerIndex = index }
    }

    fun handleScanButtonPressed() {
        toggleScanning()
    }

    private fun toggleScanning() {
        when (state().currentFingerState()) {
            is FingerCollectionState.Scanning -> cancelScanning()
            is FingerCollectionState.TransferringImage -> { /* do nothing */
            }
            FingerCollectionState.NotCollected,
            FingerCollectionState.Skipped,
            is FingerCollectionState.NotDetected,
            is FingerCollectionState.Collected -> startScanning()
        }
    }

    private fun cancelScanning() {
        updateFingerState { toNotCollected() }
        scanningTask?.dispose()
        imageTransferTask?.dispose()
    }

    private fun startScanning() {
        updateFingerState { toScanning() }
        scanningTask?.dispose()
        scanningTask = scannerManager.scanner { setUiIdle() }
            .andThen(scannerManager.scanner<CaptureFingerprintResponse> {
                captureFingerprint(
                    fingerprintPreferencesManager.captureFingerprintStrategy,
                    CollectFingerprintsPresenter.scanningTimeoutMs.toInt(),
                    CollectFingerprintsPresenter.qualityThreshold
                )
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = ::handleCaptureSuccess,
                onError = ::handleError
            )
    }

    private fun handleCaptureSuccess(captureFingerprintResponse: CaptureFingerprintResponse) {
        val scanResult = FingerScanResult(captureFingerprintResponse.imageQualityScore, captureFingerprintResponse.template, null)
        vibrate.postEvent()
        if (shouldProceedToImageTransfer(scanResult.qualityScore)) {
            updateFingerState { toTransferringImage(scanResult) }
            proceedToImageTransfer()
        } else {
            updateFingerState { toCollected(scanResult) }
            handleCaptureFinished()
        }
    }

    private fun shouldProceedToImageTransfer(quality: Int) =
        isImageTransferRequired() &&
            (quality >= CollectFingerprintsPresenter.qualityThreshold || tooManyBadScans(state().currentFingerState()))

    private fun proceedToImageTransfer() {
        imageTransferTask?.dispose()
        imageTransferTask = scannerManager.onScanner { acquireImage(fingerprintPreferencesManager.saveFingerprintImagesStrategy) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = ::handleImageTransferSuccess,
                onError = ::handleError
            )
    }

    private fun handleImageTransferSuccess(acquireImageResponse: AcquireImageResponse) {
        vibrate.postEvent()
        updateFingerState { toCollected(acquireImageResponse.imageBytes) }
        handleCaptureFinished()
    }

    private fun handleCaptureFinished() {
//        addCaptureEventInSession(currentFinger())
        if (fingerHasSatisfiedTerminalCondition(state().currentFingerState())) {
            resolveFingerTerminalConditionTriggered()
        }
    }

    fun resolveFingerTerminalConditionTriggered() {
        with(state()) {
            if (isScanningEndStateAchieved()) {
//            createMapAndShowDialog()
            } else if (currentFingerState().let {
                    it is FingerCollectionState.Collected && it.fingerScanResult.isGoodScan()
                }) {
//            fingerDisplayHelper.doNudge()
            } else {
                if (haveNotExceedMaximumNumberOfFingersToAutoAdd()) {
//                fingerDisplayHelper.showSplashAndNudgeAndAddNewFinger()
                } else if (isOnLastFinger()) {
//                fingerDisplayHelper.showSplashAndNudgeIfNecessary()
                }
            }
        }
    }

    private fun handleError(e: Throwable) {
        when (e) {
            is ScannerOperationInterruptedException -> {
                updateFingerState { toNotCollected() }
            }
            is ScannerDisconnectedException -> {
                updateFingerState { toNotCollected() }
                // TODO : reconnect
            }
            is NoFingerDetectedException -> handleNoFingerDetected()
            else -> {
                updateFingerState { toNotCollected() }
                // TODO : handle exception
            }
        }
    }

    private fun handleNoFingerDetected() {
        vibrate.postEvent()
        updateFingerState { toNotDetected() }
    }

    fun handleMissingFingerButtonPressed() {
        updateFingerState { toSkipped() }
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
        fingerStates.values.all { fingerHasSatisfiedTerminalCondition(it) }

    private fun tooManyBadScans(fingerState: FingerCollectionState): Boolean =
        when (fingerState) {
            is FingerCollectionState.Scanning -> fingerState.numberOfBadScans
            is FingerCollectionState.TransferringImage -> fingerState.numberOfBadScans
            is FingerCollectionState.NotDetected -> fingerState.numberOfBadScans
            is FingerCollectionState.Collected -> fingerState.numberOfBadScans
            else -> 0
        } >= numberOfBadScansRequiredToAutoAddNewFinger

    private fun CollectFingerprintsState.haveNotExceedMaximumNumberOfFingersToAutoAdd() =
        fingerStates.size < maximumTotalNumberOfFingersForAutoAdding

    private fun CollectFingerprintsState.weHaveTheMinimumNumberOfGoodScans(): Boolean =
        fingerStates.values.filter {
            it is FingerCollectionState.Collected && it.fingerScanResult.isGoodScan()
        }.size >= min(targetNumberOfGoodScans, numberOfOriginalFingers())

    private fun CollectFingerprintsState.weHaveTheMinimumNumberOfAnyQualityScans() =
        fingerStates.values.filter {
            fingerHasSatisfiedTerminalCondition(it)
        }.size >= maximumTotalNumberOfFingersForAutoAdding

    private fun numberOfOriginalFingers() =
        fingerprintsToCapture.toSet().size

    private fun fingerHasSatisfiedTerminalCondition(fingerState: FingerCollectionState) =
        (fingerState is FingerCollectionState.Collected && tooManyBadScans(fingerState))
            || fingerState is FingerCollectionState.Skipped

    fun handleConfirmFingerprintsAndContinue() {
//        logMessageForCrashReport("Confirm fingerprints clicked")
//        dismissConfirmDialogIfStillShowing()

        val fingersStates = state().fingerStates
            .mapNotNullValues { it as? FingerCollectionState.Collected }

        if (fingersStates.isEmpty()) {
            noFingersScannedToast.postEvent()
            handleRestart()
        } else {
            saveImagesAndProceedToFinish(fingersStates)
        }
    }

    private fun saveImagesAndProceedToFinish(fingerprints: Map<Finger, FingerCollectionState.Collected>) {
        runBlocking {
            fingerprints.forEach { (finger, state) ->
                saveImage(finger, state)
            }
            proceedToFinish(fingerprints)
        }
    }

    private suspend fun saveImage(finger: Finger, fingerState: FingerCollectionState) {
        val imageBytes = finger.imageBytes
        val captureEventId = captureEventIds[finger.id]

        if (imageBytes != null && captureEventId != null) {
            finger.template?.imageRef = imageManager.save(imageBytes, captureEventId,
                fingerprintPreferencesManager.saveFingerprintImagesStrategy.deduceFileExtension())
        } else if (imageBytes != null && captureEventId == null) {
            crashReportManager.logExceptionOrSafeException(FingerprintUnexpectedException("Could not save fingerprint image because of null capture ID"))
        }
    }

    private fun proceedToFinish(fingerprints: Map<Finger, FingerCollectionState.Collected>) {
        view.setResultAndFinishSuccess(CollectFingerprintsTaskResult(fingerprints))
    }

    fun handleRestart() {

    }

    companion object {
        const val targetNumberOfGoodScans = 2
        const val maximumTotalNumberOfFingersForAutoAdding = 4
        const val numberOfBadScansRequiredToAutoAddNewFinger = 3
        const val scanningTimeoutMs = 3000L
        const val imageTransferTimeoutMs = 3000L
    }
}
