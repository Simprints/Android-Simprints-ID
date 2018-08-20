package com.simprints.id.activities.refusal

import android.app.Activity
import com.simprints.id.R
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.eventData.SessionEventsManager
import com.simprints.id.data.analytics.eventData.models.events.RefusalEvent
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.remote.enums.REFUSAL_FORM_REASON
import com.simprints.id.di.AppComponent
import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.exceptions.unsafe.UninitializedDataManagerError
import com.simprints.id.tools.InternalConstants
import com.simprints.id.tools.TimeHelper
import com.simprints.libsimprints.RefusalForm
import javax.inject.Inject

class RefusalPresenter(private val view: RefusalContract.View,
                       component: AppComponent) : RefusalContract.Presenter {

    @Inject lateinit var dbManager: DbManager
    @Inject lateinit var analyticsManager: AnalyticsManager
    @Inject lateinit var sessionEventsManager: SessionEventsManager
    @Inject lateinit var timeHelper: TimeHelper

    private var reason: REFUSAL_FORM_REASON? = null
    private var refusalStartTime: Long = 0

    init {
        component.inject(this)
        refusalStartTime = timeHelper.msSinceBoot()
    }

    override fun start() {
    }

    override fun handleRadioOptionClicked(optionIdentifier: Int) {
        view.enableSubmitButton()
        view.enableRefusalText()
        when (optionIdentifier) {
            R.id.rbScannerNotWorking ->
                reason = REFUSAL_FORM_REASON.SCANNER_NOT_WORKING
            R.id.rbRefused ->
                reason = REFUSAL_FORM_REASON.REFUSED
            R.id.rb_other ->
                reason = REFUSAL_FORM_REASON.OTHER
        }
    }

    override fun handleSubmitButtonClick(reason: REFUSAL_FORM_REASON?, refusalText: String) {
        reason.let {
            saveRefusalFormInDb(getRefusalForm(refusalText))
        }
        view.setResultAndFinish(Activity.RESULT_CANCELED, reason)

        reason?.let {
            sessionEventsManager.updateSessionInBackground({
                it.events.add(RefusalEvent(
                    it.timeRelativeToStartTime(refusalStartTime),
                    it.nowRelativeToStartTime(timeHelper),
                    RefusalEvent.Answer.fromRefusalReason(reason),
                    refusalText))
            })
        }
    }

    override fun handleScanFingerprintsClick() {
        view.setResultAndFinish(InternalConstants.RESULT_TRY_AGAIN, null)
    }

    override fun handleLayoutChange() {
        view.scrollToBottom()
    }

    override fun handleChangesInRefusalText(refusalText: String) {
        if (refusalText.isEmpty()) {
            view.disableSubmitButton()
        } else {
            view.enableSubmitButton()
        }
    }

    private fun getRefusalForm(refusalText: String) = RefusalForm(reason.toString(), refusalText)

    private fun saveRefusalFormInDb(refusalForm: RefusalForm) {
        try {
            dbManager.saveRefusalForm(refusalForm)
        } catch (error: UninitializedDataManagerError) {
            analyticsManager.logError(error)
            view.doLaunchAlert(ALERT_TYPE.UNEXPECTED_ERROR)
        }
    }
}
