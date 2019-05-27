package com.simprints.clientapi.controllers.core.eventData.model
import com.simprints.id.data.analytics.eventdata.models.domain.events.InvalidIntentEvent.IntentAction as CoreIntentAction

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
        fun parse(action: String) =
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

fun IntentAction.fromDomainToCore() =
    when (this) {
        IntentAction.ODK_REGISTER -> CoreIntentAction.ODK_REGISTER
        IntentAction.ODK_IDENTIFY -> CoreIntentAction.ODK_IDENTIFY
        IntentAction.ODK_VERIFY -> CoreIntentAction.ODK_VERIFY
        IntentAction.ODK_CONFIRM -> CoreIntentAction.ODK_CONFIRM
        IntentAction.STANDARD_REGISTER -> CoreIntentAction.STANDARD_REGISTER
        IntentAction.STANDARD_VERIFY -> CoreIntentAction.STANDARD_VERIFY
        IntentAction.STANDARD_IDENTIFY -> CoreIntentAction.STANDARD_IDENTIFY
        IntentAction.STANDARD_CONFIRM -> CoreIntentAction.STANDARD_CONFIRM
    }
