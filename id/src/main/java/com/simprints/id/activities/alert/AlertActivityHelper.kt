package com.simprints.id.activities.alert

import android.app.Activity
import android.content.Intent
import com.simprints.clientapi.activities.errors.AlertType
import com.simprints.clientapi.activities.errors.ErrorActivity
import com.simprints.clientapi.activities.errors.request.AlertActRequest
import com.simprints.clientapi.activities.errors.response.AlertActResponse

object AlertActivityHelper {

    fun launchAlert(act: Activity, alertType: AlertType) {
        val intent = Intent(act, ErrorActivity::class.java)
        intent.putExtra(AlertActRequest.BUNDLE_KEY, AlertActRequest(alertType))
        act.startActivityForResult(intent, AlertActRequest.ALERT_SCREEN_REQUEST_CODE)
    }

    fun extractPotentialAlertScreenResponse(requestCode: Int, resultCode: Int, data: Intent?): AlertActResponse? =
        data?.getParcelableExtra(AlertActResponse.BUNDLE_KEY)
}
