package com.simprints.clientapi.activities.errors.request

import android.os.Parcelable
import com.simprints.clientapi.activities.errors.ClientApiAlert
import kotlinx.android.parcel.Parcelize

@Parcelize
class AlertActRequest(val clientApiAlert: ClientApiAlert): Parcelable {
    companion object {
        const val BUNDLE_KEY = "AlertActRequestBundleKey"
    }
}
