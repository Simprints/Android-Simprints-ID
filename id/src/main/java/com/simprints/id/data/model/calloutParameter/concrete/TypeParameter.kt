package com.simprints.id.data.model.calloutParameter.concrete

import android.content.Intent
import com.simprints.id.data.model.CalloutType
import com.simprints.id.data.model.calloutParameter.CalloutParameter
import com.simprints.id.model.ALERT_TYPE
import com.simprints.id.tools.exceptions.InvalidCalloutException
import java.security.InvalidParameterException

class TypeParameter(intent: Intent) : CalloutParameter<CalloutType> {

    private val defaultValue: CalloutType = CalloutType.INVALID_OR_MISSING

    override val value: CalloutType = CalloutType.forNullableIntentAction(intent.action, defaultValue)

    override fun validate() {
        val calloutType = value
        if (calloutType == CalloutType.INVALID_OR_MISSING) {
            throw InvalidCalloutException(ALERT_TYPE.INVALID_INTENT_ACTION)
        }
    }

    private fun CalloutType.Companion.forNullableIntentAction(intentAction: String?, default: CalloutType) =
            if (intentAction != null) {
                forNonNullIntentAction(intentAction, default)
            } else {
                default
            }

    private fun CalloutType.Companion.forNonNullIntentAction(intentAction: String, default: CalloutType) =
            try {
                forIntentAction(intentAction)
            } catch (exception: InvalidParameterException) {
                default
            }


}