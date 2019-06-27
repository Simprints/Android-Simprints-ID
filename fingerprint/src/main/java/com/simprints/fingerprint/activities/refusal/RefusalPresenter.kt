package com.simprints.fingerprint.activities.refusal

import android.annotation.SuppressLint
import android.app.Activity
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportManager
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportTag.REFUSAL
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportTrigger.UI
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.eventData.model.RefusalEvent
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.data.domain.refusal.RefusalActResult
import com.simprints.fingerprint.data.domain.refusal.RefusalFormReason
import com.simprints.fingerprint.data.domain.refusal.RefusalFormReason.*
import com.simprints.fingerprint.data.domain.refusal.toRefusalAnswerForEvent
import com.simprints.fingerprint.di.FingerprintComponent
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

class RefusalPresenter(private val view: RefusalContract.View,
                       component: FingerprintComponent) : RefusalContract.Presenter {

    @Inject lateinit var crashReportManager: FingerprintCrashReportManager
    @Inject lateinit var sessionEventsManager: FingerprintSessionEventsManager
    @Inject lateinit var timeHelper: FingerprintTimeHelper

    private var reason: RefusalFormReason = OTHER
    private var refusalStartTime: Long = 0

    init {
        component.inject(this)
        refusalStartTime = timeHelper.now()
    }

    override fun start() {
    }

    override fun handleRadioOptionCheckedChange() {
        enableSubmitButtonAndRefusalText()
    }

    override fun handleReligiousConcernsRadioClick() {
        reason = REFUSED_RELIGION
        logRadioOptionForCrashReport("Religious concerns")
    }

    override fun handleDataConcernsRadioClick() {
        reason = REFUSED_DATA_CONCERNS
        logRadioOptionForCrashReport("Data concerns")
    }

    override fun handleTooYoungRadioClick() {
        reason = REFUSED_YOUNG
        logRadioOptionForCrashReport("Too young")
    }

    override fun handleSickRadioClick() {
        reason = REFUSED_SICK
        logRadioOptionForCrashReport("Sick")
    }

    override fun handlePregnantRadioClick() {
        reason = REFUSED_PREGNANT
        logRadioOptionForCrashReport("Pregnant")
    }

    override fun handleDoesNotHavePermissionRadioClick() {
        reason = REFUSED_PERMISSION
        logRadioOptionForCrashReport("Does not have permission")
    }

    override fun handleAppNotWorkingRadioClick() {
        reason = SCANNER_NOT_WORKING
        view.setFocusOnRefusalReason()
        logRadioOptionForCrashReport("App not working")
    }

    override fun handleOtherRadioOptionClick() {
        reason = OTHER
        view.setFocusOnRefusalReasonAndDisableSubmit()
        logRadioOptionForCrashReport("Other")
    }

    private fun enableSubmitButtonAndRefusalText() {
        view.enableSubmitButton()
        view.enableRefusalText()
    }

    @SuppressLint("CheckResult")
    override fun handleSubmitButtonClick(refusalText: String) {
        logMessageForCrashReport("Submit button clicked")

        logAsMalfunctionInCrashReportIfAppNotWorking(refusalText)

        addRefusalEventInSession(reason, refusalText).doFinally {
            setResultAndFinishInView(refusalText)
        }.subscribeBy(onError = {
            crashReportManager.logExceptionOrSafeException(it)
        })
    }

    private fun addRefusalEventInSession(refusalReason: RefusalFormReason, refusalText: String) =
        sessionEventsManager.addEvent(RefusalEvent(
            refusalStartTime,
            timeHelper.now(),
            refusalReason.toRefusalAnswerForEvent(),
            refusalText))


    private fun setResultAndFinishInView(refusalText: String) {
        view.setResultAndFinish(
            Activity.RESULT_OK,
            RefusalActResult(
                RefusalActResult.Action.SUBMIT,
                RefusalActResult.Answer(reason, refusalText)))
    }

    private fun logAsMalfunctionInCrashReportIfAppNotWorking(refusalText: String) {
        if(reason == SCANNER_NOT_WORKING) {
            crashReportManager.logMalfunction(refusalText)
        }
    }

    override fun handleScanFingerprintsClick() {
        logMessageForCrashReport("Scan fingerprints button clicked")
        view.setResultAndFinish(Activity.RESULT_OK,
            RefusalActResult(
                RefusalActResult.Action.SCAN_FINGERPRINTS, RefusalActResult.Answer()))
    }

    override fun handleLayoutChange() {
        view.scrollToBottom()
    }

    override fun handleChangesInRefusalText(refusalText: String) {
        if (refusalText.isNotBlank()) {
            view.enableSubmitButton()
        } else {
            view.disableSubmitButton()
        }
    }

    override fun handleOnBackPressed() {
        if (view.isSubmitButtonEnabled()) {
            view.showToastForFormSubmit()
        } else {
            view.showToastForSelectOptionAndSubmit()
        }
    }
    private fun logRadioOptionForCrashReport(option: String) {
        logMessageForCrashReport("Radio option $option clicked")
    }

    private fun logMessageForCrashReport(message: String) {
        crashReportManager.logMessageForCrashReport(REFUSAL, UI, message = message)
    }
}
