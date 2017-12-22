package com.simprints.id.data.model.calloutParameter.concrete

import android.content.Intent
import com.simprints.id.data.model.CalloutType
import com.simprints.id.data.model.CalloutType.Companion.forActionOrDefault
import com.simprints.id.data.model.calloutParameter.CalloutParameter
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.model.ALERT_TYPE


class TypeParameter(intent: Intent,
                    defaultValue: CalloutType = CalloutType.INVALID_OR_MISSING)
    : CalloutParameter<CalloutType> {

    override val value: CalloutType =
        if (intent.action != null) {
            forActionOrDefault(intent.action, defaultValue)
        } else {
            defaultValue
        }

    override fun validate() {
        val calloutType = value
        if (calloutType == CalloutType.INVALID_OR_MISSING) {
            throw InvalidCalloutError(ALERT_TYPE.INVALID_INTENT_ACTION)
        }
    }

}
