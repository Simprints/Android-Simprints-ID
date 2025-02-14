package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType.VERO_2_INFO_SNAPSHOT
import java.util.UUID

@Keep
data class Vero2InfoSnapshotEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: Vero2InfoSnapshotPayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        createdAt: Timestamp,
        version: Vero2Version.Vero2NewApiVersion,
        battery: BatteryInfo,
    ) : this(
        UUID.randomUUID().toString(),
        Vero2InfoSnapshotPayload.Vero2InfoSnapshotPayloadForNewApi(
            createdAt = createdAt,
            eventVersion = NEW_EVENT_VERSION,
            battery = battery,
            version = version,
        ),
        VERO_2_INFO_SNAPSHOT,
    )

    override fun getTokenizableFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this // No tokenized fields

    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "eventVersion",
        visible = true,
    )
    @JsonSubTypes(
        JsonSubTypes.Type(
            value = Vero2InfoSnapshotPayload.Vero2InfoSnapshotPayloadForNewApi::class,
            name = NEW_EVENT_VERSION.toString(),
        ),
        JsonSubTypes.Type(
            value = Vero2InfoSnapshotPayload.Vero2InfoSnapshotPayloadForOldApi::class,
            name = OLD_EVENT_VERSION.toString(),
        ),
    )
    @Keep
    sealed class Vero2InfoSnapshotPayload(
        override val createdAt: Timestamp,
        override val eventVersion: Int,
        open val battery: BatteryInfo,
        open val version: Vero2Version,
        override val endedAt: Timestamp? = null,
        override val type: EventType = VERO_2_INFO_SNAPSHOT,
    ) : EventPayload() {
        override fun toSafeString(): String = "battery charge: ${battery.charge}, " +
            version
                .let { it as? Vero2Version.Vero2NewApiVersion }
                ?.let {
                    "hardware: ${it.hardwareRevision}, cypress: ${it.cypressApp},  stm: ${it.stmApp}, un20: ${it.un20App}"
                }.orEmpty()

        @Keep
        data class Vero2InfoSnapshotPayloadForNewApi(
            override val createdAt: Timestamp,
            override val eventVersion: Int,
            override val battery: BatteryInfo,
            override val version: Vero2Version.Vero2NewApiVersion,
        ) : Vero2InfoSnapshotPayload(
                createdAt,
                eventVersion,
                battery,
                version,
            )

        @Deprecated(message = "Only used for backwards compatibility")
        @Keep
        data class Vero2InfoSnapshotPayloadForOldApi(
            override val createdAt: Timestamp,
            override val eventVersion: Int,
            override val battery: BatteryInfo,
            override val version: Vero2Version.Vero2OldApiVersion,
        ) : Vero2InfoSnapshotPayload(
                createdAt,
                eventVersion,
                battery,
                version,
            )
    }

    sealed class Vero2Version {
        @Keep
        data class Vero2NewApiVersion(
            val hardwareRevision: String,
            val cypressApp: String,
            val stmApp: String,
            val un20App: String,
            val master: Long = 0,
        ) : Vero2Version()

        @Deprecated(message = "Only used for backwards compatibility")
        @Keep
        data class Vero2OldApiVersion(
            val cypressApp: String,
            val cypressApi: String,
            val stmApp: String,
            val stmApi: String,
            val un20App: String,
            val un20Api: String,
            val master: Long,
        ) : Vero2Version()
    }

    @Keep
    data class BatteryInfo(
        val charge: Int,
        val voltage: Int,
        val current: Int,
        val temperature: Int,
    )

    companion object {
        const val NEW_EVENT_VERSION = 3
        const val OLD_EVENT_VERSION = 2
    }
}
