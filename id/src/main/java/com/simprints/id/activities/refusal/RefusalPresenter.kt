package com.simprints.id.activities.refusal

import android.app.Activity
import com.simprints.id.R
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.models.domain.events.RefusalEvent
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.remote.enums.REFUSAL_FORM_REASON
import com.simprints.id.di.AppComponent
import com.simprints.id.domain.refusal_form.IdRefusalForm
import com.simprints.id.tools.InternalConstants
import com.simprints.id.tools.TimeHelper
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

class RefusalPresenter(private val view: RefusalContract.View,
                       component: AppComponent) : RefusalContract.Presenter {

    @Inject lateinit var dbManager: DbManager
    @Inject lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var sessionEventsManager: SessionEventsManager
    @Inject lateinit var timeHelper: TimeHelper

    private var reason: REFUSAL_FORM_REASON? = null
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
                reason = REFUSAL_FORM_REASON.SCANNER_NOT_WORKING
                logMessageForCrashReport("Radio option ${REFUSAL_FORM_REASON.SCANNER_NOT_WORKING} Clicked")
            }
            R.id.rbRefused -> {
                reason = REFUSAL_FORM_REASON.REFUSED
                logMessageForCrashReport("Radio option ${REFUSAL_FORM_REASON.REFUSED} Clicked")
            }
            R.id.rb_other -> {
                reason = REFUSAL_FORM_REASON.OTHER
                logMessageForCrashReport("Radio option ${REFUSAL_FORM_REASON.OTHER} Clicked")
            }
        }
    }

    override fun handleSubmitButtonClick(refusalText: String) {
        logMessageForCrashReport("Submit button clicked")
        saveRefusalFormInDb(getRefusalForm(refusalText))
        reason?.let { refusalReason ->
            sessionEventsManager.updateSession {
                it.events.add(RefusalEvent(
                        it.timeRelativeToStartTime(refusalStartTime),
                        it.nowRelativeToStartTime(timeHelper),
                        RefusalEvent.Answer.fromRefusalReason(refusalReason),
                        refusalText))
            }.subscribeBy(onError = {
                crashReportManager.logExceptionOrThrowable(it)
                view.setResultAndFinish(Activity.RESULT_CANCELED, reason)
            }, onComplete = {
                view.setResultAndFinish(Activity.RESULT_CANCELED, reason)
            })
        } ?: view.setResultAndFinish(Activity.RESULT_CANCELED, reason)
    }

    override fun handleScanFingerprintsClick() {
        logMessageForCrashReport("Scan fingerprints button clicked")
        view.setResultAndFinish(InternalConstants.RESULT_TRY_AGAIN, null)
    }

    override fun handleLayoutChange() {
        view.scrollToBottom()
    }

    override fun handleChangesInRefusalText(refusalText: String) {
        view.enableSubmitButton()
    }

    private fun getRefusalForm(refusalText: String) = IdRefusalForm(reason.toString(), refusalText)

    private fun saveRefusalFormInDb(refusalForm: IdRefusalForm) {
            dbManager.saveRefusalForm(refusalForm)
    }

    private fun logMessageForCrashReport(message: String) {
        crashReportManager.logMessageForCrashReport(CrashReportTag.REFUSAL, CrashReportTrigger.UI, message = message)
    }
}
