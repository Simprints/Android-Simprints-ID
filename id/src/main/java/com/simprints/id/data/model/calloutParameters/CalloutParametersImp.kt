package com.simprints.id.data.model.calloutParameters

import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.data.model.calloutParameter.CalloutParameter

abstract class CalloutParametersImp: CalloutParameters {

    protected abstract val parameters: Collection<CalloutParameter<*>>

    @Throws(InvalidCalloutError::class)
    override fun validate() {
        for (parameter in parameters) {
            parameter.validate()
        }
    }


}