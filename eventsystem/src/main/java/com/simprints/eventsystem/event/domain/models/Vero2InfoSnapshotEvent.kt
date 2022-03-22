package com.simprints.eventsystem.event.domain.models

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.simprints.eventsystem.event.domain.models.EventType.VERO_2_INFO_SNAPSHOT
import com.simprints.eventsystem.event.domain.models.Vero2InfoSnapshotEvent.Vero2InfoSnapshotPayload.BatteryInfo
import com.simprints.eventsystem.event.domain.models.Vero2InfoSnapshotEvent.Vero2InfoSnapshotPayload.Vero2Version
import java.util.*

@Keep
data class Vero2InfoSnapshotEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: Vero2InfoSnapshotPayload,
    override val type: EventType
): Event() {

    constructor(
        createdAt: Long,
        version: Vero2Version,
        battery: BatteryInfo,
        labels: EventLabels = EventLabels()
    ): this(
        UUID.randomUUID().toString(),
        labels,
        Vero2InfoSnapshotPayload(createdAt, version.eventVersion, version, battery),
        VERO_2_INFO_SNAPSHOT
    )

    @Keep
    data class Vero2InfoSnapshotPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        val version: Vero2Version,
        val battery: BatteryInfo,
        override val type: EventType = VERO_2_INFO_SNAPSHOT,
        override val endedAt: Long = 0
    ): EventPayload() {

        @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.EXISTING_PROPERTY,
            property = "eventVersion",
            visible = true
        )
        @JsonSubTypes(
            JsonSubTypes.Type(
                value = Vero2Version.Vero2NewApiVersion::class,
                name = NEW_EVENT_VERSION.toString()
            ),
            JsonSubTypes.Type(
                value = Vero2Version.Vero2OldApiVersion::class,
                name = OLD_EVENT_VERSION.toString()
            ),
        )
        sealed class Vero2Version(val eventVersion: Int) {
            @Keep
            data class Vero2NewApiVersion(
                val hardwareVersion: String,
                val cypressApp: String,
                val stmApp: String,
                val un20App: String
            ): Vero2Version(NEW_EVENT_VERSION)


            @Deprecated(message = "Only used for backwards compatibility")
            @Keep
            data class Vero2OldApiVersion(
                val master: Long,
                val cypressApp: String,
                val cypressApi: String,
                val stmApp: String,
                val stmApi: String,
                val un20App: String,
                val un20Api: String
            ): Vero2Version(OLD_EVENT_VERSION)
        }

        @Keep
        data class BatteryInfo(
            val charge: Int,
            val voltage: Int,
            val current: Int,
            val temperature: Int
        )
    }

    companion object {
        const val NEW_EVENT_VERSION = 2
        const val OLD_EVENT_VERSION = 1
    }
}

