package com.simprints.id.tools

import android.app.Activity
import android.content.Intent
import com.simprints.id.activities.AlertActivity
import com.simprints.id.activities.IntentKeys
import com.simprints.id.domain.ALERT_TYPE

class AlertLauncher(private val activity: Activity) {

    fun launch(alertType: ALERT_TYPE, requestCode: Int) {
        val intent = Intent(activity, AlertActivity::class.java)
        intent.putExtra(IntentKeys.alertActivityAlertTypeKey, alertType)
        activity.startActivityForResult(intent, requestCode)
    }
}
