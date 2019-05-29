package com.simprints.clientapi.routers

import android.app.Activity
import android.content.Intent
import com.simprints.clientapi.activities.errors.ClientApiAlert
import com.simprints.clientapi.activities.errors.ErrorActivity
import com.simprints.clientapi.activities.errors.request.AlertActRequest
import com.simprints.clientapi.activities.errors.response.AlertActResponse
import com.simprints.id.tools.InternalConstants

object ClientRequestErrorRouter {

    fun launchAlert(act: Activity, clientApiAlert: ClientApiAlert) {
        val intent = Intent(act, ErrorActivity::class.java)
        intent.putExtra(AlertActRequest.BUNDLE_KEY, AlertActRequest(clientApiAlert))
        act.startActivityForResult(intent, InternalConstants.RequestIntents.ALERT_ACTIVITY_REQUEST)
    }

    fun extractPotentialAlertScreenResponse(data: Intent?): AlertActResponse? =
        data?.getParcelableExtra(AlertActResponse.BUNDLE_KEY)

}
