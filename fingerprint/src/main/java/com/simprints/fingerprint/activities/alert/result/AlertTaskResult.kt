package com.simprints.fingerprint.activities.alert.result

import android.os.Parcelable
import com.simprints.fingerprint.orchestrator.task.TaskResult
import com.simprints.fingerprint.activities.alert.FingerprintAlert
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AlertTaskResult(val alert: FingerprintAlert,
                           val closeButtonAction: CloseButtonAction): TaskResult, Parcelable {

    companion object {
        const val BUNDLE_KEY = "AlertActResponseBundleKey"
    }

    enum class CloseButtonAction {
        CLOSE,
        TRY_AGAIN,
        BACK
    }
}
