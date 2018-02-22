package com.simprints.id.tools.extensions

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import com.simprints.id.activities.AlertActivity
import com.simprints.id.activities.IntentKeys
import com.simprints.id.model.ALERT_TYPE
import com.simprints.id.tools.InternalConstants

val Activity.activityName: String
    get() =
        try {
            packageManager.getActivityInfo(componentName, 0).name
        } catch (e: PackageManager.NameNotFoundException) {
            "Class activityName not found"
        }

fun Activity.launchAlert(alertType: ALERT_TYPE) {
    val intent = Intent(this, AlertActivity::class.java)
    intent.putExtra(IntentKeys.alertActivityAlertTypeKey, alertType)
    startActivityForResult(intent, InternalConstants.ALERT_ACTIVITY_REQUEST)
}

fun Activity.ifStillRunning(block: () -> Unit) {
    if (!isFinishing) {
        block()
    }
}
