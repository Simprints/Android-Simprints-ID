package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.externalcredential.ExternalCredential
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType.EXTERNAL_CREDENTIAL_CAPTURE_VALUE
import java.util.UUID

@Keep
@ExcludedFromGeneratedTestCoverageReports("Data struct")
data class ExternalCredentialCaptureValueEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: ExternalCredentialCaptureValuePayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        startTime: Timestamp,
        payloadId: String,
        credential: ExternalCredential,
    ) : this(
        id = UUID.randomUUID().toString(),
        payload = ExternalCredentialCaptureValuePayload(
            startTime = startTime,
            eventVersion = EVENT_VERSION,
            id = payloadId,
            credential = credential,
        ),
        type = EXTERNAL_CREDENTIAL_CAPTURE_VALUE,
    )

    override fun getTokenizableFields(): Map<TokenKeyType, TokenizableString> = mapOf(
        TokenKeyType.ExternalCredential to payload.credential.value,
    )

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this.copy(
        payload = payload.copy(
            credential = payload.credential.copy(
                value = map[TokenKeyType.ExternalCredential] as? TokenizableString.Tokenized
                    ?: payload.credential.value,
            ),
        ),
    )

    @Keep
    @ExcludedFromGeneratedTestCoverageReports("Data struct")
    data class ExternalCredentialCaptureValuePayload(
        override val startTime: Timestamp,
        override val eventVersion: Int,
        val id: String,
        val credential: ExternalCredential,
        override val endTime: Timestamp? = null,
        override val type: EventType = EXTERNAL_CREDENTIAL_CAPTURE_VALUE,
    ) : EventPayload() {
        override fun toSafeString(): String = "id $id, credential: $credential"
    }

    companion object {
        const val EVENT_VERSION = 0
    }
}
