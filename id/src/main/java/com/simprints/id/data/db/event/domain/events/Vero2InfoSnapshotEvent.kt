package com.simprints.id.data.db.event.domain.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.Vero2InfoSnapshotEvent.Vero2InfoSnapshotPayload.BatteryInfo
import com.simprints.id.data.db.event.domain.events.Vero2InfoSnapshotEvent.Vero2InfoSnapshotPayload.Vero2Version
import java.util.*

@Keep
class Vero2InfoSnapshotEvent(
    createdAt: Long,
    version: Vero2Version,
    battery: BatteryInfo,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    DEFAULT_EVENT_VERSION,
    listOf(EventLabel.SessionId(sessionId)),
    Vero2InfoSnapshotPayload(createdAt, DEFAULT_EVENT_VERSION, version, battery)) {

    @Keep
    class Vero2InfoSnapshotPayload(createdAt: Long,
                                   eventVersion: Int,
                                   val version: Vero2Version,
                                   val battery: BatteryInfo) : EventPayload(EventPayloadType.VERO_2_INFO_SNAPSHOT, eventVersion, createdAt) {

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
}

