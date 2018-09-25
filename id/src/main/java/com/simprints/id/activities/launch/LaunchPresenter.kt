package com.simprints.id.activities.launch

import android.app.Activity
import android.content.Intent
import com.google.gson.JsonSyntaxException
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.controllers.ScannerManager
import com.simprints.id.controllers.ScannerManager.SetupStateDone
import com.simprints.id.data.DataManager
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.eventData.SessionEventsManager
import com.simprints.id.data.analytics.eventData.models.events.ConsentEvent
import com.simprints.id.data.analytics.eventData.models.events.ConsentEvent.Result.*
import com.simprints.id.data.analytics.eventData.models.events.ConsentEvent.Type.INDIVIDUAL
import com.simprints.id.data.analytics.eventData.models.events.ConsentEvent.Type.PARENTAL
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.domain.consent.GeneralConsent
import com.simprints.id.domain.consent.ParentalConsent
import com.simprints.id.exceptions.safe.setup.*
import com.simprints.id.exceptions.unsafe.MalformedConsentTextError
import com.simprints.id.tools.PositionTracker
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.json.JsonHelper
import com.simprints.libscanner.ButtonListener
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class LaunchPresenter(private val view: LaunchContract.View) : LaunchContract.Presenter {

    @Inject lateinit var dataManager: DataManager
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var analyticsManager: AnalyticsManager
    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var scannerManager: ScannerManager
    @Inject lateinit var timeHelper: TimeHelper
    @Inject lateinit var sessionEventsManager: SessionEventsManager

    private val activity = view as Activity
    private lateinit var positionTracker: PositionTracker
    private var syncSchedulerHelper: SyncSchedulerHelper

    // True iff the app is waiting for the user to confirm consent
    private var waitingForConfirmation = true

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
        val component = (activity.application as Application).component
        component.inject(this)
        startConsentEventTime = timeHelper.now()
        syncSchedulerHelper = SyncSchedulerHelper(component)
    }

    override fun start() {
        view.setLanguage(preferencesManager.language)
        view.initTextsInButtons()
        view.initConsentTabs()
        initPositionTracker()

        syncSchedulerHelper.scheduleSyncsAndStartPeopleSyncIfNecessary()
        setTextToConsentTabs()
    }

    private fun initPositionTracker() {
        positionTracker = PositionTracker(activity, preferencesManager)
        positionTracker.start()
    }

    private fun startScannerSetup() {
        //Permission
        //Check Person

        view.handleSetupProgress(30, R.string.launch_bt_connect)
        scannerManager.scanner?.unregisterButtonListener(scannerButton)

        scannerManager.start()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribeBy(
                onNext = {
                    when (it) {
                        SetupStateDone.DISCONNECT_VERO -> view.handleSetupProgress(15, R.string.launch_bt_connect)
                        SetupStateDone.INIT_VERO -> view.handleSetupProgress(45, R.string.launch_bt_connect)
                        SetupStateDone.CONNECTING_TO_VERO -> view.handleSetupProgress(60, R.string.launch_bt_connect)
                        SetupStateDone.RESET_UI -> view.handleSetupProgress(80, R.string.launch_setup)
                        SetupStateDone.WAKING_UP_VERO -> view.handleSetupProgress(90, R.string.launch_wake_un20)
                    }
                },
                onComplete = { handleSetupFinished() },
                onError = {
                    when (it) {
                        is BluetoothNotEnabledException -> view.doLaunchAlert(ALERT_TYPE.BLUETOOTH_NOT_ENABLED)
                        is BluetoothNotSupportedException -> view.doLaunchAlert(ALERT_TYPE.BLUETOOTH_NOT_SUPPORTED)
                        is MultipleScannersPairedException -> view.doLaunchAlert(ALERT_TYPE.MULTIPLE_PAIRED_SCANNERS)
                        is ScannerLowBatteryException -> view.doLaunchAlert(ALERT_TYPE.LOW_BATTERY)
                        is ScannerNotPairedException -> view.doLaunchAlert(ALERT_TYPE.NOT_PAIRED)
                        is ScannerUnbondedException -> view.doLaunchAlert(ALERT_TYPE.DISCONNECTED)
                        is UnknownBluetoothIssueException -> view.doLaunchAlert(ALERT_TYPE.DISCONNECTED)
                        else -> view.doLaunchAlert(ALERT_TYPE.UNEXPECTED_ERROR)
                    }

                    analyticsManager.logThrowable(it)
                })
    }

    private fun handleSetupFinished() {
        preferencesManager.msSinceBootOnLoadEnd = timeHelper.now()
        // If it is the first time the launch process finishes, wait for consent confirmation
        // Else, go directly to the collectFingerprintsActivity
        if (waitingForConfirmation) {
            view.handleSetupFinished()
            scannerManager.scanner?.registerButtonListener(scannerButton)
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
        //setup.onRequestPermissionsResult(activity, requestCode, permissions, grantResults)
    }

    override fun handleOnBackPressed() {
        addConsentEvent(NO_RESPONSE)
        handleOnBackOrDeclinePressed()
    }

    override fun handleDeclinePressed() {
        addConsentEvent(DECLINED)
        handleOnBackOrDeclinePressed()
    }

    private fun handleOnBackOrDeclinePressed() {
        launchOutOfFocus = true
        view.goToRefusalActivity()
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

    override fun updatePositionTracker(requestCode: Int, resultCode: Int, data: Intent?) {
        positionTracker.onActivityResult(requestCode, resultCode, data)
    }

    override fun handleOnDestroy() {
        positionTracker.finish()
        scannerManager.disconnectScannerIfNeeded()
    }

    override fun tryAgainFromErrorScreen() {}

    override fun tearDownAppWithResult(resultCode: Int, resultData: Intent?) {
        waitingForConfirmation = false
        preferencesManager.msSinceBootOnSessionEnd = timeHelper.now()
        dataManager.saveSession()
        view.setResultAndFinish(resultCode, resultData)
    }

    override fun confirmConsentAndContinueToNextActivity() {
        addConsentEvent(ACCEPTED)
        waitingForConfirmation = false
        view.continueToNextActivity()
    }

    override fun handleOnResume() {
        //StopShip
        launchOutOfFocus = false
        startScannerSetup()
    }

    override fun handleOnPause() {
        launchOutOfFocus = true
    }
}
