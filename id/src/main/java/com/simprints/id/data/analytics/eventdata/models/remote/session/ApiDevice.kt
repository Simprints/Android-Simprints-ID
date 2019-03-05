package com.simprints.id.data.analytics.eventdata.models.remote.session

import android.os.Build
import com.simprints.id.data.analytics.eventdata.models.domain.session.DatabaseInfo
import com.simprints.id.data.analytics.eventdata.models.domain.session.Device
import java.util.*

open class ApiDevice(
    var androidSdkVersion: String = Build.VERSION.SDK_INT.toString(),
    var deviceModel: String = Build.MANUFACTURER + "_" + Build.MODEL,
    var deviceId: String = "") {

    constructor(deviceId: Device) :
        this(deviceId.androidSdkVersion, deviceId.deviceModel, deviceId.deviceId)
}
