package com.simprints.id.data.analytics.eventdata.models.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.InvalidIntentEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.InvalidIntentEvent.IntentAction.*

@Keep
class ApiInvalidIntentEvent(val action: String,
                            val extras: Map<String, Any?>) : ApiEvent(ApiEventType.INVALID_INTENT) {

    constructor(invalidIntentEvent: InvalidIntentEvent) :
        this(invalidIntentEvent.action.fromDomainToApi(), invalidIntentEvent.extras)
}

fun InvalidIntentEvent.IntentAction.fromDomainToApi(): String =
    when (this) {
        ODK_REGISTER -> "com.simprints.simodkadapter.REGISTER"
        ODK_IDENTIFY -> "com.simprints.simodkadapter.IDENTIFY"
        ODK_VERIFY -> "com.simprints.simodkadapter.VERIFY"
        ODK_CONFIRM -> "com.simprints.simodkadapter.CONFIRM_IDENTITY"
        STANDARD_REGISTER -> "com.simprints.id.REGISTER"
        STANDARD_VERIFY -> "com.simprints.id.VERIFY"
        STANDARD_IDENTIFY -> "com.simprints.id.IDENTIFY"
        STANDARD_CONFIRM -> "com.simprints.id.CONFIRM_IDENTITY"
    }
