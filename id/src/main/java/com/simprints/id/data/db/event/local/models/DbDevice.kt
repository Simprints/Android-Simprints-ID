package com.simprints.id.data.db.event.local.models

import android.os.Build
import com.simprints.id.data.db.event.domain.events.session.Device
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class DbDevice : RealmObject {

    @PrimaryKey
    lateinit var id: String

    var androidSdkVersion: String = Build.VERSION.SDK_INT.toString()
    var deviceModel: String = Build.MANUFACTURER + "_" + Build.MODEL
    var deviceId: String = ""

    constructor()
    constructor(device: Device): this() {
        id = device.id
        androidSdkVersion = device.androidSdkVersion
        deviceModel = device.deviceModel
        deviceId = device.deviceId
    }
}

fun DbDevice.toDomainDevice(): Device = Device(androidSdkVersion, deviceModel, deviceId, id)
