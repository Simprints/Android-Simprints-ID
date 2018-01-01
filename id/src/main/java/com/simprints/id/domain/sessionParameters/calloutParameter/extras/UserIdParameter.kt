package com.simprints.id.domain.sessionParameters.calloutParameter.extras

import com.simprints.id.domain.callout.CalloutParameters
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.model.ALERT_TYPE
import com.simprints.libsimprints.Constants


class UserIdParameter(calloutParameters: CalloutParameters, defaultValue: String = "")
    : CalloutExtraParameter<String>(calloutParameters, Constants.SIMPRINTS_USER_ID, defaultValue) {

    override fun validate() {
        validateValueIsNotMissing()
    }

    private fun validateValueIsNotMissing() {
        if (isMissing) {
            throw InvalidCalloutError(ALERT_TYPE.MISSING_USER_ID)
        }
    }

}
