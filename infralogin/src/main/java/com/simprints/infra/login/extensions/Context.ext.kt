package com.simprints.infra.login.extensions

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings

internal val Context.deviceId: String
    @SuppressLint("HardwareIds")
    get() = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) ?: "no-device-id"

internal val Context.packageVersionName: String
    get() = try {
        packageManager.getPackageInfo(packageName, 0).versionName
    } catch (e: PackageManager.NameNotFoundException) {
        "Version Name Not Found"
    }
