package com.simprints.fingerprint.tools.extensions

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import com.simprints.fingerprint.activities.alert.AlertActivity
import com.simprints.fingerprint.activities.alert.FingerprintAlert
import com.simprints.fingerprint.activities.alert.request.AlertActRequest
import com.simprints.fingerprint.activities.refusal.RefusalActivity
import com.simprints.fingerprint.data.domain.InternalConstants.RequestIntents.Companion.ALERT_ACTIVITY_REQUEST
import com.simprints.fingerprint.data.domain.InternalConstants.RequestIntents.Companion.REFUSAL_ACTIVITY_REQUEST

fun Activity.launchRefusalActivity(requestCode: Int = REFUSAL_ACTIVITY_REQUEST) {
    val intent = Intent(this, RefusalActivity::class.java)
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
