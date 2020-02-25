package com.simprints.id.data.db.session.domain.events

import io.realm.internal.Keep


@Keep
class IntentParsingEvent(starTime: Long,
                         val integration: IntegrationInfo) : Event(EventType.INTENT_PARSING, starTime) {


    @Keep
    enum class IntegrationInfo {
        ODK,
        STANDARD,
        COMMCARE
    }
}
