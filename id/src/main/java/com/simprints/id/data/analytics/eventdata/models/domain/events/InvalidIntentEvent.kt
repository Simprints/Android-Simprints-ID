package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep

@Keep
class InvalidIntentEvent(override val starTime: Long,
                         val action: IntentAction,
                         val extras: Map<String, Any?>): Event(EventType.INVALID_INTENT) {
    
    enum class IntentAction {
        ODK_REGISTER,
        ODK_IDENTIFY,
        ODK_VERIFY,
        ODK_CONFIRM,
        STANDARD_REGISTER,
        STANDARD_VERIFY,
        STANDARD_IDENTIFY,
        STANDARD_CONFIRM
    }
}
