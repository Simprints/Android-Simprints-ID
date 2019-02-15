package com.simprints.id.activities.launch

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import com.google.android.gms.location.LocationRequest
import com.google.gson.JsonSyntaxException
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.data.DataManager
import com.simprints.id.data.analytics.crashReport.CrashReportManager
import com.simprints.id.data.analytics.eventData.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventData.models.domain.events.CandidateReadEvent
import com.simprints.id.data.analytics.eventData.models.domain.events.ConsentEvent
import com.simprints.id.data.analytics.eventData.models.domain.events.ConsentEvent.Result.*
import com.simprints.id.data.analytics.eventData.models.domain.events.ConsentEvent.Type.INDIVIDUAL
import com.simprints.id.data.analytics.eventData.models.domain.events.ConsentEvent.Type.PARENTAL
import com.simprints.id.data.analytics.eventData.models.domain.events.ScannerConnectionEvent
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.PersonFetchResult
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.domain.consent.GeneralConsent
import com.simprints.id.domain.consent.ParentalConsent
import com.simprints.id.exceptions.unexpected.MalformedConsentTextException
import com.simprints.id.scanner.ScannerManager
import com.simprints.id.services.scheduledSync.SyncSchedulerHelper
import com.simprints.id.session.callout.CalloutAction
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.json.JsonHelper
import com.simprints.id.tools.utils.SimNetworkUtils
import com.simprints.libcommon.Person
import com.simprints.libscanner.ButtonListener
import com.tbruyelle.rxpermissions2.Permission
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class LaunchPresenter(private val view: LaunchContract.View) : LaunchContract.Presenter {

    @Inject lateinit var dataManager: DataManager
    @Inject lateinit var dbManager: DbManager
    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var simNetworkUtils: SimNetworkUtils
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var scannerManager: ScannerManager
    @Inject lateinit var timeHelper: TimeHelper
    @Inject lateinit var sessionEventsManager: SessionEventsManager
    @Inject lateinit var syncSchedulerHelper: SyncSchedulerHelper

    private var startConsentEventTime: Long = 0
    private val activity = view as Activity
    private var permissionsAlreadyRequested = false

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

    init {
        val component = (activity.application as Application).component
        component.inject(this)
        startConsentEventTime = timeHelper.now()
    }

    override fun start() {
        view.setLanguage(preferencesManager.language)
        view.initTextsInButtons()
        view.initConsentTabs()

        syncSchedulerHelper.scheduleBackgroundSyncs()
        syncSchedulerHelper.startDownSyncOnLaunchIfPossible()

        setTextToConsentTabs()

        startSetup()
        initOrUpdateAnalyticsKeys()
    }

    private fun startSetup() {
        requestPermissionsForLocation(5)
            .andThen(checkIfVerifyAndGuidExists(15))
            .andThen(veroTask(30, R.string.launch_bt_connect, scannerManager.disconnectVero()))
            .andThen(veroTask(45, R.string.launch_bt_connect, scannerManager.initVero()))
            .andThen(veroTask(60, R.string.launch_bt_connect, scannerManager.connectToVero()) { addBluetoothConnectivityEvent() })
            .andThen(veroTask(75, R.string.launch_setup, scannerManager.resetVeroUI()))
            .andThen(veroTask(90, R.string.launch_wake_un20, scannerManager.wakeUpVero()) { updateBluetoothConnectivityEventWithVeroInfo() })
            .subscribeBy(onError = { it.printStackTrace() }, onComplete = { handleSetupFinished() })
    }

    private fun updateBluetoothConnectivityEventWithVeroInfo() {
        sessionEventsManager.updateHardwareVersionInScannerConnectivityEvent(preferencesManager.hardwareVersionString)
    }

    private fun veroTask(progress: Int, messageRes: Int, task: Completable, callback: (() -> Unit)? = null): Completable =
        Completable.fromAction { view.handleSetupProgress(progress, messageRes) }
            .andThen(task)
            .andThen(Completable.fromAction { callback?.invoke() })
            .doOnError { manageVeroErrors(it) }

    private fun checkIfVerifyAndGuidExists(progress: Int): Completable =
        Completable.fromAction { view.handleSetupProgress(progress, R.string.launch_checking_person_in_db) }
            .andThen(tryToFetchGuid())

    private fun tryToFetchGuid(): Completable {
        return if (preferencesManager.calloutAction != CalloutAction.VERIFY) {
            Completable.complete()
        } else {
            val guid = preferencesManager.patientId
            val startCandidateSearchTime = timeHelper.now()
            dbManager.loadPerson(loginInfoManager.getSignedInProjectIdOrEmpty(), guid).doOnSuccess { personFetchResult ->
                handleGuidFound(personFetchResult, guid, startCandidateSearchTime)
            }.doOnError {
                it.printStackTrace()
                // For any error, we show the missing guid screen.
                saveNotFoundVerificationAndShowAlert(Person(guid), startCandidateSearchTime)
            }.ignoreElement()
        }
    }

    private fun handleGuidFound(result: PersonFetchResult, guid: String, startCandidateSearchTime: Long) {
        Timber.d("Setup: GUID found.")
        val isPersonFromLocalDb = !result.fetchedOnline
        saveEventForCandidateReadInBackgroundNotFound(
            guid,
            startCandidateSearchTime,
            if (isPersonFromLocalDb) CandidateReadEvent.LocalResult.NOT_FOUND else CandidateReadEvent.LocalResult.FOUND,
            if (isPersonFromLocalDb) CandidateReadEvent.RemoteResult.FOUND else CandidateReadEvent.RemoteResult.NOT_FOUND)
    }

    private fun saveNotFoundVerificationAndShowAlert(probe: Person, startCandidateSearchTime: Long) {
        if (simNetworkUtils.isConnected()) {
            // We've synced with the online dbManager and they're not in the dbManager
            view.doLaunchAlert(ALERT_TYPE.GUID_NOT_FOUND_ONLINE)
            saveEventForCandidateReadInBackgroundNotFound(probe.guid, startCandidateSearchTime, CandidateReadEvent.LocalResult.NOT_FOUND, CandidateReadEvent.RemoteResult.NOT_FOUND)
        } else {
            // We're offline but might find the person if we sync
            view.doLaunchAlert(ALERT_TYPE.GUID_NOT_FOUND_OFFLINE)
            saveEventForCandidateReadInBackgroundNotFound(probe.guid, startCandidateSearchTime, CandidateReadEvent.LocalResult.NOT_FOUND, null)
        }
    }

    private fun saveEventForCandidateReadInBackgroundNotFound(guid: String,
                                                              startCandidateSearchTime: Long,
                                                              localResult: CandidateReadEvent.LocalResult,
                                                              remoteResult: CandidateReadEvent.RemoteResult?) {
        sessionEventsManager.addEventForCandidateReadInBackground(
            guid,
            startCandidateSearchTime,
            localResult,
            remoteResult)
    }

    private fun manageVeroErrors(it: Throwable) {
        it.printStackTrace()
        view.doLaunchAlert(scannerManager.getAlertType(it))
        crashReportManager.logThrowable(it)
    }

    private fun requestPermissionsForLocation(progress: Int): Completable {
        view.handleSetupProgress(progress, R.string.launch_checking_permissions)
        val permissionsNeeded = arrayListOf(Manifest.permission.ACCESS_FINE_LOCATION)

        val permissionsToRequest = if (permissionsAlreadyRequested) {
            0
        } else {
            permissionsNeeded.size
        }
        val requestForPermissions = view.requestPermissions(permissionsNeeded)

        return requestForPermissions
            .take(permissionsToRequest.toLong())
            .toList()
            .flatMapCompletable { permissions ->
                collectLocationIfPermitted(permissions)
                permissionsAlreadyRequested = true
                Completable.complete()
            }
    }

    @SuppressLint("MissingPermission")
    private fun collectLocationIfPermitted(permissions: List<Permission>) {
        if (!permissionsAlreadyRequested &&
            permissions.first { it.name == Manifest.permission.ACCESS_FINE_LOCATION }.granted) {
            val req = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            view.getLocationProvider()
                .getUpdatedLocation(req)
                .firstOrError()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeBy(onSuccess = {
                    preferencesManager.location = com.simprints.id.domain.Location.fromAndroidLocation(it)
                    sessionEventsManager.addLocationToSession(it.latitude, it.longitude)
                }, onError = { it.printStackTrace() })
        }
    }

    private fun handleSetupFinished() {
        preferencesManager.msSinceBootOnLoadEnd = timeHelper.now()
        view.handleSetupFinished()
        scannerManager.scanner?.registerButtonListener(scannerButton)
        view.doVibrateIfNecessary(preferencesManager.vibrateMode)
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
            crashReportManager.logException(MalformedConsentTextException("Malformed General Consent Text Error", e))
            GeneralConsent()
        }
        return generalConsent.assembleText(activity, preferencesManager.calloutAction, preferencesManager.programName, preferencesManager.organizationName)
    }

    private fun getParentalConsentText(): String {
        val parentalConsent = try {
            JsonHelper.gson.fromJson(preferencesManager.parentalConsentOptionsJson, ParentalConsent::class.java)
        } catch (e: JsonSyntaxException) {
            crashReportManager.logException(MalformedConsentTextException("Malformed Parental Consent Text Error", e))
            ParentalConsent()
        }
        return parentalConsent.assembleText(activity, preferencesManager.calloutAction, preferencesManager.programName, preferencesManager.organizationName)
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
        sessionEventsManager.updateSessionInBackground {
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
        }
    }

    override fun handleOnDestroy() {
        scannerManager.disconnectScannerIfNeeded()
    }

    override fun tryAgainFromErrorScreen() {
        startSetup()
    }

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
        launchOutOfFocus = false
    }

    override fun handleOnPause() {
        launchOutOfFocus = true
    }

    private fun addBluetoothConnectivityEvent() {
        sessionEventsManager.addEventForScannerConnectivityInBackground(
            ScannerConnectionEvent.ScannerInfo(
                preferencesManager.scannerId,
                preferencesManager.macAddress,
                preferencesManager.hardwareVersionString))
    }

    private fun initOrUpdateAnalyticsKeys() {
        crashReportManager.setProjectIdCrashlyticsKey(loginInfoManager.getSignedInProjectIdOrEmpty())
        crashReportManager.setUserIdCrashlyticsKey(loginInfoManager.getSignedInUserIdOrEmpty())
        crashReportManager.setModuleIdsCrashlyticsKey(preferencesManager.selectedModules)
        crashReportManager.setDownSyncTriggersCrashlyticsKey(preferencesManager.peopleDownSyncTriggers)
        crashReportManager.setFingersSelectedCrashlyticsKey(preferencesManager.fingerStatus)
        sessionEventsManager.getCurrentSession().subscribeBy {
            it -> crashReportManager.setSessionIdCrashlyticsKey(it.id)
        }
    }
}
