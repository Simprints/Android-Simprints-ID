package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType.PERSON_CREATION
import java.util.UUID

@Keep
data class PersonCreationEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: PersonCreationPayload,
    override val type: EventType
) : Event() {

    constructor(
        startTime: Long,
        fingerprintCaptureIds: List<String>?,
        fingerprintReferenceId: String?,
        faceCaptureIds: List<String>?,
        faceReferenceId: String?,
        labels: EventLabels = EventLabels()
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        PersonCreationPayload(
            createdAt = startTime,
            eventVersion = EVENT_VERSION,
            fingerprintCaptureIds = fingerprintCaptureIds,
            fingerprintReferenceId = fingerprintReferenceId,
            faceCaptureIds = faceCaptureIds,
            faceReferenceId = faceReferenceId
        ),
        PERSON_CREATION
    )

    override fun getTokenizedFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this // No tokenized fields


    // At the end of the sequence of capture, we build a Person object used either for enrolment, verification or identification
    @Keep
    data class PersonCreationPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        val fingerprintCaptureIds: List<String>?,
        val fingerprintReferenceId: String?,
        val faceCaptureIds: List<String>?,
        val faceReferenceId: String?,
        override val type: EventType = PERSON_CREATION,
        override val endedAt: Long = 0
    ) : EventPayload()

    companion object {
        const val EVENT_VERSION = 1
    }
}
