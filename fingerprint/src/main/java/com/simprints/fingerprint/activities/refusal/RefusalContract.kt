package com.simprints.fingerprint.activities.refusal

import com.simprints.fingerprint.activities.BasePresenter
import com.simprints.fingerprint.activities.BaseView
import com.simprints.fingerprint.data.domain.alert.FingerprintAlert
import com.simprints.fingerprint.data.domain.refusal.RefusalActResult

interface RefusalContract {

    interface View : BaseView<Presenter> {

        fun doLaunchAlert(alert: FingerprintAlert)

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
