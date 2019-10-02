package com.simprints.fingerprint.activities.collect.scanning

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.collect.CollectFingerprintsContract
import com.simprints.fingerprint.activities.collect.CollectFingerprintsPresenter
import com.simprints.fingerprint.activities.collect.CollectFingerprintsPresenter.Companion.qualityThreshold
import com.simprints.fingerprint.activities.collect.CollectFingerprintsPresenter.Companion.timeoutInMillis
import com.simprints.fingerprint.activities.collect.models.FingerStatus
import com.simprints.fingerprint.activities.collect.models.FingerStatus.*
import com.simprints.fingerprint.activities.collect.views.TimeoutBar
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportManager
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportTag.FINGER_CAPTURE
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportTrigger.SCANNER_BUTTON
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportTrigger.UI
import com.simprints.fingerprint.data.domain.person.Fingerprint
import com.simprints.fingerprint.exceptions.unexpected.FingerprintUnexpectedException
import com.simprints.fingerprint.scanner.ScannerManager
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
import timber.log.Timber

class CollectFingerprintsScanningHelper(private val context: Context,
                                        private val view: CollectFingerprintsContract.View,
                                        private val presenter: CollectFingerprintsContract.Presenter,
                                        private val scannerManager: ScannerManager,
                                        private val crashReportManager: FingerprintCrashReportManager) {

    private var previousStatus: FingerStatus = NOT_COLLECTED
    private var currentFingerStatus: FingerStatus
        get() = presenter.currentFinger().status
        set(value) {
            presenter.currentFinger().status = value
        }

    private val scannerTriggerListener = ScannerTriggerListener {
        crashReportManager.logMessageForCrashReport(FINGER_CAPTURE, SCANNER_BUTTON, message = "Scanner button clicked")
        if (presenter.isConfirmDialogShown)
            presenter.handleConfirmFingerprintsAndContinue()
        else if (shouldEnableScanButton())
            presenter.handleScannerButtonPressed()
    }

    private var scanningTask: Disposable? = null

    private fun shouldEnableScanButton() = !presenter.isBusyWithFingerTransitionAnimation

    init {
        view.timeoutBar = initTimeoutBar()
        view.un20WakeupDialog = initUn20Dialog()
    }

    fun startListeners() {
        scannerManager.scanner.registerTriggerListener(scannerTriggerListener)
    }

    fun stopListeners() {
        scannerManager.scanner.unregisterTriggerListener(scannerTriggerListener)
    }

    private fun initTimeoutBar(): TimeoutBar =
        TimeoutBar(context.applicationContext, view.progressBar, timeoutInMillis)

    // Creates a progress dialog when the scan gets disconnected
    private fun initUn20Dialog(): ProgressDialog =
        ProgressDialog(context).also { dialog ->
            dialog.isIndeterminate = true
            dialog.setCanceledOnTouchOutside(false)
            dialog.setMessage(context.getString(R.string.reconnecting_message))
            dialog.setOnCancelListener { view.cancelAndFinish() }
        }

    @SuppressLint("CheckResult")
    fun reconnect() {
        scannerManager.scanner.unregisterTriggerListener(scannerTriggerListener)
        (view as Activity).runOnUiThreadIfStillRunning {
            view.un20WakeupDialog.show()
        }

        scannerManager.scanner.disconnect()
            .andThen(scannerManager.checkBluetoothStatus())
            .andThen(scannerManager.initScanner())
            .andThen(scannerManager.scanner.connect())
            .andThen(scannerManager.scanner.setUiIdle())
            .andThen(scannerManager.scanner.sensorShutDown())
            .andThen(scannerManager.scanner.sensorWakeUp())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribeBy(onComplete = {
                Timber.d("reconnect.onSuccess()")
                view.un20WakeupDialog.dismiss()
                scannerManager.scanner.registerTriggerListener(scannerTriggerListener)
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
        scannerManager.scanner.setUiIdle()
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
        }

    private fun askIfWantRescan() {
        currentFingerStatus = RESCAN_GOOD_SCAN
        presenter.refreshDisplay()
    }

    private fun startContinuousCapture() {
        previousStatus = currentFingerStatus
        currentFingerStatus = COLLECTING
        presenter.refreshDisplay()
        view.scanButton.isEnabled = true
        presenter.refreshDisplay()
        view.timeoutBar.startTimeoutBar()
        scanningTask?.dispose()
        scanningTask = scannerManager.scanner.captureFingerprint(timeoutInMillis, qualityThreshold)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = ::handleCaptureSuccess,
                onError = ::handleError
            )
    }

    private fun stopContinuousCapture() {
        scanningTask?.dispose()
    }

    private fun handleNoFingerTemplateDetected() {
        currentFingerStatus = NO_FINGER_DETECTED
        Vibrate.vibrate(context)
        presenter.refreshDisplay()
    }

    private fun cancelCaptureUI() {
        currentFingerStatus = previousStatus
        view.timeoutBar.cancelTimeoutBar()
        presenter.refreshDisplay()
    }

    private fun handleCaptureSuccess(captureFingerprintResponse: CaptureFingerprintResponse) {
        view.timeoutBar.stopTimeoutBar()
        val quality = captureFingerprintResponse.imageQualityScore
        parseTemplateAndAddToCurrentFinger(captureFingerprintResponse.template)
        setGoodOrBadScanFingerStatusToCurrentFinger(quality)
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
        scannerManager.scanner.setUiIdle().doInBackground()
    }

    fun stopReconnecting() {
        scannerManager.scanner.disconnect().doInBackground()
    }

    fun disconnectScannerIfNeeded() {
        scannerManager.scanner.disconnect().doInBackground()
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
