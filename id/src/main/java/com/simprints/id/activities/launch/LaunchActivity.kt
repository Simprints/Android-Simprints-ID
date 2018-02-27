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
import com.simprints.id.activities.MainActivity
import com.simprints.id.activities.RefusalActivity
import com.simprints.id.controllers.Setup
import com.simprints.id.controllers.SetupCallback
import com.simprints.id.data.DataManager
import com.simprints.id.model.ALERT_TYPE
import com.simprints.id.tools.AppState
import com.simprints.id.tools.InternalConstants.*
import com.simprints.id.tools.LanguageHelper
import com.simprints.id.tools.Log
import com.simprints.id.tools.PositionTracker
import com.simprints.id.tools.Vibrate.vibrate
import com.simprints.id.tools.extensions.launchAlert
import com.simprints.libscanner.ButtonListener
import com.simprints.libscanner.SCANNER_ERROR
import com.simprints.libscanner.ScannerCallback
import kotlinx.android.synthetic.main.activity_launch.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.coroutines.experimental.bg

@SuppressLint("HardwareIds")
open class LaunchActivity : AppCompatActivity() {

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
    private lateinit var dataManager: DataManager
    private lateinit var positionTracker: PositionTracker
    private lateinit var appState: AppState
    private lateinit var setup: Setup
    private val timeHelper by lazy { app.timeHelper }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDependencies()
        initView()
        positionTracker.start()
        setup.start(this, getSetupCallback())
    }

    private fun injectDependencies() {
        app = application as Application
        dataManager = app.dataManager
        positionTracker = PositionTracker(this, dataManager)
        appState = app.appState
        setup = app.setup
    }

    private fun initView() {
        LanguageHelper.setLanguage(this, dataManager.language)
        setContentView(R.layout.activity_launch)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    // Setup callback
    private fun getSetupCallback(): SetupCallback =
        object : SetupCallback {
            override fun onSuccess() {
                dataManager.msSinceBootOnLoadEnd = timeHelper.msSinceBoot()
                // If it is the first time the launch process finishes, wait for consent confirmation
                // Else, go directly to the main activity
                if (!consentConfirmed) {
                    launchProgressBar.progress = 100
                    confirmConsentTextView.visibility = View.VISIBLE
                    loadingInfoTextView.visibility = View.INVISIBLE
                    waitingForConfirmation = true
                    appState.scanner.registerButtonListener(scannerButton)
                    vibrate(this@LaunchActivity, dataManager.vibrateMode, 100)
                } else {
                    finishLaunch()
                }
            }

            override fun onProgress(progress: Int, detailsId: Int) {
                Log.d(this@LaunchActivity, "onprogress")
                launchProgressBar.progress = progress
                loadingInfoTextView.setText(detailsId)
            }

            override fun onError(resultCode: Int, resultData: Intent) {
                finishWith(resultCode, resultData)
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
            MAIN_ACTIVITY_REQUEST -> when (resultCode) {
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
        dataManager.msSinceBootOnSessionEnd = timeHelper.msSinceBoot()
        async(UI) {
            try {
                bg { dataManager.saveSession() }.await()
            } catch (exception: Throwable) {
                Log.d(this@LaunchActivity, exception.message ?: "")
            }
            finish()
        }
    }

    override fun onDestroy() {
        positionTracker.finish()

        if (appState.scanner != null) {
            appState.scanner.disconnect(object : ScannerCallback {
                override fun onSuccess() {
                    appState.destroy()
                }

                override fun onFailure(scanner_error: SCANNER_ERROR) {
                    appState.destroy()
                }
            })
            appState.scanner = null
        }

        setup.destroy()
        super.onDestroy()
    }

    fun finishLaunch() {
        consentConfirmed = true
        waitingForConfirmation = false
        appState.scanner.unregisterButtonListener(scannerButton)
        startActivityForResult(Intent(this@LaunchActivity, MainActivity::class.java), MAIN_ACTIVITY_REQUEST)
        launchLayout.visibility = View.INVISIBLE
    }
}
