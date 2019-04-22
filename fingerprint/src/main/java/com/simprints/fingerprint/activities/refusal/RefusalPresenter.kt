package com.simprints.fingerprint.activities.refusal

import android.app.Activity
import com.simprints.fingerprint.R
import com.simprints.fingerprint.data.domain.refusal.RefusalActResult
import com.simprints.fingerprint.data.domain.refusal.RefusalFormReason
import com.simprints.fingerprint.data.domain.refusal.RefusalFormReason.*
import com.simprints.fingerprint.data.domain.refusal.toAnswerEvent
import com.simprints.fingerprint.di.FingerprintComponent
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.models.domain.events.RefusalEvent
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

class RefusalPresenter(private val view: RefusalContract.View,
                       component: FingerprintComponent) : RefusalContract.Presenter {

    @Inject lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var sessionEventsManager: SessionEventsManager
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

    override fun handleSubmitButtonClick(refusalText: String) {
        logMessageForCrashReport("Submit button clicked")
        reason.let { refusalReason ->
            sessionEventsManager.updateSession {
                it.addEvent(RefusalEvent(
                        it.timeRelativeToStartTime(refusalStartTime),
                        it.timeRelativeToStartTime(timeHelper.now()),
                        refusalReason.toAnswerEvent(),
                        refusalText))
            }.subscribeBy(onError = {
                crashReportManager.logExceptionOrThrowable(it)
                view.setResultAndFinish(Activity.RESULT_CANCELED, RefusalActResult(reason, refusalText))
            }, onComplete = {
                view.setResultAndFinish(Activity.RESULT_OK, RefusalActResult(reason, refusalText))
            })
        }
    }

    override fun handleScanFingerprintsClick() {
        logMessageForCrashReport("Scan fingerprints button clicked")
        view.setResultAndFinish(Activity.RESULT_OK, RefusalActResult(reason))
    }

    override fun handleLayoutChange() {
        view.scrollToBottom()
    }

    override fun handleChangesInRefusalText(refusalText: String) {
        view.enableSubmitButton()
    }

    private fun logMessageForCrashReport(message: String) {
        crashReportManager.logMessageForCrashReport(CrashReportTag.REFUSAL, CrashReportTrigger.UI, message = message)
    }
}
