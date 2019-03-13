package com.simprints.id.activities.refusal

import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView
import com.simprints.id.domain.alert.Alert
import com.simprints.id.domain.refusal_form.RefusalFormAnswer

interface RefusalContract {

    interface View : BaseView<Presenter> {

        fun doLaunchAlert(alert: Alert)

        fun setResultAndFinish(activityResult: Int, refusalAnswer: RefusalFormAnswer)

        fun scrollToBottom()

        fun enableSubmitButton()

        fun enableRefusalText()
    }

    interface Presenter : BasePresenter {

        fun handleSubmitButtonClick(refusalText: String)

        fun handleScanFingerprintsClick()

        fun handleLayoutChange()

        fun handleChangesInRefusalText(refusalText: String)

        fun handleRadioOptionClicked(optionIdentifier: Int)
    }
}
