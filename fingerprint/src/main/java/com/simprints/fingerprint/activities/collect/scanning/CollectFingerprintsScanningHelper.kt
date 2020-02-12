package com.simprints.fingerprint.activities.collect.scanning

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.collect.CollectFingerprintsContract
import com.simprints.fingerprint.activities.collect.CollectFingerprintsPresenter
import com.simprints.fingerprint.activities.collect.CollectFingerprintsPresenter.Companion.imageTransferTimeoutMs
import com.simprints.fingerprint.activities.collect.CollectFingerprintsPresenter.Companion.qualityThreshold
import com.simprints.fingerprint.activities.collect.CollectFingerprintsPresenter.Companion.scanningTimeoutMs
import com.simprints.fingerprint.activities.collect.models.FingerStatus
import com.simprints.fingerprint.activities.collect.models.FingerStatus.*
import com.simprints.fingerprint.activities.collect.timeoutbar.ScanningOnlyTimeoutBar
import com.simprints.fingerprint.activities.collect.timeoutbar.ScanningTimeoutBar
import com.simprints.fingerprint.activities.collect.timeoutbar.ScanningWithImageTransferTimeoutBar
import com.simprints.fingerprint.controllers.core.androidResources.FingerprintAndroidResourcesHelper
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportManager
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportTag.FINGER_CAPTURE
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportTrigger.SCANNER_BUTTON
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportTrigger.UI
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.data.domain.fingerprint.Fingerprint
import com.simprints.fingerprint.data.domain.images.SaveFingerprintImagesStrategy
import com.simprints.fingerprint.exceptions.unexpected.FingerprintUnexpectedException
import com.simprints.fingerprint.scanner.ScannerManager
import com.simprints.fingerprint.scanner.domain.AcquireImageResponse
import com.simprints.fingerprint.scanner.domain.CaptureFingerprintResponse
import com.simprints.fingerprint.scanner.domain.ScannerTriggerListener
import com.simprints.fingerprint.scanner.exceptions.safe.NoFingerDetectedException
import com.simprints.fingerprint.scanner.exceptions.safe.ScannerDisconnectedException
import com.simprints.fingerprint.scanner.exceptions.safe.ScannerOperationInterruptedException
import com.simprints.fingerprint.tools.Vibrate
import com.simprints.fingerprint.tools.extensions.runOnUiThreadIfStillRunning
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.runOnUiThread
import timber.log.Timber

