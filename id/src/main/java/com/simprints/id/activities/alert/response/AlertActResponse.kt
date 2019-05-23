package com.simprints.id.activities.alert.response

import android.os.Parcelable
import com.simprints.id.domain.alert.AlertType
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AlertActResponse(val alertType: AlertType): Parcelable {

    companion object {
        const val BUNDLE_KEY = "AlertActResponseBundleKey"
    }
}
