package com.simprints.infra.events.event.domain.models.callout

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventPayload
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.EventType.CALLOUT_IDENTIFICATION_V3
import java.util.UUID

@Keep
data class IdentificationCalloutEventV3(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: IdentificationCalloutPayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        createdAt: Timestamp,
        projectId: String,
        userId: TokenizableString,
        moduleId: TokenizableString,
        metadata: String?,
        biometricDataSource: BiometricDataSource,
    ) : this(
        UUID.randomUUID().toString(),
        IdentificationCalloutPayload(
            createdAt = createdAt,
            eventVersion = EVENT_VERSION,
            projectId = projectId,
            userId = userId,
            moduleId = moduleId,
            metadata = metadata,
            biometricDataSource = biometricDataSource,
        ),
        CALLOUT_IDENTIFICATION_V3,
    )

    override fun getTokenizableFields(): Map<TokenKeyType, TokenizableString> = mapOf(
        TokenKeyType.AttendantId to payload.userId,
        TokenKeyType.ModuleId to payload.moduleId,
    )

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this.copy(
        payload = payload.copy(
            userId = map[TokenKeyType.AttendantId] ?: payload.userId,
            moduleId = map[TokenKeyType.ModuleId] ?: payload.moduleId,
        ),
    )

    @Keep
    data class IdentificationCalloutPayload(
        override val createdAt: Timestamp,
        override val eventVersion: Int,
        val projectId: String,
        val userId: TokenizableString,
        val moduleId: TokenizableString,
        val metadata: String?,
        val biometricDataSource: BiometricDataSource,
        override val endedAt: Timestamp? = null,
        override val type: EventType = CALLOUT_IDENTIFICATION_V3,
    ) : EventPayload() {
        override fun toSafeString(): String = "module ID: $moduleId, metadata: $metadata, biometricDataSource: $biometricDataSource"
    }

    companion object {
        const val EVENT_VERSION = 3
    }
}
