package com.simprints.id.activities.collectFingerprints.scanning

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
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
import com.simprints.id.tools.Vibrate
import com.simprints.id.tools.extensions.runOnUiThreadIfStillRunning
import com.simprints.libcommon.Fingerprint
import com.simprints.libscanner.ButtonListener
import com.simprints.libscanner.SCANNER_ERROR
import com.simprints.libscanner.ScannerCallback
import kotlinx.android.synthetic.main.content_main.*
import javax.inject.Inject


class CollectFingerprintsScanningHelper(private val context: Context,
                                        private val view: CollectFingerprintsContract.View,
                                        private val presenter: CollectFingerprintsContract.Presenter) {

    @Inject lateinit var appState: AppState
    @Inject lateinit var setup: Setup
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var analyticsManager: AnalyticsManager

    private val scannerButtonListener = ButtonListener {
        if (view.buttonContinue)
            view.onActionForward()
        else if (!presenter.activeFingers[presenter.currentActiveFingerNo].isGoodScan)
            toggleContinuousCapture()
    }

    init {
        ((view as Activity).application as Application).component.inject(this)

        view.un20WakeupDialog = initUn20Dialog()
    }

    fun startListeners() {
        appState.scanner.registerButtonListener(scannerButtonListener)
    }

    fun stopListeners() {
        appState.scanner.unregisterButtonListener(scannerButtonListener)
    }

    // Creates a progress dialog when the scan gets disconnected
    private fun initUn20Dialog(): ProgressDialog {
        val dialog = ProgressDialog(context)
        dialog.isIndeterminate = true
        dialog.setCanceledOnTouchOutside(false)
        dialog.setMessage(context.getString(R.string.reconnecting_message))
        dialog.setOnCancelListener { view.cancelAndFinish() }
        return dialog
    }

    fun reconnect() {
        appState.scanner.unregisterButtonListener(scannerButtonListener)

        val setupCallback = object : SetupCallback {
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

        (view as Activity).runOnUiThreadIfStillRunning {
            view.un20WakeupDialog.show()
        }

        setup.start(view as CollectFingerprintsActivity, setupCallback)
    }

    private fun handleError(scanner_error: SCANNER_ERROR) {
        when (scanner_error) {
            SCANNER_ERROR.BUSY, SCANNER_ERROR.INTERRUPTED, SCANNER_ERROR.TIMEOUT -> cancelCaptureUI()

            SCANNER_ERROR.OUTDATED_SCANNER_INFO -> {
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

            SCANNER_ERROR.INVALID_STATE, SCANNER_ERROR.SCANNER_UNREACHABLE, SCANNER_ERROR.UN20_INVALID_STATE -> {
                cancelCaptureUI()
                reconnect()
            }

            SCANNER_ERROR.UN20_SDK_ERROR -> forceCaptureNotPossible()

            else -> {
                cancelCaptureUI()
                handleUnexpectedError(UnexpectedScannerError.forScannerError(scanner_error, "CollectFingerprintsActivity"))
            }
        }
    }

    private fun resetUIFromError() {
        presenter.activeFingers[presenter.currentActiveFingerNo].status = Finger.Status.NOT_COLLECTED
        presenter.activeFingers[presenter.currentActiveFingerNo].template = null

        appState.scanner.resetUI(object : ScannerCallback {
            override fun onSuccess() {
                refreshDisplay()
                scan_button.isEnabled = true
                view.un20WakeupDialog.dismiss()
            }

            override fun onFailure(scanner_error: SCANNER_ERROR) {
                when (scanner_error) {
                    SCANNER_ERROR.BUSY -> resetUIFromError()
                    SCANNER_ERROR.INVALID_STATE -> reconnect()
                    else -> handleUnexpectedError(UnexpectedScannerError.forScannerError(scanner_error, "CollectFingerprintsActivity"))
                }
            }
        })
    }

    // it start/stop the scan based on the activeFingers[currentActiveFingerNo] state
    fun toggleContinuousCapture() {
        val finger = presenter.activeFingers[presenter.currentActiveFingerNo]

        when (finger.status) {
            Finger.Status.GOOD_SCAN -> {
                presenter.activeFingers[presenter.currentActiveFingerNo].isRescanGoodScan
                refreshDisplay()
            }
            Finger.Status.RESCAN_GOOD_SCAN, Finger.Status.BAD_SCAN, Finger.Status.NOT_COLLECTED -> {
                previousStatus = finger.status
                finger.status = Finger.Status.COLLECTING
                refreshDisplay()
                scan_button.isEnabled = true
                refreshDisplay()
                startContinuousCapture()
            }
            Finger.Status.COLLECTING -> stopContinuousCapture()
        }
    }

    private fun startContinuousCapture() {
        timeoutBar.startTimeoutBar()

        appState.scanner.startContinuousCapture(preferencesManager.qualityThreshold,
            (preferencesManager.timeoutS * 1000).toLong(), object : ScannerCallback {
            override fun onSuccess() {
                timeoutBar.stopTimeoutBar()
                captureSuccess()
            }

            override fun onFailure(scanner_error: SCANNER_ERROR) {
                if (scanner_error == SCANNER_ERROR.TIMEOUT)
                    forceCapture()
                else handleError(scanner_error)
            }
        })
    }

    private fun stopContinuousCapture() {
        appState.scanner.stopContinuousCapture()
    }

    private fun forceCapture() {
        appState.scanner.forceCapture(preferencesManager.qualityThreshold, object : ScannerCallback {
            override fun onSuccess() {
                captureSuccess()
            }

            override fun onFailure(scanner_error: SCANNER_ERROR) {
                handleError(scanner_error)
            }
        })
    }

    /**
     * For hardware version <=4, set bad scan if force capture isn't possible
     */
    private fun forceCaptureNotPossible() {
        presenter.activeFingers[presenter.currentActiveFingerNo].status = Finger.Status.BAD_SCAN
        Vibrate.vibrate(context, preferencesManager.vibrateMode)
        refreshDisplay()
    }

    private fun cancelCaptureUI() {
        presenter.activeFingers[presenter.currentActiveFingerNo].status = previousStatus
        timeoutBar.cancelTimeoutBar()
        refreshDisplay()
    }

    private fun captureSuccess() {
        val finger = presenter.activeFingers[presenter.currentActiveFingerNo]
        val quality = appState.scanner.imageQuality

        if (finger.template == null || finger.template.qualityScore < quality) {
            try {
                presenter.activeFingers[presenter.currentActiveFingerNo].template = Fingerprint(
                    finger.id,
                    appState.scanner.template)
                // TODO : change exceptions in libcommon
            } catch (ex: IllegalArgumentException) {
                analyticsManager
                    .logError(SimprintsError("IllegalArgumentException in CollectFingerprintsActivity.captureSuccess()"))
                resetUIFromError()
                return
            }
        }

        val qualityScore1 = preferencesManager.qualityThreshold

        if (quality >= qualityScore1) {
            presenter.activeFingers[presenter.currentActiveFingerNo].status = Finger.Status.GOOD_SCAN
            nudgeMode()
        } else {
            presenter.activeFingers[presenter.currentActiveFingerNo].status = Finger.Status.BAD_SCAN
        }

        Vibrate.vibrate(context, preferencesManager.vibrateMode)
        refreshDisplay()
    }

    fun stopReconnecting() {
        setup.stop()
    }
}
