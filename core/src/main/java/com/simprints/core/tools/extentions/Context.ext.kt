package com.simprints.core.tools.extentions

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.StringRes

fun Context.showToast(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, this.resources.getText(resId), duration).show()
}

// TODO: consider switching to an instance ID for privacy reasons (read https://developer.android.com/training/articles/user-data-ids.html)
val Context.deviceHardwareId: String
    @SuppressLint("HardwareIds")
    get() = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) ?: "no-device-id"

val Context.packageVersionName: String
    get() = try {
        packageManager.getPackageInfo(packageName, 0).versionName
    } catch (e: PackageManager.NameNotFoundException) {
        "Version Name Not Found"
    }
