package com.simprints.id.exceptions.safe.callout

import com.simprints.id.exceptions.safe.SafeException


class InvalidCalloutParameterError(message: String = "InvalidCalloutParameterError")
    : SafeException(message) {

    companion object {

        fun forParameter(parameterName: String) =
            InvalidCalloutParameterError("CalloutParameters parameter $parameterName is invalid")

    }

}
