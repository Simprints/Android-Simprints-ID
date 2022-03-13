package com.simprints.eventsystem.event.remote.models

import androidx.annotation.Keep
import com.simprints.eventsystem.event.domain.models.Vero2InfoSnapshotEvent.Vero2InfoSnapshotPayload
import com.simprints.eventsystem.event.domain.models.Vero2InfoSnapshotEvent.Vero2InfoSnapshotPayload.BatteryInfo
import com.simprints.eventsystem.event.domain.models.Vero2InfoSnapshotEvent.Vero2InfoSnapshotPayload.Vero2Version


@Keep
data class ApiVero2InfoSnapshotPayload(override val startTime: Long,
                                       override val version: Int,
                                       val scannerVersion: ApiVero2Version,
                                       val battery: ApiBatteryInfo) : ApiEventPayload(ApiEventPayloadType.Vero2InfoSnapshot, version, startTime) {

    @Keep
    data class ApiVero2Version(val hardwareVersion: String,
                               val cypressApp: String,
                               val stmApp: String,
                               val un20App: String) {

        constructor(vero2Version: Vero2Version) :
            this(vero2Version.hardwareVersion,
                vero2Version.cypressApp,
                vero2Version.stmApp,
                vero2Version.un20App)
    }

    @Keep
    data class ApiBatteryInfo(val charge: Int, val voltage: Int,
                              val current: Int, val temperature: Int) {

        constructor(batteryInfo: BatteryInfo) :
            this(batteryInfo.charge, batteryInfo.voltage, batteryInfo.current, batteryInfo.temperature)
    }

    constructor(domainPayload: Vero2InfoSnapshotPayload) :
        this(domainPayload.createdAt,
            domainPayload.eventVersion,
            ApiVero2Version(domainPayload.version),
            ApiBatteryInfo(domainPayload.battery))
}
