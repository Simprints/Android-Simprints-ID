package com.simprints.fingerprint.activities.launch

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import com.google.android.gms.location.LocationRequest
import com.google.gson.JsonSyntaxException
import com.simprints.core.tools.json.JsonHelper
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.alert.FingerprintAlert
import com.simprints.fingerprint.activities.alert.response.AlertActResult
import com.simprints.fingerprint.controllers.consentdata.ConsentDataManager
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportManager
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.eventData.model.CandidateReadEvent
import com.simprints.fingerprint.controllers.core.eventData.model.ConsentEvent
import com.simprints.fingerprint.controllers.core.eventData.model.ConsentEvent.Result.*
import com.simprints.fingerprint.controllers.core.eventData.model.ConsentEvent.Type.INDIVIDUAL
import com.simprints.fingerprint.controllers.core.eventData.model.ConsentEvent.Type.PARENTAL
import com.simprints.fingerprint.controllers.core.eventData.model.ScannerConnectionEvent
import com.simprints.fingerprint.controllers.core.repository.FingerprintDbManager
import com.simprints.fingerprint.controllers.core.repository.models.PersonFetchResult
import com.simprints.fingerprint.controllers.core.simnetworkutils.FingerprintSimNetworkUtils
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.controllers.locationprovider.LocationProvider
import com.simprints.fingerprint.controllers.scanner.ScannerManager
import com.simprints.fingerprint.data.domain.collect.CollectFingerprintsActResult
import com.simprints.fingerprint.data.domain.consent.GeneralConsent
import com.simprints.fingerprint.data.domain.consent.ParentalConsent
import com.simprints.fingerprint.data.domain.matching.result.MatchingActIdentifyResult
import com.simprints.fingerprint.data.domain.matching.result.MatchingActResult
import com.simprints.fingerprint.data.domain.matching.result.MatchingActVerifyResult
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.DomainToFingerprintResponse.fromDomainToFingerprintEnrolResponse
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.DomainToFingerprintResponse.fromDomainToFingerprintErrorResponse
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.DomainToFingerprintResponse.fromDomainToFingerprintIdentifyResponse
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.DomainToFingerprintResponse.fromDomainToFingerprintRefusalFormResponse
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.DomainToFingerprintResponse.fromDomainToFingerprintVerifyResponse
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintEnrolRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintIdentifyRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintVerifyRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses.*
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.responses.FingerprintErrorReason.Companion.fromFingerprintAlertToErrorResponse
import com.simprints.fingerprint.data.domain.refusal.RefusalActResult
import com.simprints.fingerprint.di.FingerprintComponent
import com.simprints.fingerprint.exceptions.unexpected.MalformedConsentTextException
import com.simprints.fingerprintscanner.ButtonListener
import com.simprints.moduleapi.fingerprint.responses.IFingerprintResponse
import com.tbruyelle.rxpermissions2.Permission
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class LaunchPresenter(component: FingerprintComponent,
                      private val view: LaunchContract.View,
                      private val fingerprintRequest: FingerprintRequest) : LaunchContract.Presenter {

    private var setupFlow: Disposable? = null

    @Inject lateinit var dbManager: FingerprintDbManager
    @Inject lateinit var simNetworkUtils: FingerprintSimNetworkUtils
    @Inject lateinit var consentDataManager: ConsentDataManager
    @Inject lateinit var crashReportManager: FingerprintCrashReportManager
    @Inject lateinit var scannerManager: ScannerManager
    @Inject lateinit var timeHelper: FingerprintTimeHelper
    @Inject lateinit var sessionEventsManager: FingerprintSessionEventsManager
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

        setTextToConsentTabs()

        setupFlow?.dispose()
        setupFlow = startSetup()
    }

    @SuppressLint("CheckResult")
    private fun startSetup() =
        requestPermissionsForLocation(5)
            .andThen(checkIfVerifyAndGuidExists(15))
            .andThen(veroTask(30, R.string.launch_bt_connect, scannerManager.disconnectVero()))
            .andThen(veroTask(45, R.string.launch_bt_connect, scannerManager.initVero()))
            .andThen(veroTask(60, R.string.launch_bt_connect, scannerManager.connectToVero()) { addBluetoothConnectivityEvent() })
            .andThen(veroTask(75, R.string.launch_setup, scannerManager.resetVeroUI()))
            .andThen(veroTask(90, R.string.launch_wake_un20, scannerManager.wakeUpVero()) { updateBluetoothConnectivityEventWithVeroInfo() })
            .subscribeBy(onError = { it.printStackTrace() }, onComplete = { handleSetupFinished() })


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
            dbManager
                .loadPerson(fingerprintRequest.projectId, guid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess { personFetchResult ->
                    handleGuidFound(personFetchResult, guid, startCandidateSearchTime)
                }.doOnError {
                    it.printStackTrace()
                    // For any reason, we show the missing guidFound screen.
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
            if (isPersonFromLocalDb) CandidateReadEvent.LocalResult.FOUND else CandidateReadEvent.LocalResult.NOT_FOUND,
            if (isPersonFromLocalDb) null else CandidateReadEvent.RemoteResult.FOUND)
    }

    private fun saveNotFoundVerificationAndShowAlert(guid: String, startCandidateSearchTime: Long) {
        if (simNetworkUtils.isConnected()) {
            // We've synced with the online dbManager and they're not in the dbManager
            launchAlert(FingerprintAlert.GUID_NOT_FOUND_ONLINE)
            saveEventForCandidateReadInBackgroundNotFound(guid, startCandidateSearchTime, CandidateReadEvent.LocalResult.NOT_FOUND, CandidateReadEvent.RemoteResult.NOT_FOUND)
        } else {
            // We're offline but might find the person if we sync
            launchAlert(FingerprintAlert.GUID_NOT_FOUND_OFFLINE)
            saveEventForCandidateReadInBackgroundNotFound(guid, startCandidateSearchTime, CandidateReadEvent.LocalResult.NOT_FOUND, null)
        }
    }

    private fun saveEventForCandidateReadInBackgroundNotFound(guid: String,
                                                              startCandidateSearchTime: Long,
                                                              localResult: CandidateReadEvent.LocalResult,
                                                              remoteResult: CandidateReadEvent.RemoteResult?) {
        sessionEventsManager.addEventInBackground(
            CandidateReadEvent(
                startCandidateSearchTime,
                timeHelper.now(),
                guid,
                localResult,
                remoteResult))
    }

    private fun manageVeroErrors(it: Throwable) {
        it.printStackTrace()
        launchAlert(scannerManager.getAlertType(it))
        crashReportManager.logExceptionOrSafeException(it)
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
                    sessionEventsManager.addLocationToSessionInBackground(it.latitude, it.longitude)
                }, onError = { it.printStackTrace() })
        }
    }

    private fun handleSetupFinished() {
        view.handleSetupFinished()
        scannerManager.scanner?.registerButtonListener(scannerButton)
        view.doVibrate()
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
            crashReportManager.logExceptionOrSafeException(MalformedConsentTextException("Malformed General Consent Text Error", e))
            GeneralConsent()
        }
        return generalConsent.assembleText(activity, fingerprintRequest, fingerprintRequest.programName, fingerprintRequest.organizationName)
    }

    private fun getParentalConsentText(): String {
        val parentalConsent = try {
            JsonHelper.gson.fromJson(consentDataManager.parentalConsentOptionsJson, ParentalConsent::class.java)
        } catch (e: JsonSyntaxException) {
            crashReportManager.logExceptionOrSafeException(MalformedConsentTextException("Malformed Parental Consent Text Error", e))
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
        sessionEventsManager.addEventInBackground(
            ConsentEvent(
                timeHelper.now(),
                startConsentEventTime,
                if (view.isCurrentTabParental()) {
                    PARENTAL
                } else {
                    INDIVIDUAL
                },
                result))
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
            val possibleCollectResult = resultData?.getParcelableExtra<CollectFingerprintsActResult>(CollectFingerprintsActResult.BUNDLE_KEY)
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

    private fun prepareErrorResponse(resultData: Intent, alertActResult: AlertActResult) {
        val fingerprintErrorResponse = fromFingerprintAlertToErrorResponse(alertActResult.alert)
        resultData.putExtra(IFingerprintResponse.BUNDLE_KEY,
            fromDomainToFingerprintErrorResponse(fingerprintErrorResponse))
    }

    private fun prepareRefusalForm(resultData: Intent, possibleRefusalForm: RefusalActResult) {
        val fingerprintResult = FingerprintRefusalFormResponse(
            possibleRefusalForm.answer?.reason.toString(),
            possibleRefusalForm.answer?.optionalText.toString())

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

    private fun prepareEnrolResponseIntent(resultData: Intent?, possibleCollectFingerprintsActResult: CollectFingerprintsActResult?) {
        possibleCollectFingerprintsActResult?.let {
            val fingerprintResult = FingerprintEnrolResponse(it.probe.patientId)
            resultData?.putExtra(IFingerprintResponse.BUNDLE_KEY, fromDomainToFingerprintEnrolResponse(fingerprintResult))
        }
    }

    override fun confirmConsentAndContinueToNextActivity() {
        addConsentEvent(ACCEPTED)
        waitingForConfirmation = false
        view.continueToNextActivity()
    }

    private fun launchAlert(alert: FingerprintAlert) {
        if (!launchOutOfFocus) {
            view.doLaunchAlert(alert)
        }
    }

    override fun onActivityResult() {
        launchOutOfFocus = false
    }

    override fun handleOnResume() {
        launchOutOfFocus = false
    }

    override fun handleOnPause() {
        launchOutOfFocus = true
    }

    private fun addBluetoothConnectivityEvent() {
        sessionEventsManager.addEventInBackground(
            ScannerConnectionEvent(
                timeHelper.now(),
                ScannerConnectionEvent.ScannerInfo(
                    scannerManager.scannerId ?: "",
                    scannerManager.macAddress ?: "",
                    scannerManager.hardwareVersion ?: "")))
    }
}
