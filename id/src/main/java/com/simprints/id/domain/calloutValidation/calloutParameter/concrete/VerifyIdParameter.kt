package com.simprints.id.domain.calloutValidation.calloutParameter.concrete

import android.content.Intent
import com.simprints.id.domain.calloutValidation.CalloutType
import com.simprints.id.domain.calloutValidation.calloutParameter.CalloutExtraParameter
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.model.ALERT_TYPE
import com.simprints.id.tools.GuidValidator
import com.simprints.libsimprints.Constants


class VerifyIdParameter(intent: Intent,
                        private val typeParameter: TypeParameter,
                        private val guidValidator: GuidValidator = GuidValidator(),
                        defaultValue: String = "")
    : CalloutExtraParameter<String>(intent, Constants.SIMPRINTS_VERIFY_GUID, defaultValue) {

    override fun validate() {
        if (isVerify()) {
            validateValueIsNotMissing()
            validateValueIsGuid()
        } else {
            validateValueIsMissing()
        }
    }

    private fun isVerify(): Boolean =
        typeParameter.value == CalloutType.VERIFY

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
