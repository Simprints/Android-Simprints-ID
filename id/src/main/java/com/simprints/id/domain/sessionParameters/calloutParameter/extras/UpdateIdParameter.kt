package com.simprints.id.domain.sessionParameters.calloutParameter.extras

import com.simprints.id.domain.callout.CalloutAction
import com.simprints.id.domain.callout.CalloutParameters
import com.simprints.id.domain.sessionParameters.calloutParameter.TypeParameter
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.model.ALERT_TYPE
import com.simprints.id.tools.GuidValidator
import com.simprints.libsimprints.Constants


class UpdateIdParameter(calloutParameters: CalloutParameters,
                        private val typeParameter: TypeParameter,
                        private val guidValidator: GuidValidator = GuidValidator(),
                        defaultValue: String = "")
    : CalloutExtraParameter<String>(calloutParameters, Constants.SIMPRINTS_UPDATE_GUID, defaultValue) {

    override fun validate() {
        if (isUpdate()) {
            validateValueIsNotMissing()
            validateValueIsGuid()
        } else {
            validateValueIsMissing()
        }
    }

    private fun isUpdate(): Boolean =
        typeParameter.value == CalloutAction.UPDATE

    private fun validateValueIsNotMissing() {
        if (isMissing) {
            throw InvalidCalloutError(ALERT_TYPE.MISSING_UPDATE_GUID)
        }
    }

    private fun validateValueIsGuid() {
        if (!guidValidator.isGuid(value)) {
            throw InvalidCalloutError(ALERT_TYPE.INVALID_UPDATE_GUID)
        }
    }

    private fun validateValueIsMissing() {
        if (!isMissing) {
            throw InvalidCalloutError(ALERT_TYPE.UNEXPECTED_PARAMETER)
        }
    }

}
