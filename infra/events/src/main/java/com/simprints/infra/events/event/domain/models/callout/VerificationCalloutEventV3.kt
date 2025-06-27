package com.simprints.infra.events.event.domain.models.callout

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventPayload
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.EventType.CALLOUT_VERIFICATION_V3
import java.util.UUID

@Keep
data class VerificationCalloutEventV3(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: VerificationCalloutPayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        createdAt: Timestamp,
        projectId: String,
        userId: TokenizableString,
        moduleId: TokenizableString,
        verifyGuid: String,
        metadata: String,
        biometricDataSource: BiometricDataSource,
    ) : this(
        UUID.randomUUID().toString(),
        VerificationCalloutPayload(
            createdAt = createdAt,
            eventVersion = EVENT_VERSION,
            projectId = projectId,
            userId = userId,
            moduleId = moduleId,
            verifyGuid = verifyGuid,
            metadata = metadata,
            biometricDataSource = biometricDataSource,
        ),
        CALLOUT_VERIFICATION_V3,
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
    data class VerificationCalloutPayload(
        override val createdAt: Timestamp,
        override val eventVersion: Int,
        val projectId: String,
        val userId: TokenizableString,
        val moduleId: TokenizableString,
        val verifyGuid: String,
        val metadata: String,
        val biometricDataSource: BiometricDataSource,
        override val endedAt: Timestamp? = null,
        override val type: EventType = CALLOUT_VERIFICATION_V3,
    ) : EventPayload() {
        override fun toSafeString(): String = "module ID: $moduleId, guid: $verifyGuid, metadata: $metadata, biometricDataSource: $biometricDataSource"
    }

    companion object {
        const val EVENT_VERSION = 3
    }
}
