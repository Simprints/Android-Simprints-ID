package com.simprints.fingerprint.activities.refusal

import android.annotation.SuppressLint
import android.app.Activity
import com.simprints.fingerprint.R
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

    override fun handleRadioOptionClicked(optionIdentifier: Int) {
        view.enableSubmitButton()
        view.enableRefusalText()
        when (optionIdentifier) {
            R.id.rbScannerNotWorking -> {
                reason = SCANNER_NOT_WORKING
                logMessageForCrashReport("Radio option $SCANNER_NOT_WORKING Clicked")
            }
            R.id.rbRefused -> {
                reason = REFUSED
                logMessageForCrashReport("Radio option $REFUSED Clicked")
            }
            R.id.rb_other -> {
                reason = OTHER
                logMessageForCrashReport("Radio option $OTHER Clicked")
            }
        }
    }

    @SuppressLint("CheckResult")
    override fun handleSubmitButtonClick(refusalText: String) {
        logMessageForCrashReport("Submit button clicked")
        reason.let { refusalReason ->
            sessionEventsManager.addEvent(RefusalEvent(
                refusalStartTime,
                timeHelper.now(),
                refusalReason.toRefusalAnswerForEvent(),
                refusalText))
        }.doFinally {

            view.setResultAndFinish(
                Activity.RESULT_OK,
                RefusalActResult(
                    RefusalActResult.Action.SUBMIT,
                    RefusalActResult.Answer(reason, refusalText)))

        }.subscribeBy(onError = {
            crashReportManager.logExceptionOrSafeException(it)
        })
    }

    override fun handleScanFingerprintsClick() {
        logMessageForCrashReport("Scan fingerprints button clicked")
        view.setResultAndFinish(Activity.RESULT_OK,
            RefusalActResult(
                RefusalActResult.Action.SCAN_FINGERPRINTS))
    }

    override fun handleLayoutChange() {
        view.scrollToBottom()
    }

    override fun handleChangesInRefusalText(refusalText: String) {
        view.enableSubmitButton()
    }

    private fun logMessageForCrashReport(message: String) {
        crashReportManager.logMessageForCrashReport(REFUSAL, UI, message = message)
    }
}
