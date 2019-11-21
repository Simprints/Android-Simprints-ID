package com.simprints.id.tools.extensions

import android.app.Activity
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat.checkSelfPermission
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

fun Activity.hideKeyboard() {
    val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
    var focusedView = currentFocus
    if (focusedView == null)
        focusedView = View(this)
    val flags = 0
    inputMethodManager.hideSoftInputFromWindow(focusedView.windowToken, flags)
}

fun Activity.requestPermissionsIfRequired(permissions: Array<String>, permissionsRequestCode: Int) {
    val permissionsToAsk = permissions.filter {
        checkSelfPermission(this, it) != PERMISSION_GRANTED
    }.toTypedArray()

    if (permissionsToAsk.isNotEmpty()) {
        requestPermissions(this, permissionsToAsk, permissionsRequestCode)
    }
}
