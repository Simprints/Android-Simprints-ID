package com.simprints.id.domain.calloutValidation.calloutParameters

import com.simprints.id.domain.calloutValidation.calloutParameter.CalloutParameter
import com.simprints.id.exceptions.unsafe.InvalidCalloutError


abstract class CalloutParametersImp: CalloutParameters {

    protected abstract val parameters: Collection<CalloutParameter<*>>

    @Throws(InvalidCalloutError::class)
    override fun validate() {
        for (parameter in parameters) {
            parameter.validate()
        }
    }


}
