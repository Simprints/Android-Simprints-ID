package com.simprints.id.testUtils.roboletric

import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import org.robolectric.Shadows

inline fun injectHowToResolveScannerAppIntent(pm: PackageManager): ResolveInfo {

    // Pretend that ScannerQR app is installed
    val spm = Shadows.shadowOf(pm)
    val info = ResolveInfo()
    info.isDefault = true
    val applicationInfo = ApplicationInfo()
    applicationInfo.packageName = "com.google.zxing.client.android"
    applicationInfo.className = "com.google.zxing.client.android.CaptureActivity"
    info.activityInfo = ActivityInfo()
    info.activityInfo.applicationInfo = applicationInfo
    info.activityInfo.name = "Barcode Scanner"
    return info
}
