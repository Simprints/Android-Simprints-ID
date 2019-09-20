package com.simprints.id.activities.coreexitform.result

import android.os.Parcelable
import com.simprints.id.data.exitform.CoreExitFormReason
import kotlinx.android.parcel.Parcelize

@Parcelize
class CoreExitFormResult(val action: Action, val answer: Answer) : Parcelable {

    @Parcelize
    class Answer(val reason: CoreExitFormReason = CoreExitFormReason.OTHER, val optionalText: String = "") : Parcelable

    enum class Action {
        SUBMIT, GO_BACK
    }

    companion object {
        const val BUNDLE_KEY = "ExitActResultBundleKey"
        const val CORE_EXIT_FORM_RESULT_CODE_SUBMIT = 201
        const val CORE_EXIT_FORM_RESULT_CODE_GO_BACK = 202
    }
}
