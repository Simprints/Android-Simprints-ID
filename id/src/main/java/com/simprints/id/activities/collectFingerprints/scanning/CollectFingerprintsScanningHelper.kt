package com.simprints.id.activities.collectFingerprints.scanning

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.view.View
import android.widget.ProgressBar
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.collectFingerprints.CollectFingerprintsActivity
import com.simprints.id.activities.collectFingerprints.CollectFingerprintsContract
import com.simprints.id.controllers.Setup
import com.simprints.id.controllers.SetupCallback
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.domain.Finger
import com.simprints.id.exceptions.unsafe.SimprintsError
import com.simprints.id.exceptions.unsafe.UnexpectedScannerError
import com.simprints.id.tools.AppState
import com.simprints.id.tools.Log
import com.simprints.id.tools.TimeoutBar
import com.simprints.id.tools.Vibrate
import com.simprints.id.tools.extensions.runOnUiThreadIfStillRunning
import com.simprints.libcommon.Fingerprint
import com.simprints.libscanner.ButtonListener
import com.simprints.libscanner.SCANNER_ERROR
import com.simprints.libscanner.ScannerCallback
import javax.inject.Inject


class CollectFingerprintsScanningHelper(private val context: Context,
                                        private val view: CollectFingerprintsContract.View,
                                        private val presenter: CollectFingerprintsContract.Presenter) {

    @Inject lateinit var appState: AppState
    @Inject lateinit var setup: Setup
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var analyticsManager: AnalyticsManager

    private var previousStatus: Finger.Status = Finger.Status.NOT_COLLECTED

    private val scannerButtonListener = ButtonListener {
        if (presenter.isConfirmDialogShown)
            presenter.handleConfirmFingerprintsAndContinue()
        else if (!presenter.currentFinger().isGoodScan)
            toggleContinuousCapture()
    }

    init {
        ((view as Activity).application as Application).component.inject(this)

        view.timeoutBar = initTimeoutBar()
        view.un20WakeupDialog = initUn20Dialog()
    }

    fun startListeners() {
        appState.scanner.registerButtonListener(scannerButtonListener)
    }

    fun stopListeners() {
        appState.scanner.unregisterButtonListener(scannerButtonListener)
    }

    private fun initTimeoutBar(): TimeoutBar =
        TimeoutBar(context.applicationContext, (view as Activity).findViewById<View>(R.id.pb_timeout) as ProgressBar,
        preferencesManager.timeoutS * 1000)

    // Creates a progress dialog when the scan gets disconnected
    private fun initUn20Dialog(): ProgressDialog = 
        ProgressDialog(context).also {
            it.isIndeterminate = true
            it.setCanceledOnTouchOutside(false)
            it.setMessage(context.getString(R.string.reconnecting_message))
            it.setOnCancelListener { view.cancelAndFinish() }
        }

    private val setupCallback = object : SetupCallback {
        override fun onSuccess() {
            Log.d(this@CollectFingerprintsScanningHelper, "reconnect.onSuccess()")
            view.un20WakeupDialog.dismiss()
            appState.scanner.registerButtonListener(scannerButtonListener)
        }

        override fun onProgress(progress: Int, detailsId: Int) {
            Log.d(this@CollectFingerprintsScanningHelper, "reconnect.onProgress()")
        }

        override fun onError(resultCode: Int) {
            Log.d(this@CollectFingerprintsScanningHelper, "reconnect.onError()")
            view.un20WakeupDialog.dismiss()
            view.doLaunchAlert(ALERT_TYPE.DISCONNECTED)
        }

        override fun onAlert(alertType: ALERT_TYPE) {
            Log.d(this@CollectFingerprintsScanningHelper, "reconnect.onAlert()")
            view.un20WakeupDialog.dismiss()
            view.doLaunchAlert(alertType)
        }
    }

    fun reconnect() {
        appState.scanner.unregisterButtonListener(scannerButtonListener)
        (view as Activity).runOnUiThreadIfStillRunning {
            view.un20WakeupDialog.show()
        }
        setup.start(view as CollectFingerprintsActivity, setupCallback)
    }

    private fun handleError(scanner_error: SCANNER_ERROR) {
        when (scanner_error) {
            SCANNER_ERROR.BUSY, SCANNER_ERROR.INTERRUPTED, SCANNER_ERROR.TIMEOUT ->
                cancelCaptureUI()
            SCANNER_ERROR.OUTDATED_SCANNER_INFO ->
                handleOutdatedScannerInfo()
            SCANNER_ERROR.INVALID_STATE, SCANNER_ERROR.SCANNER_UNREACHABLE, SCANNER_ERROR.UN20_INVALID_STATE -> {
                cancelCaptureUI()
                reconnect()
            }
            SCANNER_ERROR.UN20_SDK_ERROR ->
                forceCaptureNotPossible()
            else -> {
                cancelCaptureUI()
                presenter.handleUnexpectedError(UnexpectedScannerError.forScannerError(scanner_error, "CollectFingerprintsScanningHelper"))
            }
        }
    }

    private fun handleOutdatedScannerInfo() {
        cancelCaptureUI()
        appState.scanner.updateSensorInfo(object : ScannerCallback {
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
                SCANNER_ERROR.BUSY -> resetUIFromError()
                SCANNER_ERROR.INVALID_STATE -> reconnect()
                else -> presenter.handleUnexpectedError(UnexpectedScannerError.forScannerError(scanner_error, "CollectFingerprintsActivity"))
            }
        }
    }

    private fun resetUIFromError() {
        presenter.currentFinger().status = Finger.Status.NOT_COLLECTED
        presenter.currentFinger().template = null
        appState.scanner.resetUI(resetUiScannerCallback)
    }

    // it start/stop the scan based on the activeFingers[currentActiveFingerNo] state
    fun toggleContinuousCapture() {
        when (presenter.currentFinger().status!!) {
            Finger.Status.GOOD_SCAN ->
                askIfWantRescan()
            Finger.Status.RESCAN_GOOD_SCAN, Finger.Status.BAD_SCAN, Finger.Status.NOT_COLLECTED ->
                startContinuousCapture()
            Finger.Status.COLLECTING ->
                stopContinuousCapture()
        }
    }

    private fun askIfWantRescan() {
        presenter.currentFinger().status = Finger.Status.RESCAN_GOOD_SCAN
        presenter.refreshDisplay()
    }

    private val startContinuousCaptureScannerCallback = object : ScannerCallback {
        override fun onSuccess() {
            view.timeoutBar.stopTimeoutBar()
            handleCaptureSuccess()
        }

        override fun onFailure(scanner_error: SCANNER_ERROR) {
            if (scanner_error == SCANNER_ERROR.TIMEOUT)
                forceCapture()
            else handleError(scanner_error)
        }
    }

    private fun startContinuousCapture() {
        previousStatus = presenter.currentFinger().status
        presenter.currentFinger().status = Finger.Status.COLLECTING
        presenter.refreshDisplay()
        view.scanButton.isEnabled = true
        presenter.refreshDisplay()
        view.timeoutBar.startTimeoutBar()
        appState.scanner.startContinuousCapture(preferencesManager.qualityThreshold,
            (preferencesManager.timeoutS * 1000).toLong(), startContinuousCaptureScannerCallback)
    }

    private fun stopContinuousCapture() {
        appState.scanner.stopContinuousCapture()
    }

    private fun forceCapture() {
        appState.scanner.forceCapture(preferencesManager.qualityThreshold, object : ScannerCallback {
            override fun onSuccess() {
                handleCaptureSuccess()
            }

            override fun onFailure(scanner_error: SCANNER_ERROR) {
                handleError(scanner_error)
            }
        })
    }

    // For hardware version <=4, set bad scan if force capture isn't possible
    private fun forceCaptureNotPossible() {
        presenter.currentFinger().status = Finger.Status.BAD_SCAN
        Vibrate.vibrate(context, preferencesManager.vibrateMode)
        presenter.refreshDisplay()
    }

    private fun cancelCaptureUI() {
        presenter.currentFinger().status = previousStatus
        view.timeoutBar.cancelTimeoutBar()
        presenter.refreshDisplay()
    }

    private fun handleCaptureSuccess() {
        val finger = presenter.currentFinger()
        val quality = appState.scanner.imageQuality
        parseTemplateAndAddToCurrentFinger(finger)
        setGoodOrBadScanAndNudgeIfNecessary(quality)
        Vibrate.vibrate(context, preferencesManager.vibrateMode)
        presenter.refreshDisplay()
        presenter.checkScannedFingersAndCreateMapToShowDialog()
    }

    private fun parseTemplateAndAddToCurrentFinger(finger: Finger) =
        try {
            presenter.currentFinger().template =
                Fingerprint(finger.id, appState.scanner.template)
        } catch (e: IllegalArgumentException) {
            // TODO : change exceptions in libcommon
            analyticsManager.logError(SimprintsError("IllegalArgumentException in CollectFingerprintsActivity.handleCaptureSuccess()", e))
            resetUIFromError()
        }

    private fun setGoodOrBadScanAndNudgeIfNecessary(quality: Int) =
        if (quality >= preferencesManager.qualityThreshold) {
            handleGoodScan()
        } else {
            handleBadScan()
        }

    private fun handleGoodScan() {
        presenter.currentFinger().status = Finger.Status.GOOD_SCAN
        presenter.doNudgeIfNecessary()
    }

    private fun handleBadScan() {
        presenter.currentFinger().status = Finger.Status.BAD_SCAN
        presenter.currentFinger().numberOfBadScans += 1
        presenter.addNewFingerIfNecessary()
    }

    fun resetScannerUi() {
        appState.scanner.resetUI(null)
    }

    fun stopReconnecting() {
        setup.stop()
    }
}
