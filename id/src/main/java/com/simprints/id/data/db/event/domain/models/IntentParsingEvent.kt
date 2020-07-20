package com.simprints.id.data.db.event.domain.models

import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import io.realm.internal.Keep
import java.util.*

@androidx.annotation.Keep
class IntentParsingEvent(
    createdAt: Long,
    integration: IntentParsingPayload.IntegrationInfo,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    mutableListOf(SessionIdLabel(sessionId)),
    IntentParsingPayload(createdAt, DEFAULT_EVENT_VERSION, integration)) {

    @Keep
    class IntentParsingPayload(
        createdAt: Long,
        eventVersion: Int,
        val integration: IntegrationInfo
    ) : EventPayload(EventType.INTENT_PARSING, eventVersion, createdAt) {

        @Keep
        enum class IntegrationInfo {
            ODK,
            STANDARD,
            COMMCARE
        }
    }
}
