package com.simprints.id.activities.alert.request

import android.os.Parcelable
import com.simprints.id.domain.alert.AlertType
import kotlinx.android.parcel.Parcelize

@Parcelize
class AlertActRequest(val alertType: AlertType): Parcelable {
    companion object {
        const val BUNDLE_KEY = "AlertActRequestBundleKey"
    }
}
