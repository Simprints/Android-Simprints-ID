package com.simprints.infra.events.event.domain.models.scope

import android.os.Build
import androidx.annotation.Keep

@Keep
data class Device(
    var androidSdkVersion: String = Build.VERSION.SDK_INT.toString(),
    var deviceModel: String = Build.MANUFACTURER + "_" + Build.MODEL,
    var deviceId: String = "",
    var deviceUsage: DeviceUsage? = null,
)
