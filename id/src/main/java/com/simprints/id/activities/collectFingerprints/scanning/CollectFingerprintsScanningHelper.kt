package com.simprints.id.activities.collectFingerprints.scanning

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.collectFingerprints.CollectFingerprintsContract
import com.simprints.id.activities.collectFingerprints.CollectFingerprintsPresenter
import com.simprints.id.controllers.ScannerManager
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.Finger
import com.simprints.id.domain.Finger.Status.*
import com.simprints.id.exceptions.unsafe.SimprintsError
import com.simprints.id.exceptions.unsafe.UnexpectedScannerError
import com.simprints.id.tools.TimeoutBar
import com.simprints.id.tools.Vibrate
import com.simprints.id.tools.extensions.runOnUiThreadIfStillRunning
import com.simprints.libcommon.Fingerprint
import com.simprints.libscanner.ButtonListener
import com.simprints.libscanner.SCANNER_ERROR
import com.simprints.libscanner.SCANNER_ERROR.*
import com.simprints.libscanner.ScannerCallback
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class CollectFingerprintsScanningHelper(private val context: Context,
                                        private val view: CollectFingerprintsContract.View,
                                        private val presenter: CollectFingerprintsContract.Presenter) {

    @Inject lateinit var scannerManager: ScannerManager
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var analyticsManager: AnalyticsManager

    private var previousStatus: Finger.Status = NOT_COLLECTED
    private var currentFingerStatus: Finger.Status
        get() = presenter.currentFinger().status
        set(value) { presenter.currentFinger().status = value }

    private val scannerButtonListener = ButtonListener {
        if (presenter.isConfirmDialogShown)
            presenter.handleConfirmFingerprintsAndContinue()
        else if (shouldEnableScanButton())
            presenter.handleScannerButtonPressed()
    }

    private fun shouldEnableScanButton() = !presenter.isTryDifferentFingerSplashShown &&
        !presenter.isNudging

    init {
        ((view as Activity).application as Application).component.inject(this)

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
        TimeoutBar(context.applicationContext, view.progressBar, preferencesManager.timeoutS * 1000)

    // Creates a progress dialog when the scan gets disconnected
    private fun initUn20Dialog(): ProgressDialog =
        ProgressDialog(context).also { dialog ->
            dialog.isIndeterminate = true
            dialog.setCanceledOnTouchOutside(false)
            dialog.setMessage(context.getString(R.string.reconnecting_message))
            dialog.setOnCancelListener { view.cancelAndFinish() }
        }

    fun reconnect() {
        scannerManager.scanner?.unregisterButtonListener(scannerButtonListener)
        (view as Activity).runOnUiThreadIfStillRunning {
            view.un20WakeupDialog.show()
        }

        scannerManager.start()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribeBy(onComplete = {
                Timber.d( "reconnect.onSuccess()")
                view.un20WakeupDialog.dismiss()
                scannerManager.scanner?.registerButtonListener(scannerButtonListener)
            }, onError = {
                it.printStackTrace()
                Timber.d("reconnect.onError()")
                view.un20WakeupDialog.dismiss()
                view.doLaunchAlert(scannerManager.getAlertType(it))
                analyticsManager.logThrowable(it)
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
            UN20_SDK_ERROR -> // The UN20 throws an SDK error if it doesn't detect a finger
                handleNoFingerTemplateDetected()
            else -> {
                cancelCaptureUI()
                presenter.handleUnexpectedError(UnexpectedScannerError.forScannerError(scanner_error, "CollectFingerprintsScanningHelper"))
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
                else -> presenter.handleUnexpectedError(UnexpectedScannerError.forScannerError(scanner_error, "CollectFingerprintsActivity"))
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
        scannerManager.scanner?.startContinuousCapture(preferencesManager.qualityThreshold,
            (preferencesManager.timeoutS * 1000).toLong(), startContinuousCaptureScannerCallback)
    }

    private fun stopContinuousCapture() {
        scannerManager.scanner?.stopContinuousCapture()
    }

    private fun forceCapture() {
        scannerManager.scanner?.forceCapture(preferencesManager.qualityThreshold, object : ScannerCallback {
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
        Vibrate.vibrate(context, preferencesManager.vibrateMode)
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
            val quality = it.imageQuality
            parseTemplateAndAddToCurrentFinger(template)
            setGoodOrBadScanFingerStatusToCurrentFinger(quality)
            Vibrate.vibrate(context, preferencesManager.vibrateMode)
            presenter.refreshDisplay()
            presenter.handleCaptureSuccess()
        }
    }
    private fun parseTemplateAndAddToCurrentFinger(template: ByteArray) =
        try {
            presenter.currentFinger().template =
                Fingerprint(presenter.currentFinger().id, template)
        } catch (e: IllegalArgumentException) {
            // TODO : change exceptions in libcommon
            analyticsManager.logError(SimprintsError("IllegalArgumentException in CollectFingerprintsActivity.handleCaptureSuccess()", e))
            resetUIFromError()
        }

    private fun setGoodOrBadScanFingerStatusToCurrentFinger(quality: Int) =
        if (quality >= preferencesManager.qualityThreshold) {
            currentFingerStatus = Finger.Status.GOOD_SCAN
        } else {
            currentFingerStatus = Finger.Status.BAD_SCAN
            presenter.currentFinger().numberOfBadScans += 1
        }

    fun resetScannerUi() {
        scannerManager.scanner?.resetUI(null)
    }

    fun stopReconnecting() {
        scannerManager.disconnectScannerIfNeeded()
    }

    fun setCurrentFingerAsSkippedAndAsNumberOfBadScansToAutoAddFinger() {
        currentFingerStatus = Finger.Status.FINGER_SKIPPED
        presenter.currentFinger().numberOfBadScans = CollectFingerprintsPresenter.numberOfBadScansRequiredToAutoAddNewFinger
        presenter.refreshDisplay()
    }
}
