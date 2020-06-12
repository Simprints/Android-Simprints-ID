package com.simprints.id.data.db.session.domain.models.events

import io.realm.internal.Keep

@Keep
class IntentParsingEvent(
    startTime: Long,
    val integration: IntegrationInfo
) : Event(EventType.INTENT_PARSING, startTime) {

    @Keep
    enum class IntegrationInfo {
        ODK,
        STANDARD,
        COMMCARE
    }
}
