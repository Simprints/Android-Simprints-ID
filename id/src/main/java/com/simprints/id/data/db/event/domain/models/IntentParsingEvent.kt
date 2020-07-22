package com.simprints.id.data.db.event.domain.models

import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventType.INTENT_PARSING
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
    IntentParsingPayload(createdAt, EVENT_VERSION, integration),
    INTENT_PARSING) {

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