class CollectFingerprintsScanningHelper(private val context: Context,
                                        private val view: CollectFingerprintsContract.View,
                                        private val presenter: CollectFingerprintsContract.Presenter,
                                        private val scannerManager: ScannerManager,
                                        private val crashReportManager: FingerprintCrashReportManager,
                                        private val androidResourcesHelper: FingerprintAndroidResourcesHelper,
                                        private val fingerprintPreferencesManager: FingerprintPreferencesManager) {


    private var previousStatus: FingerStatus = NOT_COLLECTED
    private var currentFingerStatus: FingerStatus
        get() = presenter.currentFinger().status
        set(value) {
            presenter.currentFinger().status = value
        }

    private val scannerTriggerListener = ScannerTriggerListener {
        crashReportManager.logMessageForCrashReport(FINGER_CAPTURE, SCANNER_BUTTON, message = "Scanner button clicked")
        if (presenter.isConfirmDialogShown)
            context.runOnUiThread {
                presenter.handleConfirmFingerprintsAndContinue()
            }
        else if (shouldEnableScanButton())
            context.runOnUiThread {
                presenter.handleScannerButtonPressed()
            }
    }

    private var scanningTask: Disposable? = null
    private var reconnectingTask: Disposable? = null
    private var imageTransferTask: Disposable? = null

    private fun shouldEnableScanButton() = !presenter.isBusyWithFingerTransitionAnimation

    init {
        view.timeoutBar = initTimeoutBar()
        view.un20WakeupDialog = initUn20Dialog()
    }

    fun startListeners() {
        scannerManager.onScanner { registerTriggerListener(scannerTriggerListener) }
    }

    fun stopListeners() {
        scannerManager.onScanner { unregisterTriggerListener(scannerTriggerListener) }
    }

    private fun isDoingImageTransfer(): Boolean =
        when (fingerprintPreferencesManager.saveFingerprintImagesStrategy) {
            SaveFingerprintImagesStrategy.NEVER -> false
            SaveFingerprintImagesStrategy.WSQ_15 -> true
        }

    private fun initTimeoutBar(): ScanningTimeoutBar =
        if (isDoingImageTransfer()) {
            ScanningWithImageTransferTimeoutBar(context, view.progressBar, scanningTimeoutMs, imageTransferTimeoutMs)
        } else {
            ScanningOnlyTimeoutBar(context, view.progressBar, scanningTimeoutMs)
        }

    // Creates a progress dialog when the scan gets disconnected
    private fun initUn20Dialog(): ProgressDialog =
        ProgressDialog(context).also { dialog ->
            dialog.isIndeterminate = true
            dialog.setCanceledOnTouchOutside(false)
            dialog.setMessage(androidResourcesHelper.getString(R.string.reconnecting_message))
            dialog.setOnCancelListener { presenter.handleOnBackPressed() }
        }

    fun reconnect() {
        scannerManager.onScanner { unregisterTriggerListener(scannerTriggerListener) }
        (view as Activity).runOnUiThreadIfStillRunning {
            view.un20WakeupDialog.show()
        }

        reconnectingTask = scannerManager.scanner { disconnect() }
            .andThen(scannerManager.checkBluetoothStatus())
            .andThen(scannerManager.initScanner())
            .andThen(scannerManager.scanner { connect() })
            .andThen(scannerManager.scanner { setUiIdle() })
            .andThen(scannerManager.scanner { sensorShutDown() })
            .andThen(scannerManager.scanner { sensorWakeUp() })
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribeBy(onComplete = {
                Timber.d("reconnect.onSuccess()")
                view.un20WakeupDialog.dismiss()
                scannerManager.onScanner { registerTriggerListener(scannerTriggerListener) }
            }, onError = {
                it.printStackTrace()
                Timber.d("reconnect.onError()")
                view.un20WakeupDialog.dismiss()
                view.doLaunchAlert(scannerManager.getAlertType(it))
                crashReportManager.logExceptionOrSafeException(it)
            })
    }

    private fun handleError(e: Throwable) {
        when (e) {
            is ScannerOperationInterruptedException -> {
                cancelCaptureUI()
            }
            is ScannerDisconnectedException -> {
                cancelCaptureUI()
                reconnect()
            }
            is NoFingerDetectedException -> {
                handleNoFingerTemplateDetected()
            }
            else -> {
                cancelCaptureUI()
                presenter.handleException(e)
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun resetUIFromError() {
        currentFingerStatus = NOT_COLLECTED
        presenter.currentFinger().template = null
        scannerManager.scanner { setUiIdle() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = {
                    presenter.refreshDisplay()
                    view.scanButton.isEnabled = true
                    view.un20WakeupDialog.dismiss()
                },
                onError = {
                    reconnect()
                }
            )
    }

    // it start/stop the scan based on the activeFingers[currentActiveFingerNo] state
    fun toggleContinuousCapture() =
        when (currentFingerStatus) {
            GOOD_SCAN ->
                askIfWantRescan()
            RESCAN_GOOD_SCAN, BAD_SCAN, NOT_COLLECTED, NO_FINGER_DETECTED, FINGER_SKIPPED ->
                startContinuousCapture()
            COLLECTING ->
                stopContinuousCapture()
            TRANSFERRING_IMAGE -> {
                /* Do nothing as cannot cancel image transfer */
            }
        }

    private fun askIfWantRescan() {
        currentFingerStatus = RESCAN_GOOD_SCAN
        presenter.refreshDisplay()
    }

    private fun startContinuousCapture() {
        scannerManager.scanner { setUiIdle() }.doInBackground()
        previousStatus = currentFingerStatus
        currentFingerStatus = COLLECTING
        presenter.refreshDisplay()
        view.scanButton.isEnabled = true
        presenter.refreshDisplay()
        view.timeoutBar.startTimeoutBar()
        scanningTask?.dispose()
        scanningTask = scannerManager.scanner<CaptureFingerprintResponse> {
            captureFingerprint(
                fingerprintPreferencesManager.captureFingerprintStrategy,
                scanningTimeoutMs.toInt(),
                qualityThreshold
            )
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = ::handleCaptureSuccess,
                onError = ::handleError
            )
    }

    private fun stopContinuousCapture() {
        scanningTask?.dispose()
        cancelCaptureUI()
    }

    private fun handleNoFingerTemplateDetected() {
        currentFingerStatus = NO_FINGER_DETECTED
        view.timeoutBar.handleAllStepsFinished()
        Vibrate.vibrate(context)
        presenter.refreshDisplay()
    }

    private fun cancelCaptureUI() {
        currentFingerStatus = previousStatus
        view.timeoutBar.handleCancelled()
        presenter.refreshDisplay()
    }

    private fun handleCaptureSuccess(captureFingerprintResponse: CaptureFingerprintResponse) {
        val quality = captureFingerprintResponse.imageQualityScore
        parseTemplateAndAddToCurrentFinger(captureFingerprintResponse.template)
        presenter.currentFinger().templateQuality = quality
        Vibrate.vibrate(context)
        setGoodOrBadScanFingerStatusToCurrentFinger(quality)
        if (shouldProceedToImageTransfer(quality)) {
            view.timeoutBar.handleScanningFinished()
            proceedToImageTransfer()
        } else {
            view.timeoutBar.handleAllStepsFinished()
            presenter.refreshDisplay()
            presenter.handleCaptureSuccess()
        }
    }

    private fun shouldProceedToImageTransfer(quality: Int) =
        isDoingImageTransfer() &&
            (quality >= qualityThreshold || presenter.tooManyBadScans(presenter.currentFinger()))

    private fun proceedToImageTransfer() {
        currentFingerStatus = TRANSFERRING_IMAGE
        presenter.refreshDisplay()
        imageTransferTask = scannerManager.onScanner { acquireImage(fingerprintPreferencesManager.saveFingerprintImagesStrategy) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = ::handleImageTransferSuccess,
                onError = ::handleError
            )
    }

    private fun handleImageTransferSuccess(acquireImageResponse: AcquireImageResponse) {
        view.timeoutBar.handleAllStepsFinished()
        val imageBytes = acquireImageResponse.imageBytes
        presenter.currentFinger().imageBytes = imageBytes
        setGoodOrBadScanFingerStatusToCurrentFinger(presenter.currentFinger().templateQuality
            ?: throw IllegalStateException("Must have set template quality before here"))
        Vibrate.vibrate(context)
        presenter.refreshDisplay()
        presenter.handleCaptureSuccess()
    }

    private fun parseTemplateAndAddToCurrentFinger(template: ByteArray) =
        try {
            presenter.currentFinger().template =
                Fingerprint(presenter.currentFinger().id, template)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            crashReportManager.logExceptionOrSafeException(FingerprintUnexpectedException("IllegalArgumentException in CollectFingerprintsActivity.handleCaptureSuccess()", e))
            resetUIFromError()
        }

    private fun setGoodOrBadScanFingerStatusToCurrentFinger(quality: Int) {
        if (quality >= qualityThreshold) {
            currentFingerStatus = GOOD_SCAN
        } else {
            currentFingerStatus = BAD_SCAN
            presenter.currentFinger().numberOfBadScans += 1
        }
        logMessageForCrashReport("Finger scanned - ${presenter.currentFinger().id} - $currentFingerStatus")
    }

    fun resetScannerUi() {
        scannerManager.scanner { setUiIdle() }.doInBackground()
    }

    fun stopScannerCommunications() {
        reconnectingTask?.dispose()
        imageTransferTask?.dispose()
        disconnectScannerIfNeeded()
    }

    fun disconnectScannerIfNeeded() {
        scannerManager.scanner { disconnect() }.doInBackground()
    }

    fun setCurrentFingerAsSkippedAndAsNumberOfBadScansToAutoAddFinger() {
        currentFingerStatus = FINGER_SKIPPED
        presenter.currentFinger().numberOfBadScans = CollectFingerprintsPresenter.numberOfBadScansRequiredToAutoAddNewFinger
        presenter.refreshDisplay()
    }

    private fun logMessageForCrashReport(message: String) {
        crashReportManager.logMessageForCrashReport(FINGER_CAPTURE, UI, message = message)
    }

    private fun Completable.doInBackground() =
        subscribeOn(Schedulers.io())
            .subscribeBy(onComplete = {}, onError = {})
}
