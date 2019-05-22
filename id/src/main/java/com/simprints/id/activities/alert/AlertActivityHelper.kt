package com.simprints.id.activities.alert

import android.app.Activity
import android.content.Intent
import com.simprints.id.activities.alert.request.AlertActRequest
import com.simprints.id.activities.alert.response.AlertActResponse
import com.simprints.id.domain.alert.AlertType
import com.simprints.id.tools.InternalConstants

object AlertActivityHelper {

    fun launchAlert(act: Activity, alertType: AlertType) {
        val intent = Intent(act, AlertActivity::class.java)
        intent.putExtra(AlertActRequest.BUNDLE_KEY, AlertActRequest(alertType))
        act.startActivityForResult(intent, InternalConstants.RequestIntents.ALERT_ACTIVITY_REQUEST)
    }

    fun extractPotentialAlertScreenResponse(requestCode: Int, resultCode: Int, data: Intent?): AlertActResponse? =
        data?.getParcelableExtra(AlertActResponse.BUNDLE_KEY)
}
