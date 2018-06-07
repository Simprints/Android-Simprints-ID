package com.simprints.id.activities.launch

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.RefusalActivity
import com.simprints.id.activities.main.MainActivity
import com.simprints.id.controllers.Setup
import com.simprints.id.controllers.SetupCallback
import com.simprints.id.data.DataManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.tools.*
import com.simprints.id.tools.InternalConstants.*
import com.simprints.id.tools.Vibrate.vibrate
import com.simprints.id.tools.extensions.launchAlert
import com.simprints.libscanner.ButtonListener
import com.simprints.libscanner.SCANNER_ERROR
import com.simprints.libscanner.ScannerCallback
import kotlinx.android.synthetic.main.activity_launch.*
import javax.inject.Inject

@SuppressLint("HardwareIds")
open class LaunchActivity : AppCompatActivity() {

    companion object {
        const val MAIN_ACTIVITY_REQUEST_CODE = LAST_GLOBAL_REQUEST_CODE + 1
    }

    // Scanner button callback
    private val scannerButton = ButtonListener {
        if (!launchOutOfFocus) {
            finishLaunch()
        }
    }

    // True iff the user confirmed consent
    private var consentConfirmed = false

    // True iff the app is waiting for the user to confirm consent
    private var waitingForConfirmation = false

    // True iff another activity launched by this activity is running
    private var launchOutOfFocus = false

    private lateinit var app: Application

    @Inject lateinit var dataManager: DataManager
    @Inject lateinit var preferencesManager: PreferencesManager
    private lateinit var positionTracker: PositionTracker
    @Inject lateinit var appState: AppState
    @Inject lateinit var setup: Setup
    @Inject lateinit var timeHelper: TimeHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDependencies()
        initView()
        positionTracker.start()
        setup.start(this, getSetupCallback())
    }

    private fun injectDependencies() {
        app = application as Application
        (application as Application).component.inject(this)
        positionTracker = PositionTracker(this, preferencesManager)
    }

    private fun initView() {
        LanguageHelper.setLanguage(this, preferencesManager.language)
        setContentView(R.layout.activity_launch)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    // Setup callback
    private fun getSetupCallback(): SetupCallback =
        object : SetupCallback {
            override fun onSuccess() {
                preferencesManager.msSinceBootOnLoadEnd = timeHelper.msSinceBoot()
                // If it is the first time the launch process finishes, wait for consent confirmation
                // Else, go directly to the main activity
                if (!consentConfirmed) {
                    launchProgressBar.progress = 100
                    confirmConsentTextView.visibility = View.VISIBLE
                    loadingInfoTextView.visibility = View.INVISIBLE
                    waitingForConfirmation = true
                    appState.scanner.registerButtonListener(scannerButton)
                    vibrate(this@LaunchActivity, preferencesManager.vibrateMode)
                } else {
                    finishLaunch()
                }
            }

            override fun onProgress(progress: Int, detailsId: Int) {
                Log.d(this@LaunchActivity, "onprogress")
                launchProgressBar.progress = progress
                loadingInfoTextView.setText(detailsId)
            }

            override fun onError(resultCode: Int) {
                finishWith(resultCode, null)
            }

            override fun onAlert(alertType: ALERT_TYPE) {
                launchAlertAndStopSetup(alertType)
            }
        }

    fun launchAlertAndStopSetup(alertType: ALERT_TYPE) {
        if (launchOutOfFocus)
            return

        launchOutOfFocus = true
        setup.stop()
        launchAlert(alertType)
    }

    private fun tryAgain() {
        launchOutOfFocus = false
        setup.start(this, getSetupCallback())
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (waitingForConfirmation) {
            finishLaunch()
            return true
        }
        return super.onTouchEvent(event)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        positionTracker.onRequestPermissionsResult(requestCode, permissions, grantResults)
        setup.onRequestPermissionsResult(this, requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        when (requestCode) {
            RESOLUTION_REQUEST, GOOGLE_SERVICE_UPDATE_REQUEST -> positionTracker.onActivityResult(requestCode, resultCode, data)
            MAIN_ACTIVITY_REQUEST_CODE -> when (resultCode) {
                RESULT_TRY_AGAIN -> tryAgain()

                Activity.RESULT_CANCELED, Activity.RESULT_OK -> finishWith(resultCode, data)
            }
            ALERT_ACTIVITY_REQUEST, REFUSAL_ACTIVITY_REQUEST -> when (resultCode) {
                RESULT_TRY_AGAIN -> tryAgain()

                else -> finishWith(resultCode, data)
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onBackPressed() {
        launchOutOfFocus = true
        setup.stop()
        startActivityForResult(Intent(this, RefusalActivity::class.java), REFUSAL_ACTIVITY_REQUEST)
    }

    /**
     * Close Simprints ID
     */
    private fun finishWith(resultCode: Int, resultData: Intent?) {
        waitingForConfirmation = false
        setResult(resultCode, resultData)
        preferencesManager.msSinceBootOnSessionEnd = timeHelper.msSinceBoot()
        dataManager.saveSession()
        finish()
    }

    override fun onDestroy() {
        positionTracker.finish()

        if (appState.scanner != null) {
            appState.scanner.disconnect(object : ScannerCallback {
                override fun onSuccess() {
                    //appState.destroy()
                }

                override fun onFailure(scanner_error: SCANNER_ERROR) {
                    //appState.destroy()
                }
            })
            //appState.scanner = null
        }
        super.onDestroy()
    }

    fun finishLaunch() {
        consentConfirmed = true
        waitingForConfirmation = false
        appState.scanner.unregisterButtonListener(scannerButton)
        startActivityForResult(Intent(this@LaunchActivity, MainActivity::class.java), MAIN_ACTIVITY_REQUEST_CODE)
        launchLayout.visibility = View.INVISIBLE
    }
}
