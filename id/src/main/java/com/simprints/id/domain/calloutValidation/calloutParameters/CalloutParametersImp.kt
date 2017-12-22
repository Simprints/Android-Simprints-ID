package com.simprints.id.domain.calloutValidation.calloutParameters

import com.simprints.id.domain.calloutValidation.calloutParameter.CalloutParameter
import com.simprints.id.domain.calloutValidation.calloutParameter.concrete.UnexpectedExtrasParameter
import com.simprints.id.exceptions.unsafe.InvalidCalloutError


class CalloutParametersImp(expectedParameters: Array<CalloutParameter<*>>,
                           override val unexpectedExtrasParameter: UnexpectedExtrasParameter)
    : CalloutParameters {

    private val parameters = arrayOf(*expectedParameters, unexpectedExtrasParameter)

    @Throws(InvalidCalloutError::class)
    override fun validate() {
        for (parameter in parameters) {
            parameter.validate()
        }
    }

}
