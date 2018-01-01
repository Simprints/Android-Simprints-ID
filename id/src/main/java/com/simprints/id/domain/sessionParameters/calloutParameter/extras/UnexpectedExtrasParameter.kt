package com.simprints.id.domain.sessionParameters.calloutParameter.extras

import com.simprints.id.domain.callout.CalloutParameters
import com.simprints.id.domain.sessionParameters.calloutParameter.CalloutParameter
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.model.ALERT_TYPE


class UnexpectedExtrasParameter(calloutParameters: CalloutParameters, expectedExtraKeys: Collection<String>)
    : CalloutParameter<Map<String, Any?>> {

    override val value: Map<String, Any?> = calloutParameters
        .filterNot { it.key in expectedExtraKeys }
        .map { it.key to it.value }
        .toMap()

    override fun validate() {
        validateNoUnexpectedParameters()
    }

    private fun validateNoUnexpectedParameters() {
        if (value.isNotEmpty()) {
            throw InvalidCalloutError(ALERT_TYPE.UNEXPECTED_PARAMETER)
        }
    }

}
