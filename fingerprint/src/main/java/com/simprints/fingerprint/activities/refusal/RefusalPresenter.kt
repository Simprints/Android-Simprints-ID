package com.simprints.fingerprint.activities.refusal

import android.annotation.SuppressLint
import com.simprints.fingerprint.activities.refusal.result.RefusalTaskResult
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportManager
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportTag.REFUSAL
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.eventData.model.RefusalAnswer
import com.simprints.fingerprint.controllers.core.eventData.model.RefusalEvent
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.data.domain.refusal.RefusalFormReason
import com.simprints.fingerprint.data.domain.refusal.RefusalFormReason.*
import com.simprints.fingerprint.orchestrator.domain.ResultCode
import com.simprints.logging.Simber
import kotlinx.coroutines.runBlocking

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

        addRefusalEventInSession(reason, refusalText)
        setResultAndFinishInView(refusalText)
    }

    private fun addRefusalEventInSession(refusalReason: RefusalFormReason, refusalText: String) {
        runBlocking {
            try {
                sessionEventsManager.addEvent(RefusalEvent(
                    refusalStartTime,
                    timeHelper.now(),
                    RefusalAnswer.fromRefusalFormReason(refusalReason),
                    refusalText))
            } catch (t: Throwable) {
                Simber.d(t)
                crashReportManager.logExceptionOrSafeException(t)
            }
        }
    }


    private fun setResultAndFinishInView(refusalText: String) {
        view.setResultAndFinish(
            ResultCode.REFUSED.value,
            RefusalTaskResult(
                RefusalTaskResult.Action.SUBMIT,
                RefusalTaskResult.Answer(reason, refusalText)))
    }

    private fun logAsMalfunctionInCrashReportIfAppNotWorking(refusalText: String) {
        if (reason == SCANNER_NOT_WORKING) {
            Simber.w(refusalText)
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
        Simber.tag(REFUSAL.name).i(message)
    }
}
