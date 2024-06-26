package com.simprints.core.tools.extentions

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports

// TODO: consider switching to an instance ID for privacy reasons (read https://developer.android.com/training/articles/user-data-ids.html)
@ExcludedFromGeneratedTestCoverageReports("UI code")
val Context.deviceHardwareId: String
    @SuppressLint("HardwareIds")
    get() = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) ?: "no-device-id"

@ExcludedFromGeneratedTestCoverageReports("UI code")
val Context.packageVersionName: String
    get() = try {
        packageManager.getPackageInfo(packageName, 0).versionName ?: ""
    } catch (e: PackageManager.NameNotFoundException) {
        "Version Name Not Found"
    }
