package com.simprints.id.activities.fingerprintexitform.result

import android.os.Parcelable
import com.simprints.id.data.exitform.FingerprintExitFormReason
import com.simprints.id.exitformhandler.ExitFormResult
import kotlinx.android.parcel.Parcelize

@Parcelize
class FingerprintExitFormActivityResult(val action: Action, val answer: Answer) :
    Parcelable, ExitFormResult(ExitFormType.CORE_FINGERPRINT_EXIT_FROM) {

    @Parcelize
    class Answer(val reason: FingerprintExitFormReason = FingerprintExitFormReason.OTHER,
                 val optionalText: String = "") : Parcelable

    enum class Action {
        SCAN_FINGERPRINTS, SUBMIT
    }
}
