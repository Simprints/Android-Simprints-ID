package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType.ARTIFICIAL_TERMINATION
import java.util.UUID

@Keep
@Deprecated("Should be removed")
data class ArtificialTerminationEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: ArtificialTerminationPayload,
    override val type: EventType,
    override var sessionId: String? = null,
    override var projectId: String? = null,
) : Event() {

    constructor(
        createdAt: Timestamp,
        reason: ArtificialTerminationPayload.Reason,
    ) : this(
        UUID.randomUUID().toString(),
        ArtificialTerminationPayload(createdAt, EVENT_VERSION, reason),
        ARTIFICIAL_TERMINATION
    )


    override fun getTokenizedFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) =
        this // No tokenized fields

    @Keep
    @Deprecated("Should be removed")
    data class ArtificialTerminationPayload(
        override val createdAt: Timestamp,
        override val eventVersion: Int,
        val reason: Reason,
        override val endedAt: Timestamp? = null,
        override val type: EventType = ARTIFICIAL_TERMINATION,
    ) : EventPayload() {

        //TODO: We can remove TIMED_OUT after checking in with analytics that it's not used for anything.
        @Keep
        @Deprecated("Should be removed")
        enum class Reason {

            TIMED_OUT, NEW_SESSION
        }
    }

    companion object {

        const val EVENT_VERSION = 2
    }
}
