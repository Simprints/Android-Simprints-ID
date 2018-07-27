package com.simprints.id.activities.collectFingerprints

import android.app.Activity
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
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.domain.Finger
import com.simprints.id.domain.FingerRes
import com.simprints.id.exceptions.unsafe.InvalidCalloutParameterError
import com.simprints.id.exceptions.unsafe.SimprintsError
import com.simprints.id.session.callout.CalloutAction
import com.simprints.id.tools.FormatResult
import com.simprints.id.tools.LanguageHelper
import com.simprints.id.tools.TimeHelper
import com.simprints.libcommon.Fingerprint
import com.simprints.libcommon.Person
import com.simprints.libsimprints.Constants
import com.simprints.libsimprints.Registration
import io.reactivex.rxkotlin.subscribeBy
import java.util.*
import javax.inject.Inject

class CollectFingerprintsPresenter(private val context: Context,
                                   private val view: CollectFingerprintsContract.View)
    : CollectFingerprintsContract.Presenter {

    @Inject lateinit var analyticsManager: AnalyticsManager
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var dbManager: DbManager
    @Inject lateinit var timeHelper: TimeHelper

    private lateinit var scanningHelper: CollectFingerprintsScanningHelper
    private lateinit var fingerDisplayHelper: CollectFingerprintsFingerDisplayHelper
    private lateinit var indicatorsHelper: CollectFingerprintsIndicatorsHelper

    // Array with only the active Fingers, used to populate the ViewPager
    override val activeFingers = ArrayList<Finger>()
    override var currentActiveFingerNo: Int = 0
    override var isConfirmDialogShown = false
    private var numberOfFingersAdded = 0

    init {
        ((view as Activity).application as Application).component.inject(this)
    }

    override fun start() {
        preferencesManager.msSinceBootOnMainStart = timeHelper.msSinceBoot()
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
        view.scanButton.setOnClickListener { scanningHelper.toggleContinuousCapture() }
        view.scanButton.setOnLongClickListener { resetFingerState() }
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
        scanningHelper.toggleContinuousCapture()
    }

    override fun showSplashAndAddNewFingerIfNecessary() {
        if (tooManyBadScans(currentFinger()) && haveNotExceedMaximumNumberOfFingersToAdd()) {
            fingerDisplayHelper.showSplashAndAddNewFinger()
            numberOfFingersAdded++
        }
    }

    private fun tooManyBadScans(finger: Finger) =
        finger.numberOfBadScans >= numberOfBadScansRequiredToAutoAddNewFinger

    private fun haveNotExceedMaximumNumberOfFingersToAdd() =
        numberOfFingersAdded < maximumNumberOfFingersAdded

    override fun doNudgeIfNecessary() {
        fingerDisplayHelper.doNudgeIfNecessary()
    }

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

    override fun handleOnStart() {
        scanningHelper.startListeners()
    }

    override fun handleOnStop() {
        scanningHelper.stopListeners()
    }

    override fun handleOnBackPressedToLeave() {
        scanningHelper.stopReconnecting()
    }

    private fun getCollectedFingerprints(): List<Finger> =
        activeFingers.filter { isFingerScannedAndHasTemplate(it) }

    private fun getNumberOfCollectedFingerprints(): Int =
        getCollectedFingerprints().size

    override fun handleConfirmFingerprintsAndContinue() {
        val fingerprints = getCollectedFingerprints()
            .map { Fingerprint(it.id, it.template.templateBytes) }

        if (fingerprints.isEmpty()) {
            Toast.makeText(context, "Please scan at least 1 required finger", Toast.LENGTH_LONG).show()
        } else {
            proceedToFinish(fingerprints)
        }
    }

    private fun isFingerScannedAndHasTemplate(it: Finger) =
        (it.isGoodScan || it.isBadScan || it.isRescanGoodScan) && it.template != null

    private fun proceedToFinish(fingerprints: List<Fingerprint>) {
        val person = Person(preferencesManager.patientId, fingerprints)
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
        handleUnexpectedError(SimprintsError(throwable))
        view.cancelAndFinish()
    }

    private fun goToMatching(person: Person) {
        val intent = Intent(context, MatchingActivity::class.java)
        intent.putExtra(IntentKeys.matchingActivityProbePersonKey, person)
        view.finishSuccessAndStartMatching(intent)
    }

    override fun handleUnexpectedError(error: SimprintsError) {
        analyticsManager.logError(error)
        view.doLaunchAlert(ALERT_TYPE.UNEXPECTED_ERROR)
    }

    override fun checkScannedFingersAndCreateMapToShowDialog() {
        if (everyActiveFingerHasBeenScanned()) {
            if (weHaveTheMinimumNumberOfAnyQualityScans() || weHaveTheMinimumNumberOfGoodScans()) {
                createMapAndShowDialog()
            }
        }
    }

    private fun weHaveTheMinimumNumberOfGoodScans(): Boolean =
        getCollectedFingerprints().filter { it.isGoodScan }.size >= minimumNumberOfGoodScans

    private fun weHaveTheMinimumNumberOfAnyQualityScans() =
        getNumberOfCollectedFingerprints() >= minimumNumberOfAnyQualityScans

    private fun everyActiveFingerHasBeenScanned() =
        getNumberOfCollectedFingerprints() == activeFingers.size

    private fun createMapAndShowDialog() {
        isConfirmDialogShown = true
        ConfirmFingerprintsDialog(context, createMapForScannedFingers(),
            callbackConfirm = { handleConfirmFingerprintsAndContinue() },
            callbackRestart = { handleRestart() })
            .create()
            .show()
    }

    private fun createMapForScannedFingers(): MutableMap<String, Boolean> =
        mutableMapOf<String, Boolean>().also { mapOfScannedFingers ->
            activeFingers.forEach {
                mapOfScannedFingers[context.getString(FingerRes.get(it).nameId)] = it.isGoodScan
            }
        }

    private fun handleRestart() {
        fingerDisplayHelper.clearAndPopulateFingerArraysWithDefaultFingers()
        fingerDisplayHelper.handleFingersChanged()
        fingerDisplayHelper.resetFingerIndexToBeginning()
        numberOfFingersAdded = 0
        isConfirmDialogShown = false
    }

    companion object {
        private const val maximumNumberOfFingersAdded = 2
        private const val minimumNumberOfGoodScans = 2
        private const val minimumNumberOfAnyQualityScans = minimumNumberOfGoodScans + maximumNumberOfFingersAdded
        private const val numberOfBadScansRequiredToAutoAddNewFinger = 3
    }
}
