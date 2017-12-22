package com.simprints.id.data.model.calloutParameter.concrete

import android.content.Intent
import com.simprints.id.data.model.CalloutType
import com.simprints.id.data.model.calloutParameter.CalloutExtraParameter
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.model.ALERT_TYPE
import com.simprints.id.tools.GuidValidator
import com.simprints.libsimprints.Constants


class UpdateIdParameter(intent: Intent,
                        private val typeParameter: TypeParameter,
                        private val guidValidator: GuidValidator = GuidValidator(),
                        defaultValue: String = "")
    : CalloutExtraParameter<String>(intent, Constants.SIMPRINTS_UPDATE_GUID, defaultValue) {

    override fun validate() {
        if (isUpdate()) {
            validateValueIsNotMissing()
            validateValueIsGuid()
        } else {
            validateValueIsMissing()
        }
    }

    private fun isUpdate(): Boolean =
        typeParameter.value == CalloutType.UPDATE

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
