package com.simprints.fingerprint.activities.refusal

import com.simprints.fingerprint.activities.base.BasePresenter
import com.simprints.fingerprint.activities.base.BaseView
import com.simprints.fingerprint.activities.refusal.result.RefusalTaskResult

interface RefusalContract {

    interface View : BaseView<Presenter> {

        fun setResultAndFinish(activityResult: Int, refusalResult: RefusalTaskResult)

        fun scrollToBottom()

        fun enableSubmitButton()

        fun enableRefusalText()

        fun setFocusOnRefusalReasonAndDisableSubmit()

        fun isSubmitButtonEnabled(): Boolean

        fun showToastForFormSubmit()

        fun showToastForSelectOptionAndSubmit()

        fun disableSubmitButton()
    }

    interface Presenter : BasePresenter {

        fun handleSubmitButtonClick(refusalText: String)

        fun handleScanFingerprintsClick()

        fun handleLayoutChange()

        fun handleChangesInRefusalText(refusalText: String)

        fun handleReligiousConcernsRadioClick()

        fun handleDataConcernsRadioClick()

        fun handleTooYoungRadioClick()

        fun handleDoesNotHavePermissionRadioClick()

        fun handlePersonNotPresentRadioClick()

        fun handleAppNotWorkingRadioClick()

        fun handleOtherRadioOptionClick()

        fun handleRadioOptionCheckedChange()

        fun handleOnBackPressed()
    }
}
