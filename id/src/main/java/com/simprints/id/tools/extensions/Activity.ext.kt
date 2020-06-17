package com.simprints.id.tools.extensions

import android.app.Activity
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.widget.Toast
import androidx.core.app.ActivityCompat.requestPermissions
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

fun Activity.hasPermission(permission: String): Boolean {
    return checkSelfPermission(permission) == PERMISSION_GRANTED
}

fun Activity.requestPermissionsIfRequired(permissions: List<String>, permissionsRequestCode: Int): Boolean {
    val permissionsToAsk = getNotGrantedPermissions(permissions)

    return if (permissionsToAsk.isNotEmpty()) {
        requestPermissions(this, permissionsToAsk.toTypedArray(), permissionsRequestCode)
        true
    } else {
        false
    }
}

fun Activity.getNotGrantedPermissions(permissions: List<String>) =
    permissions.filterNot { hasPermission(it) }
