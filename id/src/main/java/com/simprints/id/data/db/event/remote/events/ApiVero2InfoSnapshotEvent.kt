package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.Vero2InfoSnapshotEvent
import com.simprints.id.data.db.event.domain.events.Vero2InfoSnapshotEvent.Vero2InfoSnapshotPayload
import com.simprints.id.data.db.event.domain.events.Vero2InfoSnapshotEvent.Vero2InfoSnapshotPayload.BatteryInfo
import com.simprints.id.data.db.event.domain.events.Vero2InfoSnapshotEvent.Vero2InfoSnapshotPayload.Vero2Version
import com.simprints.id.data.db.session.remote.events.ApiEvent

@Keep
class ApiVero2InfoSnapshotEvent(val relativeStartTime: Long,
                                val version: ApiVero2Version,
                                val battery: ApiBatteryInfo) : ApiEvent(ApiEventType.VERO_2_INFO_SNAPSHOT) {

    @Keep
    class ApiVero2Version(val master: Long,
                          val cypressApp: String,
                          val cypressApi: String,
                          val stmApp: String,
                          val stmApi: String,
                          val un20App: String,
                          val un20Api: String) {

        constructor(vero2Version: Vero2Version) :
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

        constructor(batteryInfo: BatteryInfo) :
            this(batteryInfo.charge, batteryInfo.voltage, batteryInfo.current, batteryInfo.temperature)
    }

    constructor(vero2InfoSnapshotEvent: Vero2InfoSnapshotEvent) :
        this((vero2InfoSnapshotEvent.payload as Vero2InfoSnapshotPayload).relativeStartTime ?: 0,
            ApiVero2Version(vero2InfoSnapshotEvent.payload.version),
            ApiBatteryInfo(vero2InfoSnapshotEvent.payload.battery))
}
