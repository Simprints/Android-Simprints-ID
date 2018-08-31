package com.simprints.id.activities.launch

import android.app.Activity
import android.content.Intent
import com.google.gson.JsonSyntaxException
import com.simprints.id.Application
import com.simprints.id.controllers.Setup
import com.simprints.id.controllers.SetupCallback
import com.simprints.id.data.DataManager
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.eventData.SessionEventsManager
import com.simprints.id.data.analytics.eventData.models.events.ConsentEvent
import com.simprints.id.data.analytics.eventData.models.events.ConsentEvent.Result.*
import com.simprints.id.data.analytics.eventData.models.events.ConsentEvent.Type.INDIVIDUAL
import com.simprints.id.data.analytics.eventData.models.events.ConsentEvent.Type.PARENTAL
import com.simprints.id.data.db.sync.SyncManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.services.scheduledSync.peopleSync.ScheduledPeopleSyncManager
import com.simprints.id.services.scheduledSync.sessionSync.ScheduledSessionsSyncManager
import com.simprints.id.domain.consent.GeneralConsent
import com.simprints.id.domain.consent.ParentalConsent
import com.simprints.id.exceptions.unsafe.MalformedConsentTextError
import com.simprints.id.services.sync.SyncCategory
import com.simprints.id.services.sync.SyncTaskParameters
import com.simprints.id.tools.AppState
import com.simprints.id.tools.Log
import com.simprints.id.tools.PositionTracker
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.json.JsonHelper
import com.simprints.libscanner.ButtonListener
import com.simprints.libscanner.SCANNER_ERROR
import com.simprints.libscanner.ScannerCallback
import javax.inject.Inject

class LaunchPresenter(private val view: LaunchContract.View) : LaunchContract.Presenter {

    @Inject lateinit var dataManager: DataManager
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var analyticsManager: AnalyticsManager
    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var syncManager: SyncManager
    @Inject lateinit var scheduledPeopleSyncManager: ScheduledPeopleSyncManager
    @Inject lateinit var scheduledSessionsSyncManager: ScheduledSessionsSyncManager
    @Inject lateinit var appState: AppState
    @Inject lateinit var setup: Setup
    @Inject lateinit var timeHelper: TimeHelper
    @Inject lateinit var sessionEventsManager: SessionEventsManager

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

    private var startConsentEventTime: Long = 0

    init {
        (activity.application as Application).component.inject(this)
        startConsentEventTime = timeHelper.now()
    }

    override fun start() {
        view.setLanguage(preferencesManager.language)
        view.initTextsInButtons()
        view.initConsentTabs()
        initPositionTracker()
        initSetup()
        initBackgroundSyncIfNecessary()
        schedulePeopleSyncIfNecessary()
        scheduleSessionsSyncIfNecessary()
        setTextToConsentTabs()
    }

    private fun initPositionTracker() {
        positionTracker = PositionTracker(activity, preferencesManager)
        positionTracker.start()
    }

    private fun initSetup() {
        setup.start(activity, setupCallback)
    }

    private fun initBackgroundSyncIfNecessary() {
        if (preferencesManager.syncOnCallout) {
            syncManager.sync(SyncTaskParameters.build(preferencesManager.syncGroup, preferencesManager.moduleId, loginInfoManager), SyncCategory.AT_LAUNCH)
        }
    }

    private fun schedulePeopleSyncIfNecessary() {
        if (preferencesManager.scheduledBackgroundSync) {
            scheduledPeopleSyncManager.scheduleSyncIfNecessary()
        } else {
            scheduledPeopleSyncManager.deleteSyncIfNecessary()
        }
    }

    private fun scheduleSessionsSyncIfNecessary() {
        scheduledSessionsSyncManager.scheduleSyncIfNecessary()
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
        preferencesManager.msSinceBootOnLoadEnd = timeHelper.now()
        // If it is the first time the launch process finishes, wait for consent confirmation
        // Else, go directly to the collectFingerprintsActivity
        if (!consentConfirmed) {
            view.handleSetupFinished()
            waitingForConfirmation = true
            appState.scanner.registerButtonListener(scannerButton)
            view.doVibrateIfNecessary(preferencesManager.vibrateMode)
        } else {
            confirmConsentAndContinueToNextActivity()
        }
    }

    private fun setTextToConsentTabs() {
        view.setTextToGeneralConsent(getGeneralConsentText())
        if (preferencesManager.parentalConsentExists) {
            view.addParentalConsentTabWithText(getParentalConsentText())
        }
    }

    private fun getGeneralConsentText(): String {
        val generalConsent = try {
            JsonHelper.gson.fromJson(preferencesManager.generalConsentOptionsJson, GeneralConsent::class.java)
        } catch (e: JsonSyntaxException) {
            analyticsManager.logError(MalformedConsentTextError("Malformed General Consent Text Error", e))
            GeneralConsent()
        }
        return generalConsent.assembleText(activity, preferencesManager.calloutAction, preferencesManager.programName, preferencesManager.organizationName)
    }

    private fun getParentalConsentText(): String {
        val parentalConsent = try {
            JsonHelper.gson.fromJson(preferencesManager.parentalConsentOptionsJson, ParentalConsent::class.java)
        } catch (e: JsonSyntaxException) {
            analyticsManager.logError(MalformedConsentTextError("Malformed Parental Consent Text Error", e))
            ParentalConsent()
        }
        return parentalConsent.assembleText(activity, preferencesManager.calloutAction, preferencesManager.programName, preferencesManager.organizationName)
    }

    override fun handleOnRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        positionTracker.onRequestPermissionsResult(requestCode, permissions, grantResults)
        setup.onRequestPermissionsResult(activity, requestCode, permissions, grantResults)
    }

    override fun handleOnBackPressed() {
        addConsentEvent(NO_RESPONSE)
        handleOnBackOrDeclinePressed()
    }

    override fun handleDeclinePressed() {
        addConsentEvent(DECLINED)
        handleOnBackOrDeclinePressed()
    }

    private fun addConsentEvent(result: ConsentEvent.Result) {

        sessionEventsManager.updateSessionInBackground({
            it.events.add(
                ConsentEvent(
                    it.timeRelativeToStartTime(startConsentEventTime),
                    it.nowRelativeToStartTime(timeHelper),
                    if (view.isCurrentTabParental()) {
                        PARENTAL
                    } else {
                        INDIVIDUAL
                    },
                    result))

            if (result == DECLINED || result == NO_RESPONSE) {
                it.location = null
            }
        })
    }

    private fun handleOnBackOrDeclinePressed() {
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
        view.doLaunchAlert(alertType)
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
        preferencesManager.msSinceBootOnSessionEnd = timeHelper.now()
        dataManager.saveSession()
        view.setResultAndFinish(resultCode, resultData)
    }

    override fun confirmConsentAndContinueToNextActivity() {
        addConsentEvent(ACCEPTED)

        consentConfirmed = true
        waitingForConfirmation = false
        view.continueToNextActivity()
    }

    override fun handleOnResume() {
        if (waitingForConfirmation) {
            appState.scanner.registerButtonListener(scannerButton)
        }
    }

    override fun handleOnPause() {
        if (appState.scanner != null) {
            appState.scanner.unregisterButtonListener(scannerButton)
        }
    }
}
