package com.simprints.fingerprint.activities.alert.result

import android.os.Parcelable
import com.simprints.fingerprint.activities.alert.AlertError
import com.simprints.fingerprint.orchestrator.task.TaskResult
import kotlinx.parcelize.Parcelize

/**
 * This class represents the result returned from an alert screen
 *
 * @property alert  the type of error that was shown
 */
@Parcelize
data class AlertTaskResult(
    val alert: AlertError,
): TaskResult, Parcelable {

    companion object {
        const val BUNDLE_KEY = "AlertActResponseBundleKey"
    }
}
