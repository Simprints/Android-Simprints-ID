package com.simprints.fingerprint.activities.collect

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.simprints.fingerprint.activities.collect.confirmFingerprints.ConfirmFingerprintsDialog
import com.simprints.fingerprint.activities.collect.fingers.CollectFingerprintsFingerDisplayHelper
import com.simprints.fingerprint.activities.collect.indicators.CollectFingerprintsIndicatorsHelper
import com.simprints.fingerprint.activities.collect.models.Finger
import com.simprints.fingerprint.activities.collect.scanning.CollectFingerprintsScanningHelper
import com.simprints.fingerprint.data.domain.requests.FingerprintEnrolRequest
import com.simprints.fingerprint.data.domain.requests.FingerprintIdentifyRequest
import com.simprints.fingerprint.data.domain.requests.FingerprintRequest
import com.simprints.fingerprint.data.domain.requests.FingerprintVerifyRequest
import com.simprints.fingerprint.di.FingerprintsComponent
import com.simprints.fingerprint.tools.extensions.toResultEvent
import com.simprints.id.R
import com.simprints.id.activities.IntentKeys
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.models.domain.events.FingerprintCaptureEvent
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.alert.Alert
import com.simprints.id.domain.fingerprint.Fingerprint
import com.simprints.id.domain.fingerprint.Person
import com.simprints.id.domain.responses.EnrolResponse
import com.simprints.id.domain.responses.Response
import com.simprints.id.exceptions.SimprintsException
import com.simprints.id.exceptions.safe.callout.InvalidCalloutParameterError
import com.simprints.id.exceptions.unexpected.UnexpectedException
import com.simprints.id.tools.LanguageHelper
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.utils.EncodingUtils
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.math.min

