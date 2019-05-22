package com.simprints.id.activities.alert.response

import android.os.Parcelable
import com.simprints.id.domain.alert.AlertType
import kotlinx.android.parcel.Parcelize

@Parcelize
class AlertActResponse(val alertType: AlertType): Parcelable {

    companion object {
        const val ALERT_SCREEN_RESPONSE_CODE_OK = 201
        const val BUNDLE_KEY = "AlertActResponseBundleKey"
    }
}
