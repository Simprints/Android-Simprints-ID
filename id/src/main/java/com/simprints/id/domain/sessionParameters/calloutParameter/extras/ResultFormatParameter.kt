package com.simprints.id.domain.sessionParameters.calloutParameter.extras

import com.simprints.id.domain.callout.CalloutParameters
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.model.ALERT_TYPE
import com.simprints.libsimprints.Constants


class ResultFormatParameter(calloutParameters: CalloutParameters,
                            private val validResultFormats: List<String> = VALID_RESULT_FORMATS,
                            defaultValue: String = "")
    : CalloutExtraParameter<String>(calloutParameters, Constants.SIMPRINTS_RESULT_FORMAT, defaultValue) {

    companion object {
        private val VALID_RESULT_FORMATS = listOf(Constants.SIMPRINTS_ODK_RESULT_FORMAT_V01)
    }

    override fun validate() {
        if (!isMissing) {
            validateValueIsValidResultFormat()
        }
    }

    private fun validateValueIsValidResultFormat() {
        if (value !in validResultFormats) {
            throw InvalidCalloutError(ALERT_TYPE.INVALID_RESULT_FORMAT)
        }
    }

}
