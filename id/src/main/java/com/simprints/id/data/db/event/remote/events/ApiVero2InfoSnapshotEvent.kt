package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.Vero2InfoSnapshotEvent

@Keep
class ApiVero2InfoSnapshotEvent(val relativeStartTime: Long,
                                val version: ApiVero2Version,
                                val battery: ApiBatteryInfo): ApiEvent(ApiEventType.VERO_2_INFO_SNAPSHOT) {

    @Keep
    class ApiVero2Version(val master: Long,
                          val cypressApp: String,
                          val cypressApi: String,
                          val stmApp: String,
                          val stmApi: String,
                          val un20App: String,
                          val un20Api: String) {

        constructor(vero2Version: Vero2InfoSnapshotEvent.Vero2Version):
            this(vero2Version.master,
                vero2Version.cypressApp,
                vero2Version.cypressApi,
                vero2Version.stmApp,
                vero2Version.stmApi,
                vero2Version.un20App,
                vero2Version.un20Api)
    }

    @Keep
    class ApiBatteryInfo(val charge: Int, val voltage: Int,
                         val current: Int, val temperature: Int) {

        constructor(batteryInfo: Vero2InfoSnapshotEvent.BatteryInfo):
            this(batteryInfo.charge, batteryInfo.voltage, batteryInfo.current, batteryInfo.temperature)
    }

    constructor(vero2InfoSnapshotEvent: Vero2InfoSnapshotEvent) :
        this(vero2InfoSnapshotEvent.relativeStartTime ?: 0,
            ApiVero2Version(vero2InfoSnapshotEvent.version),
            ApiBatteryInfo(vero2InfoSnapshotEvent.battery))
}
