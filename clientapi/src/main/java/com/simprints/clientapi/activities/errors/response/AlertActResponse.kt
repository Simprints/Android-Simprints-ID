package com.simprints.clientapi.activities.errors.response

import android.os.Parcelable
import com.simprints.clientapi.activities.errors.ClientApiAlert
import kotlinx.android.parcel.Parcelize

@Parcelize
class AlertActResponse(val clientApiAlert: ClientApiAlert): Parcelable {

    companion object {
        const val BUNDLE_KEY = "AlertActResponseBundleKey"
    }
}
