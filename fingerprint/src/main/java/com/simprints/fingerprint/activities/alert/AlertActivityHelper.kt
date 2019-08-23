package com.simprints.fingerprint.activities.alert

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.simprints.fingerprint.activities.alert.request.AlertTaskRequest
import com.simprints.fingerprint.orchestrator.domain.RequestCode

object AlertActivityHelper {

    private fun buildAlertIntent(context: Context, alertType: FingerprintAlert) =
        Intent(context, AlertActivity::class.java).apply {
            putExtra(AlertTaskRequest.BUNDLE_KEY, AlertTaskRequest(alertType))
        }

    fun launchAlert(act: Activity, alertType: FingerprintAlert) {
        act.startActivityForResult(buildAlertIntent(act, alertType), RequestCode.ALERT.value)
    }
}
