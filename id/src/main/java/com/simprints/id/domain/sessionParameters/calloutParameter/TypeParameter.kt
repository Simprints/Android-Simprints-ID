package com.simprints.id.domain.sessionParameters.calloutParameter

import com.simprints.id.domain.callout.CalloutAction
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.model.ALERT_TYPE


class TypeParameter(calloutAction: CalloutAction)
    : CalloutParameter<CalloutAction> {

    override val value: CalloutAction = calloutAction

    override fun validate() {
        val calloutType = value
        when (calloutType) {
            CalloutAction.MISSING -> throw InvalidCalloutError(ALERT_TYPE.INVALID_INTENT_ACTION)
            CalloutAction.INVALID -> throw InvalidCalloutError(ALERT_TYPE.INVALID_INTENT_ACTION)
            else -> {}
        }
    }

}
