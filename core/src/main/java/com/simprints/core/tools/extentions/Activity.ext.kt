package com.simprints.core.tools.extentions

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.app.ActivityCompat
import com.simprints.core.domain.permission.PermissionStatus

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

fun Activity.hasPermission(permission: String): Boolean {
    return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
}

fun Activity.hasPermissions(permissions: Array<String>): Boolean = permissions.all(::hasPermission)

fun Activity.permissionFromResult(permission: String, grantResult: Boolean): PermissionStatus =
    when (grantResult) {
        true -> PermissionStatus.Granted
        else -> {
            val shouldShowRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
            when (shouldShowRationale) {
                true -> PermissionStatus.Denied
                false -> PermissionStatus.DeniedNeverAskAgain
            }
        }
    }
