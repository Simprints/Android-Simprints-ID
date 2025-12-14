package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.core.tools.utils.randomUUID
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType.Companion.EXTERNAL_CREDENTIAL_SEARCH_KEY
import com.simprints.infra.events.event.domain.models.EventType.EXTERNAL_CREDENTIAL_SEARCH
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
@SerialName(EXTERNAL_CREDENTIAL_SEARCH_KEY)
data class ExternalCredentialSearchEvent(
    override val id: String = randomUUID(),
    override val payload: ExternalCredentialSearchPayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        id: String = randomUUID(),
        createdAt: Timestamp,
        endedAt: Timestamp,
        probeExternalCredentialId: String,
        candidateIds: List<String>,
    ) : this(
        id = id,
        payload = ExternalCredentialSearchPayload(
            id = id,
            createdAt = createdAt,
            endedAt = endedAt,
            eventVersion = EVENT_VERSION,
            probeExternalCredentialId = probeExternalCredentialId,
            result = ExternalCredentialSearchResult(
                candidateIds = candidateIds,
            ),
        ),
        type = EXTERNAL_CREDENTIAL_SEARCH,
    )

    @Keep
    @Serializable
    data class ExternalCredentialSearchPayload(
        override val createdAt: Timestamp,
        override val endedAt: Timestamp? = null,
        override val eventVersion: Int = EVENT_VERSION,
        val id: String,
        val probeExternalCredentialId: String,
        val result: ExternalCredentialSearchResult,
        override val type: EventType = EXTERNAL_CREDENTIAL_SEARCH,
    ) : EventPayload() {
        override fun toSafeString(): String = "results: $result, probe ID: $probeExternalCredentialId"
    }

    @Keep
    @Serializable
    data class ExternalCredentialSearchResult(
        val candidateIds: List<String>,
    )

    override fun getTokenizableFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this // No tokenized field

    companion object {
        const val EVENT_VERSION = 0
    }
}
