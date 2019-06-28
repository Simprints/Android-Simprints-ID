package com.simprints.fingerprint.activities.refusal

import com.simprints.fingerprint.activities.BasePresenter
import com.simprints.fingerprint.activities.BaseView
import com.simprints.fingerprint.activities.refusal.result.RefusalActResult

interface RefusalContract {

    interface View : BaseView<Presenter> {

        fun setResultAndFinish(activityResult: Int, refusalResult: RefusalActResult)

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
