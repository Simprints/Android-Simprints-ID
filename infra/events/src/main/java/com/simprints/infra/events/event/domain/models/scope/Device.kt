package com.simprints.infra.events.event.domain.models.scope

import android.os.Build
import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class Device(
    var androidSdkVersion: String = Build.VERSION.SDK_INT.toString(),
    var deviceModel: String = Build.MANUFACTURER + "_" + Build.MODEL,
    var deviceId: String = "",
)
