package com.simprints.id.data.model.calloutParameter.concrete

import android.content.Intent
import com.simprints.id.data.model.CalloutType
import com.simprints.id.data.model.calloutParameter.CalloutParameter
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.model.ALERT_TYPE
import java.security.InvalidParameterException

class TypeParameter(intent: Intent) : CalloutParameter<CalloutType> {

    private val defaultValue: CalloutType = CalloutType.INVALID_OR_MISSING

    override val value: CalloutType = CalloutType.forNullableAction(intent.action, defaultValue)

    override fun validate() {
        val calloutType = value
        if (calloutType == CalloutType.INVALID_OR_MISSING) {
            throw InvalidCalloutError(ALERT_TYPE.INVALID_INTENT_ACTION)
        }
    }

    private fun CalloutType.Companion.forNullableAction(action: String?, default: CalloutType) =
            if (action != null) {
                forNonNullAction(action, default)
            } else {
                default
            }

    private fun CalloutType.Companion.forNonNullAction(action: String, default: CalloutType) =
            try {
                forAction(action)
            } catch (exception: InvalidParameterException) {
                default
            }


}