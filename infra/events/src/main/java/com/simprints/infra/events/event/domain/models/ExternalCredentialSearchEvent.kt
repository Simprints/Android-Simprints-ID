package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.core.tools.utils.randomUUID
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType.EXTERNAL_CREDENTIAL_SEARCH

@Keep
data class ExternalCredentialSearchEvent(
    override val id: String = randomUUID(),
    override val payload: ExternalCredentialSearchPayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        id: String = randomUUID(),
        startTime: Timestamp,
        endTime: Timestamp,
        probeExternalCredentialId: String,
        candidateIds: List<String>,
    ) : this(
        id = id,
        payload = ExternalCredentialSearchPayload(
            id = id,
            startTime = startTime,
            endTime = endTime,
            eventVersion = EVENT_VERSION,
            probeExternalCredentialId = probeExternalCredentialId,
            result = ExternalCredentialSearchResult(
                candidateIds = candidateIds,
            ),
        ),
        type = EXTERNAL_CREDENTIAL_SEARCH,
    )

    @Keep
    data class ExternalCredentialSearchPayload(
        override val startTime: Timestamp,
        override val endTime: Timestamp? = null,
        override val eventVersion: Int,
        val id: String,
        val probeExternalCredentialId: String,
        val result: ExternalCredentialSearchResult,
        override val type: EventType = EXTERNAL_CREDENTIAL_SEARCH,
    ) : EventPayload() {
        override fun toSafeString(): String = "results: $result, probe ID: $probeExternalCredentialId"
    }

    @Keep
    data class ExternalCredentialSearchResult(
        val candidateIds: List<String>,
    )

    override fun getTokenizableFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this // No tokenized field

    companion object {
        const val EVENT_VERSION = 0
    }
}
