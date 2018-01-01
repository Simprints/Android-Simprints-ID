package com.simprints.id.domain.sessionParameters.calloutParameter.extras

import com.simprints.id.domain.callout.CalloutParameters
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.model.ALERT_TYPE
import com.simprints.id.tools.GuidValidator
import com.simprints.libsimprints.Constants


class ApiKeyParameter(calloutParameters: CalloutParameters,
                      private val guidValidator: GuidValidator = GuidValidator(),
                      defaultValue: String = "")
    : CalloutExtraParameter<String>(calloutParameters, Constants.SIMPRINTS_API_KEY, defaultValue) {

    override fun validate() {
        validateValueIsNotMissing()
        validateValueIsGuid()
    }

    private fun validateValueIsNotMissing() {
        if (isMissing) {
            throw InvalidCalloutError(ALERT_TYPE.MISSING_API_KEY)
        }
    }

    private fun validateValueIsGuid() {
        if (!guidValidator.isGuid(value)) {
            throw InvalidCalloutError(ALERT_TYPE.INVALID_API_KEY)
        }
    }

}
