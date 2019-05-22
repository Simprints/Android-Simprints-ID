package com.simprints.fingerprint.activities.alert

import android.app.Activity
import android.content.Intent
import com.simprints.fingerprint.activities.alert.request.AlertActRequest
import com.simprints.fingerprint.activities.alert.response.AlertActResponse
import com.simprints.fingerprint.activities.collect.models.Finger
import com.simprints.fingerprint.data.domain.InternalConstants

object AlertActivityHelper {

    fun launchAlert(act: Activity, alertType: FingerprintAlert) {
        val intent = Intent(act, AlertActivity::class.java)
        intent.putExtra(AlertActRequest.BUNDLE_KEY, AlertActRequest(alertType))
        act.startActivityForResult(intent, InternalConstants.RequestIntents.ALERT_ACTIVITY_REQUEST)
    }

    fun extractPotentialAlertScreenResponse(requestCode: Int, resultCode: Int, data: Intent?): AlertActResponse? =
        data?.getParcelableExtra(AlertActResponse.BUNDLE_KEY)
}
