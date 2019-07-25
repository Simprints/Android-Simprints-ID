package com.simprints.id.testtools

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.test.rule.ActivityTestRule
import com.schibsted.spain.barista.interaction.PermissionGranter
import com.simprints.testtools.android.runActivityOnUiThread
import java.util.*

private val permissions = ArrayList(Arrays.asList(
    Manifest.permission.ACCESS_NETWORK_STATE,
    Manifest.permission.BLUETOOTH,
    Manifest.permission.INTERNET,
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.RECEIVE_BOOT_COMPLETED,
    Manifest.permission.WAKE_LOCK,
    Manifest.permission.VIBRATE
))

fun ActivityTestRule<*>.launchActivityAndRunOnUiThread(intent: Intent?) {
    launchActivity(intent)
    runActivityOnUiThread(this)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) grantPermissions()
}

fun grantPermissions() {
    // Allow all first-app permissions and dismiss the dialog box
    //log("ActivityUtils.grantPermissions(): granting permissions")
    permissions.forEach { PermissionGranter.allowPermissionsIfNeeded(it) }
}
