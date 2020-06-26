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
    listOf(EventLabel.SessionId(sessionId)),
    IntentParsingPayload(startTime, integration)) {

    @Keep
    class IntentParsingPayload(
        startTime: Long,
        val integration: IntegrationInfo
    ) : EventPayload(EventPayloadType.INTENT_PARSING, startTime) {

        @Keep
        enum class IntegrationInfo {
            ODK,
            STANDARD,
            COMMCARE
        }
    }
}
