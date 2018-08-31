package com.simprints.id.tools.extensions

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import com.simprints.id.activities.alert.AlertActivity
import com.simprints.id.activities.IntentKeys
import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.tools.InternalConstants

val Activity.activityName: String
    get() =
        try {
            packageManager.getActivityInfo(componentName, 0).name
        } catch (e: PackageManager.NameNotFoundException) {
            "Class activityName not found"
        }

fun Activity.launchAlert(alertType: ALERT_TYPE, requestCode: Int = InternalConstants.ALERT_ACTIVITY_REQUEST) {
    val intent = Intent(this, AlertActivity::class.java)
    intent.putExtra(IntentKeys.alertActivityAlertTypeKey, alertType)
    startActivityForResult(intent, requestCode)
}

fun Activity.runOnUiThreadIfStillRunning(then: () -> Unit) {
    runOnUiThreadIfStillRunning(then, {})
}

fun Activity.runOnUiThreadIfStillRunning(then: () -> Unit, otherwise: () -> Unit) {
    if (!isFinishing) {
        this.runOnUiThread { then() }
    } else {
        otherwise()
    }
}

fun Activity.showToast(stringRes: Int) =
    runOnUiThread {
        Toast.makeText(this, stringRes, Toast.LENGTH_LONG).show()
    }
