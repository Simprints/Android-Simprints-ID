package com.simprints.fingerprint.activities.alert.request

import android.os.Parcelable
import com.simprints.fingerprint.activities.alert.FingerprintAlert
import com.simprints.fingerprint.orchestrator.task.TaskRequest
import kotlinx.parcelize.Parcelize

/**
 * This class represents a request to present the alert screen to the user, displaying whatever
 * fingerprint error occurred while processing the fingerprint request.
 *
 * @property alert  the error that occurred
 */
@Parcelize
class AlertTaskRequest(val alert: FingerprintAlert): TaskRequest, Parcelable {
    companion object {
        const val BUNDLE_KEY = "AlertActRequestBundleKey"
    }
}
