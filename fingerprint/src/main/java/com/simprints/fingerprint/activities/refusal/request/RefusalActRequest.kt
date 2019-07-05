package com.simprints.fingerprint.activities.refusal.request

import android.os.Parcelable
import com.simprints.fingerprint.activities.ActRequest
import kotlinx.android.parcel.Parcelize

@Parcelize
class RefusalActRequest : ActRequest, Parcelable {

    companion object {
        const val BUNDLE_KEY = "RefusalResultKey"
    }
}
