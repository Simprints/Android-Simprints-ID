package com.simprints.fingerprint.activities.refusal

import com.simprints.fingerprint.activities.BasePresenter
import com.simprints.fingerprint.activities.BaseView
import com.simprints.fingerprint.data.domain.refusal.RefusalActResult

interface RefusalContract {

    interface View : BaseView<Presenter> {

        fun setResultAndFinish(activityResult: Int, refusalResult: RefusalActResult)

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

        fun handleSickRadioClick()

        fun handlePregnantRadioClick()

        fun handleDoesNotHavePermissionRadioClick()

        fun handlePersonNotPresentRadioClick()

        fun handleAppNotWorkingRadioClick()

        fun handleOtherRadioOptionClick()

        fun handleRadioOptionCheckedChange()

        fun handleOnBackPressed()
    }
}
