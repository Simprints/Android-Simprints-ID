package com.simprints.fingerprint.activities.launch

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import com.google.android.gms.location.LocationRequest
import com.google.gson.JsonSyntaxException
import com.simprints.core.tools.json.JsonHelper
import com.simprints.fingerprint.R
import com.simprints.fingerprint.data.domain.alert.FingerprintAlert
import com.simprints.fingerprint.data.domain.collect.CollectResult
import com.simprints.fingerprint.data.domain.consent.GeneralConsent
import com.simprints.fingerprint.data.domain.consent.ParentalConsent
import com.simprints.fingerprint.data.domain.matching.result.MatchingActIdentifyResult
import com.simprints.fingerprint.data.domain.matching.result.MatchingActResult
import com.simprints.fingerprint.data.domain.matching.result.MatchingActVerifyResult
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.DomainToFingerprintResponse.fromDomainToFingerprintEnrolResponse
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.DomainToFingerprintResponse.fromDomainToFingerprintIdentifyResponse
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.DomainToFingerprintResponse.fromDomainToFingerprintRefusalFormResponse
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.DomainToFingerprintResponse.fromDomainToFingerprintVerifyResponse
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintEnrolRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintIdentifyRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintVerifyRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses.FingerprintEnrolResponse
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses.FingerprintIdentifyResponse
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses.FingerprintRefusalFormResponse
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses.FingerprintVerifyResponse
import com.simprints.fingerprint.data.domain.refusal.RefusalActResult
import com.simprints.fingerprint.di.FingerprintsComponent
import com.simprints.fingerprint.exceptions.unexpected.MalformedConsentTextException
import com.simprints.fingerprint.scanner.ScannerManager
import com.simprints.fingerprint.tools.utils.LocationProvider
import com.simprints.fingerprint.tools.utils.TimeHelper
import com.simprints.fingerprintscanner.ButtonListener
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.models.domain.events.CandidateReadEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.ConsentEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.ConsentEvent.Result.*
import com.simprints.id.data.analytics.eventdata.models.domain.events.ConsentEvent.Type.INDIVIDUAL
import com.simprints.id.data.analytics.eventdata.models.domain.events.ConsentEvent.Type.PARENTAL
import com.simprints.id.data.analytics.eventdata.models.domain.events.ScannerConnectionEvent
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.PersonFetchResult
import com.simprints.id.services.scheduledSync.SyncSchedulerHelper
import com.simprints.id.tools.utils.SimNetworkUtils
import com.simprints.moduleapi.fingerprint.responses.IFingerprintResponse
import com.tbruyelle.rxpermissions2.Permission
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class LaunchPresenter(component: FingerprintsComponent,
                      private val view: LaunchContract.View,
                      private val fingerprintRequest: FingerprintRequest) : LaunchContract.Presenter {

    @Inject lateinit var dbManager: DbManager
    @Inject lateinit var simNetworkUtils: SimNetworkUtils
    @Inject lateinit var consentDataManager: ConsentDataManager
    @Inject lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var scannerManager: ScannerManager
    @Inject lateinit var timeHelper: TimeHelper
    @Inject lateinit var sessionEventsManager: SessionEventsManager
    @Inject lateinit var syncSchedulerHelper: SyncSchedulerHelper
    @Inject lateinit var locationProvider: LocationProvider

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
        component.inject(this)
        startConsentEventTime = timeHelper.now()
    }

    override fun start() {
        view.setLanguage(fingerprintRequest.language)
        view.setLogoVisibility(fingerprintRequest.logoExists)
        view.initTextsInButtons()
        view.initConsentTabs()

        syncSchedulerHelper.scheduleBackgroundSyncs()
        syncSchedulerHelper.startDownSyncOnLaunchIfPossible()

        setTextToConsentTabs()

        startSetup()
    }

    @SuppressLint("CheckResult")
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
        sessionEventsManager.updateHardwareVersionInScannerConnectivityEvent(scannerManager.hardwareVersion
            ?: "")
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
        return if (fingerprintRequest is FingerprintVerifyRequest) {
            val guid = fingerprintRequest.verifyGuid
            val startCandidateSearchTime = timeHelper.now()
            dbManager.loadPerson(fingerprintRequest.projectId, guid).doOnSuccess { personFetchResult ->
                handleGuidFound(personFetchResult, guid, startCandidateSearchTime)
            }.doOnError {
                it.printStackTrace()
                // For any error, we show the missing guidFound screen.
                saveNotFoundVerificationAndShowAlert(guid, startCandidateSearchTime)
            }.ignoreElement()
        } else {
            Completable.complete()
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

    private fun saveNotFoundVerificationAndShowAlert(guid: String, startCandidateSearchTime: Long) {
        if (simNetworkUtils.isConnected()) {
            // We've synced with the online dbManager and they're not in the dbManager
            view.doLaunchAlert(FingerprintAlert.GUID_NOT_FOUND_ONLINE)
            saveEventForCandidateReadInBackgroundNotFound(guid, startCandidateSearchTime, CandidateReadEvent.LocalResult.NOT_FOUND, CandidateReadEvent.RemoteResult.NOT_FOUND)
        } else {
            // We're offline but might find the person if we sync
            view.doLaunchAlert(FingerprintAlert.GUID_NOT_FOUND_OFFLINE)
            saveEventForCandidateReadInBackgroundNotFound(guid, startCandidateSearchTime, CandidateReadEvent.LocalResult.NOT_FOUND, null)
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
        crashReportManager.logExceptionOrThrowable(it)
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

    @SuppressLint("MissingPermission", "CheckResult")
    private fun collectLocationIfPermitted(permissions: List<Permission>) {
        if (!permissionsAlreadyRequested &&
            permissions.first { it.name == Manifest.permission.ACCESS_FINE_LOCATION }.granted) {
            val req = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            locationProvider
                .getUpdatedLocation(req)
                .firstOrError()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeBy(onSuccess = {
                    sessionEventsManager.addLocationToSession(it.latitude, it.longitude)
                }, onError = { it.printStackTrace() })
        }
    }

    private fun handleSetupFinished() {
        view.handleSetupFinished()
        scannerManager.scanner?.registerButtonListener(scannerButton)
        view.doVibrateIfNecessary(fingerprintRequest.vibrateMode)
    }

    private fun setTextToConsentTabs() {
        view.setTextToGeneralConsent(getGeneralConsentText())
        if (consentDataManager.parentalConsentExists) {
            view.addParentalConsentTabWithText(getParentalConsentText())
        }
    }

    private fun getGeneralConsentText(): String {
        val generalConsent = try {
            JsonHelper.gson.fromJson(consentDataManager.generalConsentOptionsJson, GeneralConsent::class.java)
        } catch (e: JsonSyntaxException) {
            crashReportManager.logExceptionOrThrowable(MalformedConsentTextException("Malformed General Consent Text Error", e))
            GeneralConsent()
        }
        return generalConsent.assembleText(activity, fingerprintRequest, fingerprintRequest.programName, fingerprintRequest.organizationName)
    }

    private fun getParentalConsentText(): String {
        val parentalConsent = try {
            JsonHelper.gson.fromJson(consentDataManager.parentalConsentOptionsJson, ParentalConsent::class.java)
        } catch (e: JsonSyntaxException) {
            crashReportManager.logExceptionOrThrowable(MalformedConsentTextException("Malformed Parental Consent Text Error", e))
            ParentalConsent()
        }
        return parentalConsent.assembleText(activity, fingerprintRequest, fingerprintRequest.programName, fingerprintRequest.organizationName)
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
            it.addEvent(
                ConsentEvent(
                    it.timeRelativeToStartTime(startConsentEventTime),
                    it.timeRelativeToStartTime(timeHelper.now()),
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
        val returnIntent = Intent()

        if (resultCode == Activity.RESULT_OK) {
            val possibleMatchResult = resultData?.getParcelableExtra<MatchingActResult>(MatchingActResult.BUNDLE_KEY)
            val possibleCollectResult = resultData?.getParcelableExtra<CollectResult>(CollectResult.BUNDLE_KEY)
            val possibleRefusalForm = resultData?.getParcelableExtra<RefusalActResult>(RefusalActResult.BUNDLE_KEY)

            if (possibleRefusalForm != null) {
                prepareRefusalForm(returnIntent, possibleRefusalForm)
            } else {
                when (fingerprintRequest) {
                    is FingerprintEnrolRequest -> prepareEnrolResponseIntent(returnIntent, possibleCollectResult)
                    is FingerprintIdentifyRequest -> prepareIdentifyResponseIntent(returnIntent, possibleMatchResult as MatchingActIdentifyResult?)
                    is FingerprintVerifyRequest -> prepareVerifyResponseIntent(returnIntent, possibleMatchResult as MatchingActVerifyResult?)
                }
            }
        }

        view.setResultAndFinish(resultCode, returnIntent)
    }

    private fun prepareRefusalForm(resultData: Intent, possibleRefusalForm: RefusalActResult) {
        val fingerprintResult = FingerprintRefusalFormResponse(possibleRefusalForm.reason.toString(), possibleRefusalForm.optionalText)
        resultData.putExtra(IFingerprintResponse.BUNDLE_KEY,
            fromDomainToFingerprintRefusalFormResponse(fingerprintResult))
    }

    private fun prepareVerifyResponseIntent(resultData: Intent?, possibleMatchResult: MatchingActVerifyResult?) {
        possibleMatchResult?.let {
            val fingerprintResult = FingerprintVerifyResponse(it.guid, it.confidence, it.tier)
            resultData?.putExtra(IFingerprintResponse.BUNDLE_KEY,
                fromDomainToFingerprintVerifyResponse(fingerprintResult))
        }
    }

    private fun prepareIdentifyResponseIntent(resultData: Intent?, possibleMatchResult: MatchingActIdentifyResult?) {
        possibleMatchResult?.let {
            val fingerprintResult = FingerprintIdentifyResponse(it.identifications)
            resultData?.putExtra(IFingerprintResponse.BUNDLE_KEY,
                fromDomainToFingerprintIdentifyResponse(fingerprintResult))
        }
    }

    private fun prepareEnrolResponseIntent(resultData: Intent?, possibleCollectResult: CollectResult?) {
        possibleCollectResult?.let {
            val fingerprintResult = FingerprintEnrolResponse(it.probe.patientId)
            resultData?.putExtra(IFingerprintResponse.BUNDLE_KEY, fromDomainToFingerprintEnrolResponse(fingerprintResult))
        }
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
                scannerManager.scannerId ?: "",
                scannerManager.macAddress ?: "",
                scannerManager.hardwareVersion ?: ""))
    }
}