class CollectFingerprintsPresenter(private val context: Context,
                                   private val view: CollectFingerprintsContract.View,
                                   private val fingerprintRequest: FingerprintRequest,
                                   private val component: FingerprintsComponent)
    : CollectFingerprintsContract.Presenter {

    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var dbManager: DbManager
    @Inject lateinit var timeHelper: TimeHelper
    @Inject lateinit var sessionEventsManager: SessionEventsManager

    private lateinit var scanningHelper: CollectFingerprintsScanningHelper
    private lateinit var fingerDisplayHelper: CollectFingerprintsFingerDisplayHelper
    private lateinit var indicatorsHelper: CollectFingerprintsIndicatorsHelper

    // Array with only the active Fingers, used to populate the ViewPager
    override val activeFingers = ArrayList<Finger>()
    override var currentActiveFingerNo: Int = 0
    override var isConfirmDialogShown = false
    override var isTryDifferentFingerSplashShown = false
    override var isNudging = false
    private var lastCaptureStartedAt: Long = 0
    private var confirmDialog: AlertDialog? = null

    init {
        component.inject(this)
    }

    override fun start() {
        LanguageHelper.setLanguage(context, preferencesManager.language)

        initFingerDisplayHelper(view)
        initIndicatorsHelper(context, view)
        initScanningHelper(context, view)
        initScanButtonListeners()
        refreshDisplay()
    }

    private fun initFingerDisplayHelper(view: CollectFingerprintsContract.View) {
        fingerDisplayHelper = CollectFingerprintsFingerDisplayHelper(view, this, preferencesManager.fingerStatus, preferencesManager.nudgeMode)
    }

    private fun initIndicatorsHelper(context: Context, view: CollectFingerprintsContract.View) {
        indicatorsHelper = CollectFingerprintsIndicatorsHelper(context, view, this)
    }

    private fun initScanningHelper(context: Context, view: CollectFingerprintsContract.View) {
        scanningHelper = CollectFingerprintsScanningHelper(context, view, this, component)
    }

    private fun initScanButtonListeners() {
        view.scanButton.setOnClickListener {
            logMessageForCrashReport("Scan button clicked")
            startCapturing()
        }
        view.scanButton.setOnLongClickListener {
            logMessageForCrashReport("Scan button long clicked")
            resetFingerState()
        }
    }

    private fun resetFingerState(): Boolean {
        if (!isScanning()) {
            currentFinger().isNotCollected
            currentFinger().template = null
            refreshDisplay()
        }
        return true
    }

    override fun initIndicators() {
        indicatorsHelper.initIndicators()
    }

    override fun currentFinger(): Finger = activeFingers[currentActiveFingerNo]

    override fun isScanning(): Boolean = currentFinger().isCollecting

    override fun viewPagerOnPageSelected(position: Int) {
        currentActiveFingerNo = position
        refreshDisplay()
        scanningHelper.resetScannerUi()
    }

    override fun handleBackPressedWhileScanning() {
        stopCapturing()
    }

    override fun handleScannerButtonPressed() {
        startCapturing()
    }

    private fun stopCapturing() {
        scanningHelper.toggleContinuousCapture()
    }

    private fun startCapturing() {
        lastCaptureStartedAt = timeHelper.now()
        scanningHelper.toggleContinuousCapture()
    }

    override fun handleCaptureSuccess() {
        addCaptureEventInSession(currentFinger())
        if (fingerHasSatisfiedTerminalCondition(currentFinger())) {
            resolveFingerTerminalConditionTriggered()
        }
    }

    override fun resolveFingerTerminalConditionTriggered() {
        if (isScanningEndStateAchieved()) {
            createMapAndShowDialog()
        } else if (currentFinger().isGoodScan || currentFinger().isRescanGoodScan) {
            fingerDisplayHelper.doNudgeIfNecessary()
        } else {
            if (haveNotExceedMaximumNumberOfFingersToAutoAdd()) {
                fingerDisplayHelper.showSplashAndNudgeAndAddNewFinger()
            } else if (!currentFinger().isLastFinger) {
                fingerDisplayHelper.showSplashAndNudgeIfNecessary()
            }
        }
    }

    private fun isScanningEndStateAchieved(): Boolean {
        if (everyActiveFingerHasSatisfiedTerminalCondition()) {
            if (weHaveTheMinimumNumberOfAnyQualityScans() || weHaveTheMinimumNumberOfGoodScans()) {
                return true
            }
        }
        return false
    }

    private fun everyActiveFingerHasSatisfiedTerminalCondition(): Boolean =
        activeFingers.all { fingerHasSatisfiedTerminalCondition(it) }

    private fun tooManyBadScans(finger: Finger) =
        finger.numberOfBadScans >= numberOfBadScansRequiredToAutoAddNewFinger

    private fun haveNotExceedMaximumNumberOfFingersToAutoAdd() =
        activeFingers.size < maximumTotalNumberOfFingersForAutoAdding

    private fun weHaveTheMinimumNumberOfGoodScans(): Boolean =
        activeFingers.filter { it.isGoodScan || it.isRescanGoodScan }.size >= min(targetNumberOfGoodScans, numberOfOriginalFingers())

    private fun weHaveTheMinimumNumberOfAnyQualityScans() =
        activeFingers.filter { fingerHasSatisfiedTerminalCondition(it) }.size >= maximumTotalNumberOfFingersForAutoAdding

    private fun numberOfOriginalFingers() =
        preferencesManager.fingerStatus.filter { it.value }.size

    override fun fingerHasSatisfiedTerminalCondition(finger: Finger) =
        ((tooManyBadScans(finger) || finger.isGoodScan || finger.isRescanGoodScan) && finger.template != null) || finger.isFingerSkipped

    override fun getTitle(): String =
        when (fingerprintRequest) {
            is FingerprintEnrolRequest -> context.getString(R.string.register_title)
            is FingerprintIdentifyRequest -> context.getString(R.string.identify_title)
            is FingerprintVerifyRequest -> context.getString(R.string.verify_title)
            else -> {
                handleException(InvalidCalloutParameterError.forParameter("CalloutParameters"))
                ""
            }
        }

    override fun refreshDisplay() {
        indicatorsHelper.refreshIndicators()
        view.refreshScanButtonAndTimeoutBar()
        view.refreshFingerFragment()
    }

    override fun handleTryAgainFromDifferentActivity() {
        scanningHelper.reconnect()
    }

    override fun handleOnResume() {
        scanningHelper.startListeners()
    }

    override fun handleOnPause() {
        scanningHelper.stopListeners()
    }

    override fun handleOnBackPressedToLeave() {
        scanningHelper.stopReconnecting()
    }

    override fun handleConfirmFingerprintsAndContinue() {
        logMessageForCrashReport("Confirm fingerprints clicked")
        dismissConfirmDialogIfStillShowing()

        val fingers = activeFingers
            .filter { fingerHasSatisfiedTerminalCondition(it) && !it.isFingerSkipped && it.template != null }

        if (fingers.isEmpty()) {
            Toast.makeText(context, R.string.no_fingers_scanned, Toast.LENGTH_LONG).show()
            handleRestart()
        } else {
            proceedToFinish(fingers.mapNotNull { it.template })
        }
    }

    private fun dismissConfirmDialogIfStillShowing() {
        confirmDialog?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }
    }

    private fun proceedToFinish(fingerprints: List<Fingerprint>) {
        val person = Person(
            UUID.randomUUID().toString(),
            loginInfoManager.getSignedInProjectIdOrEmpty(),
            loginInfoManager.getSignedInUserIdOrEmpty(),
            fingerprintRequest.moduleId,
            fingerprints)
        sessionEventsManager.addPersonCreationEventInBackground(person)

        if (isRegisteringElseIsMatching()) {
            savePerson(person)
        } else {
            goToMatching(person)
        }
    }

    private fun isRegisteringElseIsMatching() = fingerprintRequest is FingerprintEnrolRequest

    private fun savePerson(person: Person) {
        dbManager.savePerson(person)
            .subscribeBy(
                onComplete = { handleSavePersonSuccess(person.patientId) },
                onError = { handleSavePersonFailure(it) })
    }

    //STOPSHIP
    //We shouldn't be using any client API models in ID past the interface layer.
    private fun handleSavePersonSuccess(guidCreated: String) {
        preferencesManager.lastEnrolDate = Date()
        val result = Intent()
        result.putExtra(Response.BUNDLE_KEY, EnrolResponse(guidCreated))
        view.finishSuccessEnrol(result)
    }

    private fun handleSavePersonFailure(throwable: Throwable) {
        handleException(UnexpectedException(throwable))
        view.cancelAndFinish()
    }

    private fun goToMatching(person: Person) {
        val fingerprintModule = "com.simprints.id" //STOPSHIP
        val matchingActivityClassName = "com.simprints.fingerprint.activities.matching.MatchingActivity"

        val intent = Intent().setClassName(fingerprintModule, matchingActivityClassName)
        intent.putExtra(IntentKeys.matchingActivityProbePersonKey, person)
        intent.putExtra(Request.BUNDLE_KEY, appRequest)
        view.finishSuccessAndStartMatching(intent)
    }

    override fun handleException(simprintsException: SimprintsException) {
        crashReportManager.logExceptionOrThrowable(simprintsException)
        Timber.e(simprintsException)
        view.doLaunchAlert(Alert.UNEXPECTED_ERROR)
    }

    private fun addCaptureEventInSession(finger: Finger) {
        sessionEventsManager.updateSessionInBackground { sessionEvents ->
            sessionEvents.addEvent(FingerprintCaptureEvent(
                sessionEvents.timeRelativeToStartTime(lastCaptureStartedAt),
                sessionEvents.nowRelativeToStartTime(timeHelper),
                finger.id,
                preferencesManager.qualityThreshold,
                finger.status.toResultEvent(),
                finger.template?.let {
                    FingerprintCaptureEvent.Fingerprint(it.qualityScore, EncodingUtils.byteArrayToBase64(it.templateBytes))
                }
            ))
        }
    }

    private fun createMapAndShowDialog() {
        isConfirmDialogShown = true
        confirmDialog = ConfirmFingerprintsDialog(context, createMapForScannedFingers(),
            callbackConfirm = { handleConfirmFingerprintsAndContinue() },
            callbackRestart = { handleRestart() })
            .create().also {
                it.show()
                logMessageForCrashReport("Confirm fingerprints dialog shown")
            }
    }

    private fun createMapForScannedFingers(): MutableMap<String, Boolean> =
        mutableMapOf<String, Boolean>().also { mapOfScannedFingers ->
            activeFingers.forEach {
                mapOfScannedFingers[context.getString(com.simprints.fingerprint.activities.collect.models.FingerRes.get(it).nameId)] = it.isGoodScan || it.isRescanGoodScan
            }
        }

    private fun handleRestart() {
        logMessageForCrashReport("Restart clicked")
        fingerDisplayHelper.clearAndPopulateFingerArrays()
        fingerDisplayHelper.handleFingersChanged()
        fingerDisplayHelper.resetFingerIndexToBeginning()
        isConfirmDialogShown = false
        confirmDialog = null
    }

    override fun handleMissingFingerClick() {
        logMessageForCrashReport("Missing finger text clicked")
        if (!currentFinger().isCollecting) {
            scanningHelper.setCurrentFingerAsSkippedAndAsNumberOfBadScansToAutoAddFinger()
            lastCaptureStartedAt = timeHelper.now()
            addCaptureEventInSession(currentFinger())
            resolveFingerTerminalConditionTriggered()
        }
    }

    private fun logMessageForCrashReport(message: String) {
        crashReportManager.logMessageForCrashReport(CrashReportTag.FINGER_CAPTURE, CrashReportTrigger.UI, message = message)
    }

    companion object {
        private const val targetNumberOfGoodScans = 2
        private const val maximumTotalNumberOfFingersForAutoAdding = 4
        const val numberOfBadScansRequiredToAutoAddNewFinger = 3
    }
}
