package com.simprints.id.data.db.session.domain.models.events

import androidx.annotation.Keep

@Keep
class Vero2InfoSnapshotEvent(startTime: Long,
                             val version: Vero2Version,
                             val battery: BatteryInfo): Event(EventType.VERO_2_INFO_SNAPSHOT, startTime) {

    @Keep
    class Vero2Version(
        val master: Long,
        val cypressApp: String,
        val cypressApi: String,
        val stmApp: String,
        val stmApi: String,
        val un20App: String,
        val un20Api: String
    )

    @Keep
    class BatteryInfo(
        val charge: Int,
        val voltage: Int,
        val current: Int,
        val temperature: Int
    )
}


