package com.simprints.fingerprint.activities.alert.request

import android.os.Parcelable
import com.simprints.fingerprint.activities.alert.FingerprintAlert
import kotlinx.android.parcel.Parcelize

@Parcelize
class AlertActRequest(val alert: FingerprintAlert): Parcelable {
    companion object {
        const val BUNDLE_KEY = "AlertActRequestBundleKey"
    }
}
