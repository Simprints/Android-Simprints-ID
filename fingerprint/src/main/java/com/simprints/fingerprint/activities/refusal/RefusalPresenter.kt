package com.simprints.fingerprint.activities.refusal

import android.annotation.SuppressLint
import com.simprints.fingerprint.activities.refusal.result.RefusalTaskResult
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportManager
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportTag.REFUSAL
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportTrigger.UI
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.eventData.model.RefusalEvent
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.data.domain.refusal.RefusalFormReason
import com.simprints.fingerprint.data.domain.refusal.RefusalFormReason.*
import com.simprints.fingerprint.data.domain.refusal.toRefusalAnswerForEvent
import com.simprints.fingerprint.orchestrator.domain.ResultCode
import io.reactivex.rxkotlin.subscribeBy

class RefusalPresenter(private val view: RefusalContract.View,
                       private val crashReportManager: FingerprintCrashReportManager,
                       private val sessionEventsManager: FingerprintSessionEventsManager,
                       private val timeHelper: FingerprintTimeHelper) : RefusalContract.Presenter {

    private var reason: RefusalFormReason = OTHER
    private var refusalStartTime: Long = 0

    init {
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

    override fun handlePersonNotPresentRadioClick() {
        reason = REFUSED_NOT_PRESENT
        logRadioOptionForCrashReport("Person not present")
    }

    override fun handleDoesNotHavePermissionRadioClick() {
        reason = REFUSED_PERMISSION
        logRadioOptionForCrashReport("Does not have permission")
    }

    override fun handleAppNotWorkingRadioClick() {
        reason = SCANNER_NOT_WORKING
        view.setFocusOnRefusalReasonAndDisableSubmit()
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
            ResultCode.REFUSED.value,
            RefusalTaskResult(
                RefusalTaskResult.Action.SUBMIT,
                RefusalTaskResult.Answer(reason, refusalText)))
    }

    private fun logAsMalfunctionInCrashReportIfAppNotWorking(refusalText: String) {
        if (reason == SCANNER_NOT_WORKING) {
            crashReportManager.logMalfunction(refusalText)
        }
    }

    override fun handleScanFingerprintsClick() {
        logMessageForCrashReport("Scan fingerprints button clicked")
        view.setResultAndFinish(ResultCode.OK.value,
            RefusalTaskResult(
                RefusalTaskResult.Action.SCAN_FINGERPRINTS, RefusalTaskResult.Answer()))
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
