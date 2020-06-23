package com.simprints.id.data.db.session.domain.models.events

import androidx.annotation.Keep
import com.simprints.id.data.db.session.domain.models.events.Vero2InfoSnapshotEvent.Vero2InfoSnapshotPayload.BatteryInfo
import com.simprints.id.data.db.session.domain.models.events.Vero2InfoSnapshotEvent.Vero2InfoSnapshotPayload.Vero2Version
import java.util.*

@Keep
class Vero2InfoSnapshotEvent(
    startTime: Long,
    version: Vero2Version,
    battery: BatteryInfo,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    listOf(EventLabel.SessionId(sessionId)),
    Vero2InfoSnapshotPayload(startTime, version, battery)) {

    @Keep
    class Vero2InfoSnapshotPayload(val startTime: Long,
                                   val version: Vero2Version,
                                   val battery: BatteryInfo) : EventPayload(EventPayloadType.VERO_2_INFO_SNAPSHOT) {

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

