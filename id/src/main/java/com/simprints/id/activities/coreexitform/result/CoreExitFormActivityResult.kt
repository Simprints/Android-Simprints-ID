package com.simprints.id.activities.coreexitform.result

import android.os.Parcelable
import com.simprints.id.data.exitform.CoreExitFormReason
import com.simprints.id.exitformhandler.ExitFormResult
import kotlinx.android.parcel.Parcelize

@Parcelize
class CoreExitFormActivityResult(val action: Action, val answer: Answer) :
    Parcelable, ExitFormResult(ExitFormType.CORE_EXIT_FORM) {

    @Parcelize
    class Answer(val reason: CoreExitFormReason = CoreExitFormReason.OTHER, val optionalText: String = "") : Parcelable

    enum class Action {
        SUBMIT, GO_BACK
    }
}
