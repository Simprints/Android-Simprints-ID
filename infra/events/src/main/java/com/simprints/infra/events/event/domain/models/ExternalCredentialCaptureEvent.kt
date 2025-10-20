package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType.EXTERNAL_CREDENTIAL_CAPTURE
import java.util.UUID

@Keep
@ExcludedFromGeneratedTestCoverageReports("Data struct")
data class ExternalCredentialCaptureEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: ExternalCredentialCapturePayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        startTime: Timestamp,
        endTime: Timestamp,
        payloadId: String,
        autoCaptureStartTime: Timestamp,
        autoCaptureEndTime: Timestamp,
        ocrErrorCount: Int,
        capturedTextLength: Int,
        credentialTextLength: Int,
        selectionId: String,
    ) : this(
        id = UUID.randomUUID().toString(),
        payload = ExternalCredentialCapturePayload(
            createdAt = startTime,
            eventVersion = EVENT_VERSION,
            id = payloadId,
            endedAt = endTime,
            autoCaptureStartTime = autoCaptureStartTime,
            autoCaptureEndTime = autoCaptureEndTime,
            ocrErrorCount = ocrErrorCount,
            capturedTextLength = capturedTextLength,
            credentialTextLength = credentialTextLength,
            selectionId = selectionId,
        ),
        type = EXTERNAL_CREDENTIAL_CAPTURE,
    )

    override fun getTokenizableFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this // No tokenized field

    @Keep
    @ExcludedFromGeneratedTestCoverageReports("Data struct")
    data class ExternalCredentialCapturePayload(
        override val createdAt: Timestamp,
        override val eventVersion: Int,
        val id: String,
        val autoCaptureStartTime: Timestamp,
        val autoCaptureEndTime: Timestamp,
        val ocrErrorCount: Int,
        val capturedTextLength: Int,
        val credentialTextLength: Int,
        val selectionId: String,
        override val endedAt: Timestamp? = null,
        override val type: EventType = EXTERNAL_CREDENTIAL_CAPTURE,
    ) : EventPayload() {
        override fun toSafeString(): String = "credential capture: $id"
    }

    companion object {
        const val EVENT_VERSION = 0
    }
}
