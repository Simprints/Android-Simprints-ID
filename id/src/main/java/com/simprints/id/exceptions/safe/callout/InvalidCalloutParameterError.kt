package com.simprints.id.exceptions.safe.callout

import com.simprints.id.exceptions.safe.SafeException

@Deprecated("Callout will be removed soon")
class InvalidCalloutParameterError(message: String = "InvalidCalloutParameterError")
    : SafeException(message) {

    companion object {

        fun forParameter(parameterName: String) =
            InvalidCalloutParameterError("CalloutParameters parameter $parameterName is invalid")

    }

}
