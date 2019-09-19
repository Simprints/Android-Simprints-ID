package com.simprints.fingerprint.activities.collect

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.widget.Toast
import com.simprints.core.tools.EncodingUtils
import com.simprints.core.tools.LanguageHelper
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.alert.FingerprintAlert
import com.simprints.fingerprint.activities.collect.confirmFingerprints.ConfirmFingerprintsDialog
import com.simprints.fingerprint.activities.collect.fingers.CollectFingerprintsFingerDisplayHelper
import com.simprints.fingerprint.activities.collect.indicators.CollectFingerprintsIndicatorsHelper
import com.simprints.fingerprint.activities.collect.models.Finger
import com.simprints.fingerprint.activities.collect.models.FingerRes
import com.simprints.fingerprint.activities.collect.scanning.CollectFingerprintsScanningHelper
import com.simprints.fingerprint.controllers.core.androidResources.FingerprintAndroidResourcesHelper
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportManager
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportTag.FINGER_CAPTURE
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportTrigger.UI
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.eventData.model.FingerprintCaptureEvent
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.controllers.core.repository.FingerprintDbManager
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.data.domain.collect.CollectFingerprintsActResult
import com.simprints.fingerprint.data.domain.matching.request.MatchingActIdentifyRequest
import com.simprints.fingerprint.data.domain.matching.request.MatchingActIdentifyRequest.QueryForIdentifyPool
import com.simprints.fingerprint.data.domain.matching.request.MatchingActRequest
import com.simprints.fingerprint.data.domain.matching.request.MatchingActVerifyRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.*
import com.simprints.fingerprint.data.domain.person.Fingerprint
import com.simprints.fingerprint.data.domain.person.Person
import com.simprints.fingerprint.di.FingerprintComponent
import com.simprints.fingerprint.exceptions.FingerprintSimprintsException
import com.simprints.fingerprint.exceptions.unexpected.FingerprintUnexpectedException
import com.simprints.id.tools.AndroidResourcesHelper
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.math.min

