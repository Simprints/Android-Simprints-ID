package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.core.tools.utils.randomUUID
import com.simprints.infra.config.store.models.TokenKeyType

@Keep
data class ExternalCredentialConfirmationEvent(
    override val id: String = randomUUID(),
    override val payload: ExternalCredentialConfirmationPayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        startTime: Timestamp,
        endTime: Timestamp,
        result: ExternalCredentialConfirmationResult,
        userInteractedWithImage: Boolean? = null,
    ) : this(
        id = randomUUID(),
        payload = ExternalCredentialConfirmationPayload(
            startTime = startTime,
            endTime = endTime,
            eventVersion = EVENT_VERSION,
            result = result,
            userInteractedWithImage = userInteractedWithImage,
        ),
        type = EventType.EXTERNAL_CREDENTIAL_CONFIRMATION,
    )

    @Keep
    data class ExternalCredentialConfirmationPayload(
        override val startTime: Timestamp,
        override val endTime: Timestamp? = null,
        override val eventVersion: Int,
        val result: ExternalCredentialConfirmationResult,
        val userInteractedWithImage: Boolean?,
        override val type: EventType = EventType.EXTERNAL_CREDENTIAL_CONFIRMATION,
    ) : EventPayload() {
        override fun toSafeString(): String = "result: $result"
    }

    @Keep
    enum class ExternalCredentialConfirmationResult {
        CONTINUE,
        RECAPTURE,
    }

    override fun getTokenizableFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this // No tokenized field

    companion object {
        const val EVENT_VERSION = 0
    }
}
