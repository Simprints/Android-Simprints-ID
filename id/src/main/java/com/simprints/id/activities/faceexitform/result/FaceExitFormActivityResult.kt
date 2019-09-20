package com.simprints.id.activities.faceexitform.result

import android.os.Parcelable
import com.simprints.id.data.exitform.FaceExitFormReason
import com.simprints.id.exitformhandler.ExitFormResult
import kotlinx.android.parcel.Parcelize


@Parcelize
class FaceExitFormActivityResult(val action: Action, val answer: Answer) :
    Parcelable, ExitFormResult(ExitFormType.CORE_FACE_EXIT_FORM) {

    @Parcelize
    class Answer(val reason: FaceExitFormReason = FaceExitFormReason.OTHER, val optionalText: String = "") : Parcelable

    enum class Action {
        SUBMIT, GO_BACK
    }
}
