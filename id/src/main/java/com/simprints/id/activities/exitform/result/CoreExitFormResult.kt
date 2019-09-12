package com.simprints.id.activities.exitform.result

import android.os.Parcelable
import com.simprints.id.data.exitform.ExitFormReason
import kotlinx.android.parcel.Parcelize

@Parcelize
class CoreExitFormResult(val action: Action, val answer: Answer) : Parcelable {

    @Parcelize
    class Answer(val reason: ExitFormReason = ExitFormReason.OTHER, val optionalText: String = "") : Parcelable

    enum class Action {
        SUBMIT, GO_BACK
    }

    companion object {
        const val BUNDLE_KEY = "ExitActResultBundleKey"
    }
}
