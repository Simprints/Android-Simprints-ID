package com.simprints.id.exceptions


class InvalidCalloutParameterException(message: String): RuntimeException(message) {

    companion object {

        fun forParameter(parameterName: String) =
                InvalidCalloutParameterException("Callout parameter $parameterName is invalid")

    }

}