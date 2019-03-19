package com.simprints.id.data.analytics.eventdata.models.domain.session

import android.os.Build
import java.util.*

open class Device(
    var androidSdkVersion: String = Build.VERSION.SDK_INT.toString(),
    var deviceModel: String = Build.MANUFACTURER + "_" + Build.MODEL,
    var deviceId: String = "",
    var id: String = UUID.randomUUID().toString())
