package com.simprints.id.activities.collectFingerprints

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.IntentKeys
import com.simprints.id.activities.collectFingerprints.confirmFingerprints.ConfirmFingerprintsDialog
import com.simprints.id.activities.collectFingerprints.fingers.CollectFingerprintsFingerDisplayHelper
import com.simprints.id.activities.collectFingerprints.indicators.CollectFingerprintsIndicatorsHelper
import com.simprints.id.activities.collectFingerprints.scanning.CollectFingerprintsScanningHelper
import com.simprints.id.activities.matching.MatchingActivity
import com.simprints.id.data.analytics.crashReport.CrashReportManager
import com.simprints.id.data.analytics.crashReport.CrashReportTags
import com.simprints.id.data.analytics.crashReport.CrashTrigger
import com.simprints.id.data.analytics.eventData.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventData.models.domain.events.FingerprintCaptureEvent
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.domain.Finger
import com.simprints.id.domain.FingerRes
import com.simprints.id.exceptions.safe.callout.InvalidCalloutParameterError
import com.simprints.id.exceptions.unexpected.UnexpectedException
import com.simprints.id.session.callout.CalloutAction
import com.simprints.id.tools.FormatResult
import com.simprints.id.tools.LanguageHelper
import com.simprints.id.tools.TimeHelper
import com.simprints.libcommon.Fingerprint
import com.simprints.libcommon.Person
import com.simprints.libcommon.Utils
import com.simprints.libsimprints.Constants
import com.simprints.libsimprints.Registration
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.math.min

class CollectFingerprintsPresenter(private val context: Context,
                                   private val view: CollectFingerprintsContract.View)
    : CollectFingerprintsContract.Presenter {

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
        ((view as Activity).application as Application).component.inject(this)
    }

    override fun start() {
        preferencesManager.msSinceBootOnMainStart = timeHelper.now()
        LanguageHelper.setLanguage(context, preferencesManager.language)

        initFingerDisplayHelper(view)
        initIndicatorsHelper(context, view)
        initScanningHelper(context, view)
        initScanButtonListeners()
        refreshDisplay()
    }

    private fun initFingerDisplayHelper(view: CollectFingerprintsContract.View) {
        fingerDisplayHelper = CollectFingerprintsFingerDisplayHelper(view, this)
    }

    private fun initIndicatorsHelper(context: Context, view: CollectFingerprintsContract.View) {
        indicatorsHelper = CollectFingerprintsIndicatorsHelper(context, view, this)
    }

    private fun initScanningHelper(context: Context, view: CollectFingerprintsContract.View) {
        scanningHelper = CollectFingerprintsScanningHelper(context, view, this)
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
        when (preferencesManager.calloutAction) {
            CalloutAction.REGISTER -> context.getString(R.string.register_title)
            CalloutAction.IDENTIFY -> context.getString(R.string.identify_title)
            CalloutAction.UPDATE -> context.getString(R.string.update_title)
            CalloutAction.VERIFY -> context.getString(R.string.verify_title)
            else -> {
                handleUnexpectedError(InvalidCalloutParameterError.forParameter("CalloutParameters"))
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

        val fingerprints = activeFingers
            .filter { fingerHasSatisfiedTerminalCondition(it) }
            .filter { !it.isFingerSkipped }
            .filter { it.template != null }
            .map { Fingerprint(it.id, it.template.templateBytes) }

        if (fingerprints.isEmpty()) {
            Toast.makeText(context, R.string.no_fingers_scanned, Toast.LENGTH_LONG).show()
            handleRestart()
        } else {
            proceedToFinish(fingerprints)
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
        val person = Person(preferencesManager.patientId, fingerprints)
        sessionEventsManager.addPersonCreationEventInBackground(person)

        if (isRegisteringElseIsMatching()) {
            savePerson(person)
        } else {
            goToMatching(person)
        }
    }

    private fun isRegisteringElseIsMatching() =
        preferencesManager.calloutAction === CalloutAction.REGISTER || preferencesManager.calloutAction === CalloutAction.UPDATE

    private fun savePerson(person: Person) {
        dbManager.savePerson(person)
            .subscribeBy(
                onComplete = { handleSavePersonSuccess() },
                onError = { handleSavePersonFailure(it) })
    }

    private fun handleSavePersonSuccess() {
        preferencesManager.lastEnrolDate = Date()
        val registrationResult = Registration(preferencesManager.patientId)
        val result = Intent(Constants.SIMPRINTS_REGISTER_INTENT)
        FormatResult.put(result, registrationResult, preferencesManager.resultFormat)
        view.finishSuccessEnrol(result)
    }

    private fun handleSavePersonFailure(throwable: Throwable) {
        handleUnexpectedError(UnexpectedException(throwable))
        view.cancelAndFinish()
    }

    private fun goToMatching(person: Person) {
        val intent = Intent(context, MatchingActivity::class.java)
        intent.putExtra(IntentKeys.matchingActivityProbePersonKey, person)
        view.finishSuccessAndStartMatching(intent)
    }

    override fun handleUnexpectedError(unexpectedException: UnexpectedException) {
        crashReportManager.logException(unexpectedException)
        Timber.e(unexpectedException)
        view.doLaunchAlert(ALERT_TYPE.UNEXPECTED_ERROR)
    }

    private fun addCaptureEventInSession(finger: Finger) {
        sessionEventsManager.updateSessionInBackground { sessionEvents ->
            sessionEvents.events.add(FingerprintCaptureEvent(
                sessionEvents.timeRelativeToStartTime(lastCaptureStartedAt),
                sessionEvents.nowRelativeToStartTime(timeHelper),
                finger.id,
                preferencesManager.qualityThreshold,
                FingerprintCaptureEvent.Result.fromFingerStatus(finger.status),
                finger.template?.let {
                    FingerprintCaptureEvent.Fingerprint(it.qualityScore, Utils.byteArrayToBase64(it.templateBytes))
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
                mapOfScannedFingers[context.getString(FingerRes.get(it).nameId)] = it.isGoodScan || it.isRescanGoodScan
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
        crashReportManager.logInfo(CrashReportTags.FINGER_CAPTURE, CrashTrigger.UI, message)
    }

    companion object {
        private const val targetNumberOfGoodScans = 2
        private const val maximumTotalNumberOfFingersForAutoAdding = 4
        const val numberOfBadScansRequiredToAutoAddNewFinger = 3
    }
}
