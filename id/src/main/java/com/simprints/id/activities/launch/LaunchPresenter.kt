package com.simprints.id.activities.launch

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.simprints.id.Application
import com.simprints.id.controllers.Setup
import com.simprints.id.controllers.SetupCallback
import com.simprints.id.data.DataManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.tools.*
import com.simprints.id.tools.Vibrate.vibrate
import com.simprints.id.tools.extensions.launchAlert
import com.simprints.libscanner.ButtonListener
import com.simprints.libscanner.SCANNER_ERROR
import com.simprints.libscanner.ScannerCallback
import javax.inject.Inject


class LaunchPresenter(private val context: Context,
                      private val view: LaunchContract.View) : LaunchContract.Presenter {

    @Inject lateinit var dataManager: DataManager
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var appState: AppState
    @Inject lateinit var setup: Setup
    @Inject lateinit var timeHelper: TimeHelper

    private val activity = view as Activity
    private lateinit var positionTracker: PositionTracker

    // True iff the user confirmed consent
    private var consentConfirmed = false

    // True iff the app is waiting for the user to confirm consent
    private var waitingForConfirmation = false

    // True iff another activity launched by this activity is running
    private var launchOutOfFocus = false

    // Scanner button callback
    private val scannerButton = ButtonListener {
        if (!launchOutOfFocus) {
            confirmConsentAndContinueToNextActivity()
        }
    }

    init {
        (activity.application as Application).component.inject(this)
    }

    override fun start() {
        LanguageHelper.setLanguage(context, preferencesManager.language)
        initPositionTracker()
        initSetup()
    }

    private fun initPositionTracker() {
        positionTracker = PositionTracker(activity, preferencesManager)
        positionTracker.start()
    }

    private fun initSetup() {
        setup.start(activity, setupCallback)
    }

    private val setupCallback = object : SetupCallback {
        override fun onSuccess() {
            handleSetupFinished()
        }

        override fun onProgress(progress: Int, detailsId: Int) {
            Log.d(this@LaunchPresenter, "onProgress")
            view.handleSetupProgress(progress, detailsId)
        }

        override fun onError(resultCode: Int) {
            view.setResultAndFinish(resultCode, null)
        }

        override fun onAlert(alertType: ALERT_TYPE) {
            stopSetupAndLaunchAlert(alertType)
        }
    }

    private fun handleSetupFinished() {
        preferencesManager.msSinceBootOnLoadEnd = timeHelper.msSinceBoot()
        // If it is the first time the launch process finishes, wait for consent confirmation
        // Else, go directly to the collectFingerprintsActivity
        if (!consentConfirmed) {
            view.handleSetupFinished()
            waitingForConfirmation = true
            appState.scanner.registerButtonListener(scannerButton)
            vibrate(context, preferencesManager.vibrateMode)
        } else {
            confirmConsentAndContinueToNextActivity()
        }
    }

    override fun handleOnRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        positionTracker.onRequestPermissionsResult(requestCode, permissions, grantResults)
        setup.onRequestPermissionsResult(activity, requestCode, permissions, grantResults)
    }

    override fun handleOnBackOrDeclinePressed() {
        launchOutOfFocus = true
        setup.stop()
        view.goToRefusalActivity()
    }

    override fun updatePositionTracker(requestCode: Int, resultCode: Int, data: Intent?) {
        positionTracker.onActivityResult(requestCode, resultCode, data)
    }

    fun stopSetupAndLaunchAlert(alertType: ALERT_TYPE) {
        if (launchOutOfFocus)
            return
        launchOutOfFocus = true
        setup.stop()
        activity.launchAlert(alertType)
    }

    override fun handleOnDestroy() {
        positionTracker.finish()
        disconnectScannerIfNeeded()
    }

    private fun disconnectScannerIfNeeded() {
        if (appState.scanner != null) {
            appState.scanner.disconnect(object : ScannerCallback {
                override fun onSuccess() {}
                override fun onFailure(scanner_error: SCANNER_ERROR) {}
            })
        }
    }

    override fun tryAgainFromErrorScreen() {
        launchOutOfFocus = false
        initSetup()
    }

    override fun isReadyToProceedToNextActivity(): Boolean = waitingForConfirmation

    override fun tearDownAppWithResult(resultCode: Int, resultData: Intent?) {
        waitingForConfirmation = false
        preferencesManager.msSinceBootOnSessionEnd = timeHelper.msSinceBoot()
        dataManager.saveSession()
        view.setResultAndFinish(resultCode, resultData)
    }

    override fun confirmConsentAndContinueToNextActivity() {
        consentConfirmed = true
        waitingForConfirmation = false
        appState.scanner.unregisterButtonListener(scannerButton)
        view.continueToNextActivity()
    }
}
