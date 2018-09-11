package com.simprints.id.data.analytics.eventData.models.session

import android.os.Build
import com.simprints.id.tools.json.SkipSerialisationField
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class Device(
    var androidSdkVersion: String = Build.VERSION.SDK_INT.toString(),
    var deviceModel: String = Build.MANUFACTURER + "_" + Build.MODEL,
    var deviceId: String = "",
    @PrimaryKey
    @SkipSerialisationField
    var id: String = UUID.randomUUID().toString()) : RealmObject()
