package com.simprints.fingerprint.activities.alert.response

import android.os.Parcelable
import com.simprints.fingerprint.activities.alert.FingerprintAlert
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AlertActResult(val alert: FingerprintAlert,
                          val closeButtonAction: CloseButtonAction): Parcelable {

    companion object {
        const val BUNDLE_KEY = "AlertActResponseBundleKey"
    }

    enum class CloseButtonAction {
        CLOSE,
        TRY_AGAIN
    }
}
