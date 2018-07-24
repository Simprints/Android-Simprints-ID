package com.simprints.id.activities.refusal

import android.app.Activity
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.remote.enums.REFUSAL_FORM_REASON
import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.exceptions.unsafe.UninitializedDataManagerError
import com.simprints.id.tools.InternalConstants
import com.simprints.libsimprints.RefusalForm
import javax.inject.Inject

class RefusalPresenter(private val view: RefusalContract.View) : RefusalContract.Presenter {



    @Inject lateinit var dbManager: DbManager
    @Inject lateinit var analyticsManager: AnalyticsManager
    private var reason: REFUSAL_FORM_REASON? = null
    var checked = false

    private val activity = view as Activity

    init {
        (activity.application as Application).component.inject(this)
    }

    override fun start() {
    }

    override fun handleSubmitButtonClick(reason: REFUSAL_FORM_REASON?, refusalText: String) {
        reason.let {
            saveRefusalFormInDb(getRefusalForm(refusalText))
        }
        view.setResultAndFinish(Activity.RESULT_CANCELED, reason)
    }

    override fun handleBackToSimprintsClick() {
        view.setResultAndFinish(InternalConstants.RESULT_TRY_AGAIN, null)
    }

    override fun handleLayoutChange() {
        if(checked)
            view.scrollToBottom()
    }

    override fun handleChangesInRefusalText(refusalText: String) {
        if (refusalText.isEmpty()) {
            view.disableSubmitButton()
        } else {
            view.enableSubmitButton()
        }
    }

    override fun handleRadioOptionClicked(optionIdentifier: Int) {

        checked = true
        view.enableSubmitButton()
        view.enableRefusalText()
        when (optionIdentifier) {
            R.id.rbScannerNotHere ->
                reason = REFUSAL_FORM_REASON.SCANNER_NOT_HERE
            R.id.rbScannerNotWorking ->
                reason = REFUSAL_FORM_REASON.SCANNER_NOT_WORKING
            R.id.rbUnableToCapture ->
                reason = REFUSAL_FORM_REASON.UNABLE_TO_CAPTURE_GOOD_SCAN
            R.id.rbUnableToGive ->
                reason = REFUSAL_FORM_REASON.UNABLE_TO_GIVE_PRINTS
            R.id.rbRefused ->
                reason = REFUSAL_FORM_REASON.REFUSED
            R.id.rb_other -> {
                reason = REFUSAL_FORM_REASON.OTHER
                view.disableSubmitButton()
            }
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
