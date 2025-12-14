package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType.Companion.VERO_2_INFO_SNAPSHOT_KEY
import com.simprints.infra.events.event.domain.models.EventType.VERO_2_INFO_SNAPSHOT
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.UUID

@Keep
@Serializable
@SerialName(VERO_2_INFO_SNAPSHOT_KEY)
data class Vero2InfoSnapshotEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: Vero2InfoSnapshotPayload,
    override val type: EventType = VERO_2_INFO_SNAPSHOT,
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
            battery = battery,
            version = version,
        ),
        VERO_2_INFO_SNAPSHOT,
    )

    override fun getTokenizableFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this

    @Keep
    @Serializable(with = Vero2InfoSnapshotPayloadSerializer::class)
    sealed class Vero2InfoSnapshotPayload : EventPayload() {
        abstract override val type: EventType
        abstract override val endedAt: Timestamp?

        abstract val version: Vero2Version
        abstract val battery: BatteryInfo

        @Keep
        @Serializable
        data class Vero2InfoSnapshotPayloadForNewApi(
            override val createdAt: Timestamp,
            override val battery: BatteryInfo,
            override val version: Vero2Version.Vero2NewApiVersion,
            override val eventVersion: Int = NEW_EVENT_VERSION,
            override val type: EventType = VERO_2_INFO_SNAPSHOT,
            override val endedAt: Timestamp? = null,
        ) : Vero2InfoSnapshotPayload()

        @Deprecated(message = "Only used for backwards compatibility")
        @Keep
        @Serializable
        data class Vero2InfoSnapshotPayloadForOldApi(
            override val createdAt: Timestamp,
            override val battery: BatteryInfo,
            override val version: Vero2Version.Vero2OldApiVersion,
            override val eventVersion: Int = OLD_EVENT_VERSION,
            override val type: EventType = VERO_2_INFO_SNAPSHOT,
            override val endedAt: Timestamp? = null,
        ) : Vero2InfoSnapshotPayload()
    }

    @Serializable
    sealed class Vero2Version {
        @Keep
        @Serializable
        data class Vero2NewApiVersion(
            val hardwareRevision: String,
            val cypressApp: String,
            val stmApp: String,
            val un20App: String,
            val master: Long = 0,
        ) : Vero2Version()

        @Deprecated(message = "Only used for backwards compatibility")
        @Keep
        @Serializable
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
    @Serializable
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

object Vero2InfoSnapshotPayloadSerializer : JsonContentPolymorphicSerializer<Vero2InfoSnapshotEvent.Vero2InfoSnapshotPayload>(
    Vero2InfoSnapshotEvent.Vero2InfoSnapshotPayload::class,
) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<Vero2InfoSnapshotEvent.Vero2InfoSnapshotPayload> =
        when (val version = element.jsonObject["eventVersion"]?.jsonPrimitive?.intOrNull) {
            Vero2InfoSnapshotEvent.NEW_EVENT_VERSION -> {
                Vero2InfoSnapshotEvent.Vero2InfoSnapshotPayload.Vero2InfoSnapshotPayloadForNewApi
                    .serializer()
            }

            Vero2InfoSnapshotEvent.OLD_EVENT_VERSION -> {
                Vero2InfoSnapshotEvent.Vero2InfoSnapshotPayload.Vero2InfoSnapshotPayloadForOldApi
                    .serializer()
            }

            else -> {
                throw SerializationException("Unknown Vero2InfoSnapshotPayload eventVersion: $version")
            }
        }
}
