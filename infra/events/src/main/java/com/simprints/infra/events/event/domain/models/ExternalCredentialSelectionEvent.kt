package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.core.tools.utils.randomUUID
import com.simprints.infra.config.store.models.TokenKeyType

@Keep
data class ExternalCredentialSelectionEvent(
    override val id: String = randomUUID(),
    override val payload: ExternalCredentialSelectionPayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        startTime: Timestamp,
        endTime: Timestamp,
        credentialType: ExternalCredentialType,
    ) : this(
        startTime = startTime,
        endTime = endTime,
        credentialType = credentialType,
        skipReason = null,
        skipOther = null,
    )

    constructor(
        startTime: Timestamp,
        endTime: Timestamp,
        skipReason: SkipReason,
        skipOther: String?,
    ) : this(
        startTime = startTime,
        endTime = endTime,
        credentialType = null,
        skipReason = skipReason,
        skipOther = skipOther.takeIf { skipReason == SkipReason.OTHER },
    )

    constructor(
        id: String = randomUUID(),
        startTime: Timestamp,
        endTime: Timestamp,
        credentialType: ExternalCredentialType?,
        skipReason: SkipReason?,
        skipOther: String?,
    ) : this(
        id = id,
        payload = ExternalCredentialSelectionPayload(
            startTime = startTime,
            endTime = endTime,
            eventVersion = EVENT_VERSION,
            id = id,
            credentialType = credentialType,
            skipReason = skipReason,
            skipOther = skipOther,
        ),
        type = EventType.EXTERNAL_CREDENTIAL_SELECTION,
    )

    @Keep
    data class ExternalCredentialSelectionPayload(
        override val startTime: Timestamp,
        override val endTime: Timestamp? = null,
        override val eventVersion: Int,
        val id: String,
        val credentialType: ExternalCredentialType?,
        val skipReason: SkipReason?,
        val skipOther: String?,
        override val type: EventType = EventType.EXTERNAL_CREDENTIAL_SELECTION,
    ) : EventPayload() {
        override fun toSafeString(): String = "credentialType: $credentialType, skipReason: $skipReason, skipOther: $skipOther"
    }

    @Keep
    enum class SkipReason {
        DOES_NOT_HAVE_ID,
        DID_NOT_BRING_ID,
        BROUGHT_INCORRECT_ID,
        NO_CONSENT,
        ID_DAMAGED,
        UNABLE_TO_SCAN,
        OTHER,
    }

    override fun getTokenizableFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this // No tokenized field

    companion object {
        const val EVENT_VERSION = 0
    }
}
