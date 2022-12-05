package com.simprints.id.tools.extensions

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings

// TODO: consider switching to an instance ID for privacy reasons (read https://developer.android.com/training/articles/user-data-ids.html)
val Context.deviceId: String
    @SuppressLint("HardwareIds")
    get() = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) ?: "no-device-id"

