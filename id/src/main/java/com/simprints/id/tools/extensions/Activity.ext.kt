package com.simprints.id.tools.extensions

import android.app.Activity
import android.widget.Toast
import com.simprints.id.tools.AndroidResourcesHelper

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

fun Activity.showToast(androidResourcesHelper: AndroidResourcesHelper, stringRes: Int) =
    runOnUiThread {
        Toast.makeText(this, androidResourcesHelper.getString(stringRes), Toast.LENGTH_LONG).show()
    }
