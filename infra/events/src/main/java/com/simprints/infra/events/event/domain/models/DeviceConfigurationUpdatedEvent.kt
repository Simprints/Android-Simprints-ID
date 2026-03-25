package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType.Companion.DEVICE_CONFIGURATION_UPDATED_KEY
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Keep
@Serializable
@SerialName(DEVICE_CONFIGURATION_UPDATED_KEY)
data class DeviceConfigurationUpdatedEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: DeviceConfigurationUpdatedPayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        createdAt: Timestamp,
        language: String,
        downSyncModules: List<TokenizableString>? = null,
        sourceUpdate: DeviceConfigurationUpdateSource,
    ) : this(
        id = UUID.randomUUID().toString(),
        payload = DeviceConfigurationUpdatedPayload(
            createdAt = createdAt,
            configuration = DeviceConfigurationUpdated(
                language = language,
                downSyncModules = downSyncModules,
            ),
            sourceUpdate = sourceUpdate,
            eventVersion = EVENT_VERSION,
        ),
        type = EventType.DEVICE_CONFIGURATION_UPDATED,
    )

    @Keep
    @Serializable
    data class DeviceConfigurationUpdatedPayload(
        override val createdAt: Timestamp,
        override val endedAt: Timestamp? = null,
        val configuration: DeviceConfigurationUpdated,
        val sourceUpdate: DeviceConfigurationUpdateSource,
        override val eventVersion: Int,
        override val type: EventType = EventType.DEVICE_CONFIGURATION_UPDATED,
    ) : EventPayload() {
        override fun toSafeString(): String = "source: $sourceUpdate, language: ${configuration.language}, " +
            "downSyncModules: ${configuration.downSyncModules?.joinToString(",") ?: "null"}"
    }

    @Keep
    @Serializable
    data class DeviceConfigurationUpdated(
        val language: String,
        val downSyncModules: List<TokenizableString>? = null,
    )

    @Keep
    @Serializable
    enum class DeviceConfigurationUpdateSource {
        REMOTE,
        LOCAL,
    }

    override fun getTokenizableListFields(): Map<TokenKeyType, List<TokenizableString>> = mapOf(
        TokenKeyType.ModuleId to payload.configuration.downSyncModules.orEmpty(),
    )

    override fun setTokenizedListFields(map: Map<TokenKeyType, List<TokenizableString>>): Event = this.copy(
        payload = payload.copy(
            configuration = payload.configuration.copy(
                downSyncModules = map[TokenKeyType.ModuleId] ?: payload.configuration.downSyncModules,
            ),
        ),
    )

    companion object {
        const val EVENT_VERSION = 0
    }
}
