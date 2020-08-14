package com.simprints.id.data.db.event.domain.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.EventType.VERO_2_INFO_SNAPSHOT
import com.simprints.id.data.db.event.domain.models.Vero2InfoSnapshotEvent.Vero2InfoSnapshotPayload.BatteryInfo
import com.simprints.id.data.db.event.domain.models.Vero2InfoSnapshotEvent.Vero2InfoSnapshotPayload.Vero2Version
import com.simprints.id.data.db.event.local.models.DbEvent.Companion.DEFAULT_EVENT_VERSION
import java.util.*

@Keep
data class Vero2InfoSnapshotEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: Vero2InfoSnapshotPayload,
    override val type: EventType
) : Event() {

    constructor(
        createdAt: Long,
        version: Vero2Version,
        battery: BatteryInfo,
        labels: EventLabels = EventLabels() //StopShip: to change in PAS-993
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        Vero2InfoSnapshotPayload(createdAt, EVENT_VERSION, version, battery),
        VERO_2_INFO_SNAPSHOT)

    @Keep
    data class Vero2InfoSnapshotPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        val version: Vero2Version,
        val battery: BatteryInfo,
        override val type: EventType = VERO_2_INFO_SNAPSHOT,
        override val endedAt: Long = 0) : EventPayload() {

        @Keep
        data class Vero2Version(
            val master: Long,
            val cypressApp: String,
            val cypressApi: String,
            val stmApp: String,
            val stmApi: String,
            val un20App: String,
            val un20Api: String
        )

        @Keep
        data class BatteryInfo(
            val charge: Int,
            val voltage: Int,
            val current: Int,
            val temperature: Int
        )
    }

    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}

