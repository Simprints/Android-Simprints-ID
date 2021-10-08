package com.simprints.id.activities.alert.response

import android.os.Parcelable
import com.simprints.id.domain.alert.AlertType
import kotlinx.parcelize.Parcelize

@Parcelize
data class AlertActResponse(val alertType: AlertType, val buttonAction: ButtonAction): Parcelable {

    enum class ButtonAction {
        CLOSE,
        TRY_AGAIN
    }

    companion object {
        const val BUNDLE_KEY = "AlertActResponseBundleKey"
    }
}
