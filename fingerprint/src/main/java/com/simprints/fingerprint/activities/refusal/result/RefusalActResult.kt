package com.simprints.fingerprint.activities.refusal.result

import android.os.Parcelable
import com.simprints.fingerprint.activities.ActResult
import com.simprints.fingerprint.data.domain.refusal.RefusalFormReason
import kotlinx.android.parcel.Parcelize

//If user taps back-back then nothing is submitted "reason" = null
@Parcelize
class RefusalActResult(val action: Action, val answer: Answer): ActResult, Parcelable {

    @Parcelize
    class Answer(val reason: RefusalFormReason = RefusalFormReason.OTHER, val optionalText: String = ""):Parcelable

    enum class Action {
        SUBMIT, SCAN_FINGERPRINTS
    }

    companion object {
        const val BUNDLE_KEY = "RefusalActResultBundleKey"
    }

}
