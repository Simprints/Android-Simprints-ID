package com.simprints.id.exceptions.unsafe


class InvalidCalloutParameterError(message: String = "InvalidCalloutParameterError")
    : SimprintsError(message) {

    companion object {

        fun forParameter(parameterName: String) =
                InvalidCalloutParameterError("CalloutParameters parameter $parameterName is invalid")

    }

}
