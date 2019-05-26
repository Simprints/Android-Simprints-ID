package com.simprints.clientapi.controllers.core.eventData.model

import androidx.annotation.Keep
import com.simprints.clientapi.controllers.core.eventData.model.InvalidIntentEvent.IntentAction.*
import com.simprints.id.data.analytics.eventdata.models.domain.events.InvalidIntentEvent as CoreInvalidIntentEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.InvalidIntentEvent.IntentAction as CoreIntentAction


@Keep
class InvalidIntentEvent(starTime: Long,
                         val action: IntentAction,
                         val extras: Map<String, Any?>) : Event(EventType.INVALID_INTENT, starTime) {

    constructor(starTime: Long,
                action: String,
                extras: Map<String, Any?>):
        this(starTime, IntentAction.fromDomainToCore(action), extras)

    enum class IntentAction {
        ODK_REGISTER,
        ODK_IDENTIFY,
        ODK_VERIFY,
        ODK_CONFIRM,
        STANDARD_REGISTER,
        STANDARD_VERIFY,
        STANDARD_IDENTIFY,
        STANDARD_CONFIRM;

        companion object {
            fun fromDomainToCore(action: String): IntentAction =
                when (action) {
                    "com.simprints.simodkadapter.REGISTER" -> ODK_REGISTER
                    "com.simprints.simodkadapter.IDENTIFY" -> ODK_IDENTIFY
                    "com.simprints.simodkadapter.CONFIRM_IDENTITY" -> ODK_CONFIRM
                    "com.simprints.simodkadapter.VERIFY" -> ODK_VERIFY
                    "com.simprints.id.REGISTER" -> STANDARD_REGISTER
                    "com.simprints.id.IDENTIFY" -> STANDARD_IDENTIFY
                    "com.simprints.id.CONFIRM_IDENTITY" -> STANDARD_CONFIRM
                    "com.simprints.id.VERIFY" -> STANDARD_VERIFY
                    else -> throw IllegalArgumentException("Invalid action")
                }
        }
    }
}

fun InvalidIntentEvent.IntentAction.fromDomainToCore() =
    when (this) {
        ODK_REGISTER -> CoreIntentAction.ODK_REGISTER
        ODK_IDENTIFY -> CoreIntentAction.ODK_IDENTIFY
        ODK_VERIFY -> CoreIntentAction.ODK_VERIFY
        ODK_CONFIRM -> CoreIntentAction.ODK_CONFIRM
        STANDARD_REGISTER -> CoreIntentAction.STANDARD_REGISTER
        STANDARD_VERIFY -> CoreIntentAction.STANDARD_VERIFY
        STANDARD_IDENTIFY -> CoreIntentAction.STANDARD_IDENTIFY
        STANDARD_CONFIRM -> CoreIntentAction.STANDARD_CONFIRM
    }

fun InvalidIntentEvent.fromDomainToCore() =
    CoreInvalidIntentEvent(starTime, action.fromDomainToCore(), extras)
