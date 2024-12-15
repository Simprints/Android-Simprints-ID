package com.simprints.core.tools.extentions

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.app.ActivityCompat
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.permission.PermissionStatus

@ExcludedFromGeneratedTestCoverageReports("UI code")
fun Activity.hideKeyboard() {
    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    var focusedView = currentFocus
    if (focusedView == null) {
        focusedView = View(this)
    }
    val flags = 0
    inputMethodManager.hideSoftInputFromWindow(focusedView.windowToken, flags)
}

@ExcludedFromGeneratedTestCoverageReports("UI code")
fun Activity.hasPermission(permission: String): Boolean = checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

@ExcludedFromGeneratedTestCoverageReports("UI code")
fun Activity.hasPermissions(permissions: Array<String>): Boolean = permissions.all(::hasPermission)

@ExcludedFromGeneratedTestCoverageReports("UI code")
fun Activity.permissionFromResult(
    permission: String,
    grantResult: Boolean,
): PermissionStatus = when (grantResult) {
    true -> PermissionStatus.Granted
    else -> {
        when (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            true -> PermissionStatus.Denied
            false -> PermissionStatus.DeniedNeverAskAgain
        }
    }
}
