package com.simprints.id.domain.sessionParameters.calloutParameter.extras

import com.simprints.id.domain.callout.CalloutAction
import com.simprints.id.domain.callout.CalloutParameters
import com.simprints.id.domain.sessionParameters.calloutParameter.TypeParameter
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.model.ALERT_TYPE
import com.simprints.id.tools.GuidValidator
import com.simprints.libsimprints.Constants


class VerifyIdParameter(calloutParameters: CalloutParameters,
                        private val typeParameter: TypeParameter,
                        private val guidValidator: GuidValidator = GuidValidator(),
                        defaultValue: String = "")
    : CalloutExtraParameter<String>(calloutParameters, Constants.SIMPRINTS_VERIFY_GUID, defaultValue) {

    override fun validate() {
        if (isVerify()) {
            validateValueIsNotMissing()
            validateValueIsGuid()
        } else {
            validateValueIsMissing()
        }
    }

    private fun isVerify(): Boolean =
        typeParameter.value == CalloutAction.VERIFY

    private fun validateValueIsNotMissing() {
        if (isMissing) {
            throw InvalidCalloutError(ALERT_TYPE.MISSING_VERIFY_GUID)
        }
    }

    private fun validateValueIsGuid() {
        if (!guidValidator.isGuid(value)) {
            throw InvalidCalloutError(ALERT_TYPE.INVALID_VERIFY_GUID)
        }
    }

    private fun validateValueIsMissing() {
        if (!isMissing) {
            throw InvalidCalloutError(ALERT_TYPE.UNEXPECTED_PARAMETER)
        }
    }
}
