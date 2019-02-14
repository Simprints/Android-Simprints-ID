package com.simprints.id.exceptions.unexpected


class NonPrimitiveTypeError(message: String = "NonPrimitiveTypeError"): UnexpectedException(message) {

    companion object {

        fun <T: Any> forTypeOf(value: T) =
                NonPrimitiveTypeError("${value.javaClass.simpleName} is not a primitive type")

    }

}
