package com.simprints.id.data.model.calloutParameter.concrete

import android.content.Intent
import com.simprints.id.data.model.calloutParameter.CalloutExtraParameter
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.model.ALERT_TYPE
import com.simprints.libsimprints.Constants


class ResultFormatParameter(intent: Intent)
    : CalloutExtraParameter<String>(intent, Constants.SIMPRINTS_RESULT_FORMAT, "") {

    companion object {
        private val VALID_RESULT_FORMATS = listOf(Constants.SIMPRINTS_ODK_RESULT_FORMAT_V01)
    }

    override fun validate() {
        if (!isMissing) {
            validateValueIsValidResultFormat()
        }
    }

    private fun validateValueIsValidResultFormat() {
        if (value !in VALID_RESULT_FORMATS) {
            throw InvalidCalloutError(ALERT_TYPE.INVALID_RESULT_FORMAT)
        }
    }

}
