package com.simprints.id.data.db.event.domain.events

import io.realm.internal.Keep
import java.util.*

@androidx.annotation.Keep
class IntentParsingEvent(
    startTime: Long,
    integration: IntentParsingPayload.IntegrationInfo,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    DEFAULT_EVENT_VERSION,
    listOf(EventLabel.SessionId(sessionId)),
    IntentParsingPayload(startTime, DEFAULT_EVENT_VERSION, integration)) {

    @Keep
    class IntentParsingPayload(
        creationTime: Long,
        version: Int,
        val integration: IntegrationInfo
    ) : EventPayload(EventPayloadType.INTENT_PARSING, version, creationTime) {

        @Keep
        enum class IntegrationInfo {
            ODK,
            STANDARD,
            COMMCARE
        }
    }
}
