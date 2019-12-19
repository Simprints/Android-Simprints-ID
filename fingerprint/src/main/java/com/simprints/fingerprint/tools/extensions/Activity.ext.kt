package com.simprints.fingerprint.tools.extensions

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import com.simprints.fingerprint.activities.refusal.RefusalActivity
import com.simprints.fingerprint.orchestrator.domain.RequestCode
import timber.log.Timber

fun Activity.launchRefusalActivity(requestCode: Int = RequestCode.REFUSAL.value) {
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

fun Activity.showToast(string: String) =
    runOnUiThread {
        Toast.makeText(this, string, Toast.LENGTH_LONG).show()
    }

fun Activity.logActivityCreated() {
    Timber.d("Fingerprint Activity Log : created ${this::class.simpleName}")
}

fun Activity.logActivityDestroyed() {
    Timber.d("Fingerprint Activity Log : destroyed ${this::class.simpleName}")
}
