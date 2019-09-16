package com.simprints.id.activities.faceexitform.result

import android.os.Parcelable
import com.simprints.id.data.exitform.FaceExitFormReason
import kotlinx.android.parcel.Parcelize


@Parcelize
class FaceExitFormResult(val action: Action, val answer: Answer) : Parcelable {

    @Parcelize
    class Answer(val reason: FaceExitFormReason = FaceExitFormReason.OTHER, val optionalText: String = "") : Parcelable

    enum class Action {
        SUBMIT, GO_BACK
    }

    companion object {
        const val FACE_EXIT_FORM_BUNDLE_KEY = "FaceExitActResultBundleKey"
        const val FACE_EXIT_FORM_RESULT_CODE_SUBMIT = 401
        const val FACE_EXIT_FORM_RESULT_CODE_GO_BACK = 402
    }
}
