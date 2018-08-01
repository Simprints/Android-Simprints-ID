package com.simprints.id.data.analytics

import android.os.Build
import io.realm.RealmObject

open class Device(
    var androidSdkVersion: String = Build.VERSION.SDK_INT.toString(),
    var deviceModel: String = Build.MANUFACTURER + "_" + Build.MODEL,
    var deviceId: String = "") : RealmObject()
