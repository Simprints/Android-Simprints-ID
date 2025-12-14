package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType.CALLOUT_ENROLMENT_V3
import com.simprints.infra.events.event.domain.models.EventType.Companion.CALLOUT_ENROLMENT_V3_KEY
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Keep
@Serializable
@SerialName(CALLOUT_ENROLMENT_V3_KEY)
data class EnrolmentCalloutEventV3(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: EnrolmentCalloutPayload,
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
        id: String = UUID.randomUUID().toString(),
    ) : this(
        id,
        EnrolmentCalloutPayload(
            createdAt = createdAt,
            eventVersion = EVENT_VERSION,
            projectId = projectId,
            userId = userId,
            moduleId = moduleId,
            metadata = metadata,
            biometricDataSource = biometricDataSource,
        ),
        CALLOUT_ENROLMENT_V3,
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
    @Serializable
    data class EnrolmentCalloutPayload(
        override val createdAt: Timestamp,
        override val eventVersion: Int,
        val projectId: String,
        val userId: TokenizableString,
        val moduleId: TokenizableString,
        val metadata: String?,
        val biometricDataSource: BiometricDataSource,
        override val endedAt: Timestamp? = null,
        override val type: EventType = CALLOUT_ENROLMENT_V3,
    ) : EventPayload() {
        override fun toSafeString(): String = "module: $moduleId, metadata: $metadata, biometricDataSource: $biometricDataSource"
    }

    companion object {
        const val EVENT_VERSION = 3
    }
}
