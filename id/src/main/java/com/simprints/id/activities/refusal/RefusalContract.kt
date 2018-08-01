package com.simprints.id.activities.refusal

import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView
import com.simprints.id.data.db.remote.enums.REFUSAL_FORM_REASON
import com.simprints.id.domain.ALERT_TYPE


interface RefusalContract {

    interface View: BaseView<Presenter> {

        fun doLaunchAlert(alertType: ALERT_TYPE)

        fun setResultAndFinish(activityResult: Int, reason: REFUSAL_FORM_REASON?)

        fun scrollToBottom()

        fun disableSubmitButton()

        fun enableSubmitButton()

        fun enableRefusalText()
    }

    interface Presenter: BasePresenter {

        fun handleSubmitButtonClick(reason: REFUSAL_FORM_REASON?, refusalText: String)

        fun handleScanFingerprintsClick()

        fun handleLayoutChange()

        fun handleChangesInRefusalText(refusalText: String)

        fun handleRadioOptionClicked(optionIdentifier: Int)
    }
}
