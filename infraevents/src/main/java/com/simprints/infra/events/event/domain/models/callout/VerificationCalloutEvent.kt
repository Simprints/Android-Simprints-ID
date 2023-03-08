package com.simprints.infra.events.event.domain.models.callout

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventLabels
import com.simprints.infra.events.event.domain.models.EventPayload
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.EventType.CALLOUT_VERIFICATION
import java.util.*

@Keep
data class VerificationCalloutEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: VerificationCalloutPayload,
    override val type: EventType
) : Event() {

    constructor(
        createdAt: Long,
        projectId: String,
        userId: String,
        moduleId: String,
        verifyGuid: String,
        metadata: String,
        labels: EventLabels = EventLabels()
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        VerificationCalloutPayload(createdAt, EVENT_VERSION, projectId, userId, moduleId, verifyGuid, metadata),
        CALLOUT_VERIFICATION)

    @Keep
    data class VerificationCalloutPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        val projectId: String,
        val userId: String,
        val moduleId: String,
        val verifyGuid: String,
        val metadata: String,
        override val type: EventType = CALLOUT_VERIFICATION,
        override val endedAt: Long = 0
    ) : EventPayload()

    companion object {
        const val EVENT_VERSION = 1
    }
}
