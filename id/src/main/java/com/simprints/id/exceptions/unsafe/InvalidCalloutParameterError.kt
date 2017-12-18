package com.simprints.id.exceptions.unsafe


class InvalidCalloutParameterError(message: String = "InvalidCalloutParameterError")
    : Error(message) {

    companion object {

        fun forParameter(parameterName: String) =
                InvalidCalloutParameterError("Callout parameter $parameterName is invalid")

    }

}
