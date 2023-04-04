package com.simprints.infra.events.remote.models.session

import android.os.Build
import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.session.Device

@Keep
data class ApiDevice(
    var androidSdkVersion: String = Build.VERSION.SDK_INT.toString(),
    var deviceModel: String = Build.MANUFACTURER + "_" + Build.MODEL,
    var deviceId: String = "") {

    constructor(deviceId: Device) :
        this(deviceId.androidSdkVersion, deviceId.deviceModel, deviceId.deviceId)
}

fun Device.fromDomainToApi() =
    ApiDevice(androidSdkVersion, deviceModel, deviceId)