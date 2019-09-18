package com.simprints.id.activities.fingerprintexitform.result

import android.os.Parcelable
import com.simprints.id.data.exitform.FingerprintExitFormReason
import kotlinx.android.parcel.Parcelize

@Parcelize
class FingerprintExitFormResult(val action: Action, val answer: Answer) : Parcelable {

    @Parcelize
    class Answer(val reason: FingerprintExitFormReason = FingerprintExitFormReason.OTHER,
                 val optionalText: String = "") : Parcelable

    enum class Action {
        SCAN_FINGERPRINTS, SUBMIT
    }

    companion object {
        const val FINGERPRINT_EXIT_FORM_BUNDLE_KEY = "FingerprintExitActResultBundleKey"
        const val FINGERPRINT_EXIT_FORM_RESULT_CODE_SUBMIT = 301
        const val FINGERPRINT_EXIT_FORM_RESULT_CODE_SCAN_FINGERPRINTS = 302
    }
}
