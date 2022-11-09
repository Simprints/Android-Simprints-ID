package com.simprints.fingerprint.activities.alert.result

import android.os.Parcelable
import com.simprints.fingerprint.activities.alert.FingerprintAlert
import com.simprints.fingerprint.activities.alert.result.AlertTaskResult.CloseButtonAction
import com.simprints.fingerprint.orchestrator.task.TaskResult
import kotlinx.parcelize.Parcelize

/**
 * This class represents the result returned from an alert screen
 *
 * @property alert  the type of error that was shown
 * @property closeButtonAction  the type of action [CloseButtonAction], that should be performed
 *                              after the alert screen was closed
 */
@Parcelize
data class AlertTaskResult(
    val alert: FingerprintAlert,
    val closeButtonAction: CloseButtonAction
): TaskResult, Parcelable {

    companion object {
        const val BUNDLE_KEY = "AlertActResponseBundleKey"
    }

    enum class CloseButtonAction {
        CLOSE,
        TRY_AGAIN,
        BACK
    }
}
