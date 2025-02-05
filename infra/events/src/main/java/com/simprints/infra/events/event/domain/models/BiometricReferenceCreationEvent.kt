package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType.BIOMETRIC_REFERENCE_CREATION
import java.util.UUID

@Keep
data class BiometricReferenceCreationEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: BiometricReferenceCreationPayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        startTime: Timestamp,
        referenceId: String,
        modality: BiometricReferenceModality,
        captureIds: List<String>,
    ) : this(
        UUID.randomUUID().toString(),
        BiometricReferenceCreationPayload(
            createdAt = startTime,
            eventVersion = EVENT_VERSION,
            id = referenceId,
            modality = modality,
            captureIds = captureIds,
        ),
        BIOMETRIC_REFERENCE_CREATION,
    )

    override fun getTokenizableFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>): Event = this

    data class BiometricReferenceCreationPayload(
        override val createdAt: Timestamp,
        override val eventVersion: Int,
        val id: String,
        val modality: BiometricReferenceModality,
        val captureIds: List<String>,
        override val endedAt: Timestamp? = null,
        override val type: EventType = BIOMETRIC_REFERENCE_CREATION,
    ) : EventPayload()

    enum class BiometricReferenceModality {
        FACE,
        FINGERPRINT,
    }

    companion object {
        const val EVENT_VERSION = 1
    }
}
