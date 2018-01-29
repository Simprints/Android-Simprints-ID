package com.simprints.id.tools.extensions

import android.content.Intent
import android.content.pm.PackageManager

/**
 * Created by fabiotuzza on 16/01/2018.
 */

// If the app (packageName) was installed manually, then the getInstallerPackageName is null.
// we should check for getInstallerPackageName == com.android.vending, but it's not
// guaranteed it won't change in the future.
fun PackageManager.isAppInstallerKnown(packageName: String): Boolean {
    try {
        return this.getInstallerPackageName(packageName) != null
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {

        // Android doesn't recognise the packageName. We loosely pretend
        // packageName comes from the Google Play Store
        return true
    }
}

fun PackageManager.scannerAppIntent(): Intent {
    val intent = Intent("com.google.zxing.client.android.SCAN")
    intent.putExtra("SAVE_HISTORY", false)
    intent.putExtra("SCAN_MODE", "QR_CODE_MODE")
    return intent
}
