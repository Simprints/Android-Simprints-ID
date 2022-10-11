
package com.simprints.core.tools.extentions
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.app.ActivityCompat

fun Activity.hideKeyboard() {
    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    var focusedView = currentFocus
    if (focusedView == null)
        focusedView = View(this)
    val flags = 0
    inputMethodManager.hideSoftInputFromWindow(focusedView.windowToken, flags)
}

fun Activity.removeAnimationsToNextActivity() =
    overridePendingTransition(0, 0)

fun Activity.requestPermissionsIfRequired(permissions: List<String>, permissionsRequestCode: Int): Boolean {
    val permissionsToAsk = getNotGrantedPermissions(permissions)

    return if (permissionsToAsk.isNotEmpty()) {
        ActivityCompat.requestPermissions(
            this,
            permissionsToAsk.toTypedArray(),
            permissionsRequestCode
        )
        true
    } else {
        false
    }
}

fun Activity.getNotGrantedPermissions(permissions: List<String>) =
    permissions.filterNot { hasPermission(it) }

fun Activity.hasPermission(permission: String): Boolean {
    return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
}