class CollectFingerprintsPresenter(private val context: Context,
                                   private val view: CollectFingerprintsContract.View,
                                   private val fingerprintRequest: FingerprintRequest,
                                   private val component: FingerprintComponent)
    : CollectFingerprintsContract.Presenter {

    @Inject lateinit var crashReportManager: FingerprintCrashReportManager
    @Inject lateinit var dbManager: FingerprintDbManager
    @Inject lateinit var timeHelper: FingerprintTimeHelper
    @Inject lateinit var sessionEventsManager: FingerprintSessionEventsManager
    @Inject lateinit var preferencesManager: FingerprintPreferencesManager
    @Inject lateinit var androidResourcesHelper: FingerprintAndroidResourcesHelper

    private lateinit var scanningHelper: CollectFingerprintsScanningHelper
    private lateinit var fingerDisplayHelper: CollectFingerprintsFingerDisplayHelper
    private lateinit var indicatorsHelper: CollectFingerprintsIndicatorsHelper

    // Array with only the active Fingers, used to populate the ViewPager
    override val activeFingers = ArrayList<Finger>()
    override var currentActiveFingerNo: Int = 0
    override var isConfirmDialogShown = false
    override var isBusyWithFingerTransitionAnimation = false
    private var lastCaptureStartedAt: Long = 0
    private var confirmDialog: AlertDialog? = null

    init {
        component.inject(this)
    }

    override fun start() {
        initFingerDisplayHelper(view)
        initIndicatorsHelper(context, view)
        initScanningHelper(context, view)
        initScanButtonListeners()
        refreshDisplay()
    }

    private fun initFingerDisplayHelper(view: CollectFingerprintsContract.View) {
        fingerDisplayHelper = CollectFingerprintsFingerDisplayHelper(
            view,
            this,
            fingerprintRequest.fingerStatus)
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

    override fun handleScannerButtonPressed() {
        startCapturing()
    }

    override fun getExtraForMatchingActivity(fingerprintsActResult: CollectFingerprintsActResult): MatchingActRequest =
        when (fingerprintRequest) {
            is FingerprintVerifyRequest ->

                MatchingActVerifyRequest(
                    fingerprintRequest.language,
                    fingerprintsActResult.probe,
                    MatchingActVerifyRequest.QueryForVerifyPool(fingerprintRequest.projectId),
                    fingerprintRequest.verifyGuid)

            is FingerprintIdentifyRequest ->

                MatchingActIdentifyRequest(
                    fingerprintRequest.language,
                    fingerprintsActResult.probe,
                    buildQueryForIdentifyPool(fingerprintRequest),
                    fingerprintRequest.returnIdCount)

            else -> throw IllegalArgumentException("Request different from FingerprintVerifyRequest or FingerprintIdentifyRequest")
        }

    private fun buildQueryForIdentifyPool(request: FingerprintIdentifyRequest): QueryForIdentifyPool =
        when(request.matchGroup) {
            MatchGroup.GLOBAL -> QueryForIdentifyPool(request.projectId)
            MatchGroup.USER -> QueryForIdentifyPool(request.projectId, userId = request.userId)
            MatchGroup.MODULE -> QueryForIdentifyPool(request.projectId, moduleId = request.moduleId)
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
            fingerDisplayHelper.doNudge()
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
        fingerprintRequest.fingerStatus.filter { it.value }.size

    override fun fingerHasSatisfiedTerminalCondition(finger: Finger) =
        ((tooManyBadScans(finger) || finger.isGoodScan || finger.isRescanGoodScan) && finger.template != null) || finger.isFingerSkipped

    override fun getTitle(): String =
        when (fingerprintRequest) {
            is FingerprintEnrolRequest -> context.getString(R.string.register_title)
            is FingerprintIdentifyRequest -> context.getString(R.string.identify_title)
            is FingerprintVerifyRequest -> context.getString(R.string.verify_title)
            else -> ""
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

    override fun handleOnBackPressed() {
        if (isScanning()) {
            stopCapturing()
        } else {
            scanningHelper.stopReconnecting()
            view.startRefusalActivity()
        }
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
            fingerprintRequest.projectId,
            fingerprintRequest.userId,
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

    @SuppressLint("CheckResult")
    private fun savePerson(person: Person) {
        dbManager.savePerson(person)
            .subscribeBy(
                onComplete = { handleSavePersonSuccess(person) },
                onError = { handleSavePersonFailure(it) })
    }

    private fun handleSavePersonSuccess(probe: Person) {
        preferencesManager.lastEnrolDate = Date()
        view.finishSuccessEnrol(CollectFingerprintsActResult.BUNDLE_KEY, CollectFingerprintsActResult(probe))
    }

    private fun handleSavePersonFailure(throwable: Throwable) {
        handleException(FingerprintUnexpectedException(throwable))
        view.cancelAndFinish()
    }

    private fun goToMatching(person: Person) {
        view.finishSuccessAndStartMatching(CollectFingerprintsActResult.BUNDLE_KEY, CollectFingerprintsActResult(person))
    }

    override fun handleException(simprintsException: FingerprintSimprintsException) {
        crashReportManager.logExceptionOrSafeException(simprintsException)
        Timber.e(simprintsException)
        view.doLaunchAlert(FingerprintAlert.UNEXPECTED_ERROR)
    }

    private fun addCaptureEventInSession(finger: Finger) {
        sessionEventsManager.addEventInBackground(FingerprintCaptureEvent(
            timeHelper.now(),
            lastCaptureStartedAt,
            finger.id,
            qualityThreshold,
            FingerprintCaptureEvent.buildResult(finger.status),
            finger.template?.let {
                FingerprintCaptureEvent.Fingerprint(finger.id, it.qualityScore, EncodingUtils.byteArrayToBase64(it.templateBytes))
            }
        ))
    }

    private fun createMapAndShowDialog() {
        isConfirmDialogShown = true
        confirmDialog = ConfirmFingerprintsDialog(context, androidResourcesHelper, createMapForScannedFingers(),
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
        isBusyWithFingerTransitionAnimation = false
    }

    override fun handleMissingFingerClick() {
        logMessageForCrashReport("Missing finger text clicked")
        if (!currentFinger().isCollecting && !isBusyWithFingerTransitionAnimation) {
            isBusyWithFingerTransitionAnimation = true
            scanningHelper.setCurrentFingerAsSkippedAndAsNumberOfBadScansToAutoAddFinger()
            lastCaptureStartedAt = timeHelper.now()
            addCaptureEventInSession(currentFinger())
            resolveFingerTerminalConditionTriggered()
        }
    }

    private fun logMessageForCrashReport(message: String) {
        crashReportManager.logMessageForCrashReport(FINGER_CAPTURE, UI, message = message)
    }

    companion object {
        private const val targetNumberOfGoodScans = 2
        private const val maximumTotalNumberOfFingersForAutoAdding = 4
        const val numberOfBadScansRequiredToAutoAddNewFinger = 3
        const val qualityThreshold = 60
        const val timeoutInMillis = 3000
    }
}
