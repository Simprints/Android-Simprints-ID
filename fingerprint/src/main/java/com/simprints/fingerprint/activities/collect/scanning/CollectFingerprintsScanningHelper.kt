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
import com.simprints.fingerprint.controllers.core.androidResources.FingerprintAndroidResourcesHelper
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportManager
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportTag.FINGER_CAPTURE
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportTrigger.SCANNER_BUTTON
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportTrigger.UI
import com.simprints.fingerprint.controllers.scanner.ScannerManager
import com.simprints.fingerprint.data.domain.person.Fingerprint
import com.simprints.fingerprint.exceptions.unexpected.FingerprintUnexpectedException
import com.simprints.fingerprint.exceptions.unexpected.scanner.UnexpectedScannerException
import com.simprints.fingerprint.tools.Vibrate
import com.simprints.fingerprint.tools.extensions.runOnUiThreadIfStillRunning
import com.simprints.fingerprintscanner.ButtonListener
import com.simprints.fingerprintscanner.SCANNER_ERROR
import com.simprints.fingerprintscanner.SCANNER_ERROR.*
import com.simprints.fingerprintscanner.ScannerCallback
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class CollectFingerprintsScanningHelper(private val context: Context,
                                        private val view: CollectFingerprintsContract.View,
                                        private val presenter: CollectFingerprintsContract.Presenter,
                                        private val scannerManager: ScannerManager,
                                        private val crashReportManager: FingerprintCrashReportManager,
                                        private val androidResourcesHelper: FingerprintAndroidResourcesHelper) {


    private var previousStatus: FingerStatus = NOT_COLLECTED
    private var currentFingerStatus: FingerStatus
        get() = presenter.currentFinger().status
        set(value) {
            presenter.currentFinger().status = value
        }

    private val scannerButtonListener = ButtonListener {
        crashReportManager.logMessageForCrashReport(FINGER_CAPTURE, SCANNER_BUTTON, message = "Scanner button clicked")
        if (presenter.isConfirmDialogShown)
            presenter.handleConfirmFingerprintsAndContinue()
        else if (shouldEnableScanButton())
            presenter.handleScannerButtonPressed()
    }

    private fun shouldEnableScanButton() = !presenter.isBusyWithFingerTransitionAnimation

    init {
        view.timeoutBar = initTimeoutBar()
        view.un20WakeupDialog = initUn20Dialog()
    }

    fun startListeners() {
        scannerManager.scanner?.registerButtonListener(scannerButtonListener)
    }

    fun stopListeners() {
        scannerManager.scanner?.unregisterButtonListener(scannerButtonListener)
    }

    private fun initTimeoutBar(): TimeoutBar =
        TimeoutBar(context.applicationContext, view.progressBar, timeoutInMillis)

    // Creates a progress dialog when the scan gets disconnected
    private fun initUn20Dialog(): ProgressDialog =
        ProgressDialog(context).also { dialog ->
            dialog.isIndeterminate = true
            dialog.setCanceledOnTouchOutside(false)
            dialog.setMessage(androidResourcesHelper.getString(R.string.reconnecting_message))
            dialog.setOnCancelListener { view.cancelAndFinish() }
        }

    @SuppressLint("CheckResult")
    fun reconnect() {
        scannerManager.scanner?.unregisterButtonListener(scannerButtonListener)
        (view as Activity).runOnUiThreadIfStillRunning {
            view.un20WakeupDialog.show()
        }

        scannerManager.start()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribeBy(onComplete = {
                Timber.d("reconnect.onSuccess()")
                view.un20WakeupDialog.dismiss()
                scannerManager.scanner?.registerButtonListener(scannerButtonListener)
            }, onError = {
                it.printStackTrace()
                Timber.d("reconnect.onError()")
                view.un20WakeupDialog.dismiss()
                view.doLaunchAlert(scannerManager.getAlertType(it))
                crashReportManager.logExceptionOrSafeException(it)
            })
    }

    private fun handleError(scanner_error: SCANNER_ERROR) {
        when (scanner_error) {
            BUSY, INTERRUPTED, TIMEOUT ->
                cancelCaptureUI()
            OUTDATED_SCANNER_INFO ->
                handleOutdatedScannerInfo()
            INVALID_STATE, SCANNER_UNREACHABLE, UN20_INVALID_STATE -> {
                cancelCaptureUI()
                reconnect()
            }
            UN20_SDK_ERROR -> // The UN20 throws an SDK reason if it doesn't detect a finger
                handleNoFingerTemplateDetected()
            else -> {
                cancelCaptureUI()
                presenter.handleException(UnexpectedScannerException.forScannerError(scanner_error, "CollectFingerprintsScanningHelper"))
            }
        }
    }

    private fun handleOutdatedScannerInfo() {
        cancelCaptureUI()
        scannerManager.scanner?.updateSensorInfo(object : ScannerCallback {
            override fun onSuccess() {
                resetUIFromError()
            }

            override fun onFailure(scanner_error: SCANNER_ERROR) {
                handleError(scanner_error)
            }
        })
    }

    private val resetUiScannerCallback = object : ScannerCallback {
        override fun onSuccess() {
            presenter.refreshDisplay()
            view.scanButton.isEnabled = true
            view.un20WakeupDialog.dismiss()
        }

        override fun onFailure(scanner_error: SCANNER_ERROR) {
            when (scanner_error) {
                BUSY -> resetUIFromError()
                INVALID_STATE -> reconnect()
                else -> presenter.handleException(UnexpectedScannerException.forScannerError(scanner_error, "CollectFingerprintsActivity"))
            }
        }
    }

    private fun resetUIFromError() {
        currentFingerStatus = NOT_COLLECTED
        presenter.currentFinger().template = null
        scannerManager.scanner?.resetUI(resetUiScannerCallback)
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

    private val startContinuousCaptureScannerCallback = object : ScannerCallback {
        override fun onSuccess() {
            view.timeoutBar.stopTimeoutBar()
            handleCaptureSuccess()
        }

        override fun onFailure(scanner_error: SCANNER_ERROR) {
            if (scanner_error == TIMEOUT)
                forceCapture()
            else handleError(scanner_error)
        }
    }

    private fun startContinuousCapture() {
        previousStatus = currentFingerStatus
        currentFingerStatus = COLLECTING
        presenter.refreshDisplay()
        view.scanButton.isEnabled = true
        presenter.refreshDisplay()
        view.timeoutBar.startTimeoutBar()
        scannerManager.scanner?.startContinuousCapture(qualityThreshold,
            timeoutInMillis.toLong(), startContinuousCaptureScannerCallback)
    }

    private fun stopContinuousCapture() {
        scannerManager.scanner?.stopContinuousCapture()
    }

    private fun forceCapture() {
        scannerManager.scanner?.forceCapture(qualityThreshold, object : ScannerCallback {
            override fun onSuccess() {
                handleCaptureSuccess()
            }

            override fun onFailure(scanner_error: SCANNER_ERROR) {
                handleError(scanner_error)
            }
        })
    }

    // For hardware version <=4, set bad scan if force capture isn't possible
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

    private fun handleCaptureSuccess() {
        scannerManager.scanner?.let {
            val template = it.template
            if (template != null) {
                val quality = it.imageQuality
                parseTemplateAndAddToCurrentFinger(template)
                setGoodOrBadScanFingerStatusToCurrentFinger(quality)
                Vibrate.vibrate(context)
                presenter.refreshDisplay()
                presenter.handleCaptureSuccess()
            } else {
                cancelCaptureUI()
                presenter.handleException(UnexpectedScannerException.forScannerError(UNEXPECTED, "CollectFingerprintsScanningHelper"))
            }
        }
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
        scannerManager.scanner?.resetUI(null)
    }

    fun stopReconnecting() {
        scannerManager.disconnectScannerIfNeeded()
    }

    fun disconnectScannerIfNeeded() {
        scannerManager.disconnectScannerIfNeeded()
    }

    fun setCurrentFingerAsSkippedAndAsNumberOfBadScansToAutoAddFinger() {
        currentFingerStatus = FINGER_SKIPPED
        presenter.currentFinger().numberOfBadScans = CollectFingerprintsPresenter.numberOfBadScansRequiredToAutoAddNewFinger
        presenter.refreshDisplay()
    }

    private fun logMessageForCrashReport(message: String) {
        crashReportManager.logMessageForCrashReport(FINGER_CAPTURE, UI, message = message)
    }
}
