package com.simprints.eventsystem.event.domain.models

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.simprints.eventsystem.event.domain.models.EventType.VERO_2_INFO_SNAPSHOT
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
        Vero2InfoSnapshotPayload.Vero2InfoSnapshotPayloadForNewApi(
            createdAt,
            NEW_EVENT_VERSION,
            battery,
            version as Vero2Version.Vero2NewApiVersion
        ),
        VERO_2_INFO_SNAPSHOT
    )


    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "eventVersion",
        visible = true
    )
    @JsonSubTypes(
        JsonSubTypes.Type(
            value = Vero2InfoSnapshotPayload.Vero2InfoSnapshotPayloadForNewApi::class,
            name = NEW_EVENT_VERSION.toString()
        ),
        JsonSubTypes.Type(
            value = Vero2InfoSnapshotPayload.Vero2InfoSnapshotPayloadForOldApi::class,
            name = OLD_EVENT_VERSION.toString()
        ),
    )
    @Keep
    sealed class Vero2InfoSnapshotPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        open val battery: BatteryInfo,
        open val version: Vero2Version,
        override val type: EventType = VERO_2_INFO_SNAPSHOT,
        override val endedAt: Long = 0
    ): EventPayload() {

        @Keep
        data class Vero2InfoSnapshotPayloadForNewApi(
            override val createdAt: Long,
            override val eventVersion: Int,
            override val battery: BatteryInfo,
            override val version: Vero2Version.Vero2NewApiVersion
        ): Vero2InfoSnapshotPayload(
            createdAt,
            eventVersion,
            battery,
            version
        )


        @Deprecated(message = "Only used for backwards compatibility")
        @Keep
        data class Vero2InfoSnapshotPayloadForOldApi(
            override val createdAt: Long,
            override val eventVersion: Int,
            override val battery: BatteryInfo,
            override val version: Vero2Version.Vero2OldApiVersion
        ): Vero2InfoSnapshotPayload(
            createdAt,
            eventVersion,
            battery,
            version
        )

    }

    sealed class Vero2Version {
        @Keep
        data class Vero2NewApiVersion(
            val hardwareVersion: String,
            val cypressApp: String,
            val stmApp: String,
            val un20App: String
        ): Vero2Version()


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
        ): Vero2Version()
    }

    @Keep
    data class BatteryInfo(
        val charge: Int,
        val voltage: Int,
        val current: Int,
        val temperature: Int
    )

    companion object {
        const val NEW_EVENT_VERSION = 2
        const val OLD_EVENT_VERSION = 1
    }
}

