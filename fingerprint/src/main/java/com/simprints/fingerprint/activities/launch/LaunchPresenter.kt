package com.simprints.fingerprint.activities.launch

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import com.google.android.gms.location.LocationRequest
import com.google.gson.JsonSyntaxException
import com.simprints.core.tools.json.JsonHelper
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.alert.FingerprintAlert
import com.simprints.fingerprint.activities.launch.request.LaunchTaskRequest
import com.simprints.fingerprint.controllers.consentdata.ConsentDataManager
import com.simprints.fingerprint.controllers.core.analytics.FingerprintAnalyticsManager
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportManager
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportTag
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportTrigger
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.eventData.model.CandidateReadEvent
import com.simprints.fingerprint.controllers.core.eventData.model.ConsentEvent
import com.simprints.fingerprint.controllers.core.eventData.model.ConsentEvent.Result.*
import com.simprints.fingerprint.controllers.core.eventData.model.ConsentEvent.Type.INDIVIDUAL
import com.simprints.fingerprint.controllers.core.eventData.model.ConsentEvent.Type.PARENTAL
import com.simprints.fingerprint.controllers.core.eventData.model.ScannerConnectionEvent
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.controllers.core.repository.FingerprintDbManager
import com.simprints.fingerprint.controllers.core.repository.models.PersonFetchResult
import com.simprints.fingerprint.controllers.core.simnetworkutils.FingerprintSimNetworkUtils
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.controllers.locationprovider.LocationProvider
import com.simprints.fingerprint.controllers.scanner.ScannerManager
import com.simprints.fingerprint.data.domain.consent.GeneralConsent
import com.simprints.fingerprint.data.domain.consent.ParentalConsent
import com.simprints.fingerprint.di.FingerprintComponent
import com.simprints.fingerprint.exceptions.unexpected.domain.MalformedConsentTextException
import com.simprints.fingerprint.tools.extensions.getUcVersionString
import com.simprints.fingerprintscanner.ButtonListener
import com.simprints.fingerprintscanner.ScannerUtils.convertAddressToSerial
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
                      private val launchRequest: LaunchTaskRequest) : LaunchContract.Presenter {

    private var setupFlow: Disposable? = null

    @Inject lateinit var dbManager: FingerprintDbManager
    @Inject lateinit var simNetworkUtils: FingerprintSimNetworkUtils
    @Inject lateinit var consentDataManager: ConsentDataManager
    @Inject lateinit var crashReportManager: FingerprintCrashReportManager
    @Inject lateinit var scannerManager: ScannerManager
    @Inject lateinit var timeHelper: FingerprintTimeHelper
    @Inject lateinit var sessionEventsManager: FingerprintSessionEventsManager
    @Inject lateinit var locationProvider: LocationProvider
    @Inject lateinit var preferencesManager: FingerprintPreferencesManager
    @Inject lateinit var analyticsManager: FingerprintAnalyticsManager

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
        view.setLanguage(launchRequest.language)
        view.setLogoVisibility(launchRequest.logoExists)
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
            .andThen(disconnectVero())
            .andThen(checkIfBluetoothIsEnabled())
            .andThen(initVero())
            .andThen(connectToVero())
            .andThen(resetVeroUI())
            .andThen(wakeUpVero())
            .subscribeBy(onError = { it.printStackTrace() }, onComplete = {
                handleSetupFinished()
            })

    private fun disconnectVero() =
        veroTask(30, R.string.launch_bt_connect, scannerManager.disconnectVero()).doOnComplete {
            logMessageForCrashReport("ScannerManager: disconnect")
        }

    private fun checkIfBluetoothIsEnabled() =
        veroTask(37, R.string.launch_bt_connect, scannerManager.checkBluetoothStatus()).doOnComplete {
            logMessageForCrashReport("ScannerManager: bluetooth is enabled")
        }

    private fun initVero() =
        veroTask(45, R.string.launch_bt_connect, scannerManager.initVero()).doOnComplete {
            logMessageForCrashReport("ScannerManager: init vero")
        }

    private fun connectToVero() =
        veroTask(60, R.string.launch_bt_connect, scannerManager.connectToVero()) { addBluetoothConnectivityEvent() }.doOnComplete {
            logMessageForCrashReport("ScannerManager: connectToVero")
        }

    private fun resetVeroUI() =
        veroTask(75, R.string.launch_setup, scannerManager.resetVeroUI()).doOnComplete {
            logMessageForCrashReport("ScannerManager: resetVeroUI")
        }

    private fun wakeUpVero() =
        veroTask(90, R.string.launch_wake_un20, scannerManager.wakeUpVero()) { updateBluetoothConnectivityEventWithVeroInfo() }.doOnComplete {
            logMessageForCrashReport("ScannerManager: wakeUpVero")
        }

    private fun updateBluetoothConnectivityEventWithVeroInfo() {
        scannerManager.scanner?.let {
            sessionEventsManager.updateHardwareVersionInScannerConnectivityEvent(it.getUcVersionString())
        }
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
        return if (launchRequest.verifyGuid != null) {
            val guid = launchRequest.verifyGuid
            val startCandidateSearchTime = timeHelper.now()
            dbManager
                .loadPerson(launchRequest.projectId, guid)
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
        launchScannerAlertOrShowDialog(scannerManager.getAlertType(it))
        crashReportManager.logExceptionOrSafeException(it)
    }

    private fun launchScannerAlertOrShowDialog(alert: FingerprintAlert) {
        if (alert == FingerprintAlert.DISCONNECTED) {
            view.showDialogForScannerErrorConfirmation(scannerManager.lastPairedScannerId ?: "")
        } else {
            launchAlert(alert)
        }
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
        scannerManager.scanner?.let {
            preferencesManager.lastScannerUsed = convertAddressToSerial(it.macAddress)
            preferencesManager.lastScannerVersion = it.hardwareVersion.toString()
            analyticsManager.logScannerProperties(it.macAddress ?: "", it.scannerId ?: "")
        }
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
        return generalConsent.assembleText(activity, launchRequest, launchRequest.programName, launchRequest.organizationName)
    }

    private fun getParentalConsentText(): String {
        val parentalConsent = try {
            JsonHelper.gson.fromJson(consentDataManager.parentalConsentOptionsJson, ParentalConsent::class.java)
        } catch (e: JsonSyntaxException) {
            crashReportManager.logExceptionOrSafeException(MalformedConsentTextException("Malformed Parental Consent Text Error", e))
            ParentalConsent()
        }
        return parentalConsent.assembleText(activity, launchRequest, launchRequest.programName, launchRequest.organizationName)
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
                startConsentEventTime,
                timeHelper.now(),
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

    override fun tryAgainFromErrorOrRefusal() {
        setupFlow?.dispose()
        view.dismissScannerErrorConfirmationDialog()
        startSetup()
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

    override fun handleScannerDisconnectedYesClick() {
        launchAlert(FingerprintAlert.DISCONNECTED)
    }

    override fun handleScannerDisconnectedNoClick() {
        launchAlert(FingerprintAlert.NOT_PAIRED)
    }

    private fun addBluetoothConnectivityEvent() {
        scannerManager.scanner?.let {
            sessionEventsManager.addEventInBackground(
                ScannerConnectionEvent(
                    timeHelper.now(),
                    ScannerConnectionEvent.ScannerInfo(
                        it.scannerId ?: "",
                        it.macAddress,
                        it.getUcVersionString())))
        }
    }

    private fun logMessageForCrashReport(message: String) {
        crashReportManager.logMessageForCrashReport(
            FingerprintCrashReportTag.SCANNER_SETUP,
            FingerprintCrashReportTrigger.SCANNER, message = message)
    }
}
