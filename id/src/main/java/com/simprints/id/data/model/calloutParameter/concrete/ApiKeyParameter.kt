package com.simprints.id.data.model.calloutParameter.concrete

import android.content.Intent
import com.simprints.id.data.model.calloutParameter.CalloutExtraParameter
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.model.ALERT_TYPE
import com.simprints.id.tools.GuidValidator
import com.simprints.libsimprints.Constants


class ApiKeyParameter(intent: Intent,
                      private val guidValidator: GuidValidator = GuidValidator(),
                      defaultValue: String = "")
    : CalloutExtraParameter<String>(intent, Constants.SIMPRINTS_API_KEY, defaultValue) {

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
