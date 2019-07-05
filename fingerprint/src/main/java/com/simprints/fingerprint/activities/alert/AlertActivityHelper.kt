package com.simprints.fingerprint.activities.alert

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.simprints.fingerprint.activities.alert.request.AlertActRequest
import com.simprints.fingerprint.data.domain.InternalConstants

object AlertActivityHelper {

    private fun buildAlertIntent(context: Context, alertType: FingerprintAlert) =
        Intent(context, AlertActivity::class.java).apply {
            putExtra(AlertActRequest.BUNDLE_KEY, AlertActRequest(alertType))
        }

    fun launchAlert(act: Activity, alertType: FingerprintAlert) {
        act.startActivityForResult(buildAlertIntent(act, alertType), InternalConstants.RequestIntents.ALERT_ACTIVITY_REQUEST)
    }
}
