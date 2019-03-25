package com.simprints.fingerprint.data.domain.refusal

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

//If user taps back-back then nothing is submitted "reason" = null
@Parcelize
class RefusalActResult(val reason: RefusalFormReason?, val optionalText: String = ""): Parcelable {

    companion object {
        const val BUNDLE_KEY = "RefusalActResultBundleKey"
    }

}
