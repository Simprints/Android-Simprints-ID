package com.simprints.id.data.db.event.domain.models


import com.simprints.id.data.db.event.domain.models.EventType.INTENT_PARSING
import io.realm.internal.Keep
import java.util.*

@androidx.annotation.Keep
class IntentParsingEvent(
        override val id: String = UUID.randomUUID().toString(),
        override var labels: EventLabels,
        override val payload: IntentParsingPayload,
        override val type: EventType
) : Event(id, labels, payload, type) {

    constructor(
        createdAt: Long,
        integration: IntentParsingPayload.IntegrationInfo,
        labels: EventLabels = EventLabels() //StopShip: to change in PAS-993
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        IntentParsingPayload(createdAt, EVENT_VERSION, integration),
        INTENT_PARSING)

    @Keep
    class IntentParsingPayload(
        createdAt: Long,
        eventVersion: Int,
        val integration: IntegrationInfo
    ) : EventPayload(INTENT_PARSING, eventVersion, createdAt) {

        @Keep
        enum class IntegrationInfo {
            ODK,
            STANDARD,
            COMMCARE
        }
    }

    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
