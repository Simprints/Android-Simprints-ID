package com.simprints.infra.eventsync.event.remote.models.session

import android.os.Build
import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.scope.Device

@Keep
internal data class ApiDevice(
    var androidSdkVersion: String = Build.VERSION.SDK_INT.toString(),
    var model: String = Build.MANUFACTURER + "_" + Build.MODEL,
    var id: String = "",
)

internal fun Device.fromDomainToApi() = ApiDevice(androidSdkVersion, deviceModel, deviceId)
