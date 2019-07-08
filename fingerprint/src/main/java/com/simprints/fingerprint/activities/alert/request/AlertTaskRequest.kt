package com.simprints.fingerprint.activities.alert.request

import android.os.Parcelable
import com.simprints.fingerprint.orchestrator.task.TaskRequest
import com.simprints.fingerprint.activities.alert.FingerprintAlert
import kotlinx.android.parcel.Parcelize

@Parcelize
class AlertTaskRequest(val alert: FingerprintAlert): TaskRequest, Parcelable {
    companion object {
        const val BUNDLE_KEY = "AlertActRequestBundleKey"
    }
}
