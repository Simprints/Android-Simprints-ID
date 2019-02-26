package com.simprints.id.exceptions.unexpected


class NonPrimitiveTypeException(message: String = "NonPrimitiveTypeException"): UnexpectedException(message) {

    companion object {

        fun <T: Any> forTypeOf(value: T) =
                NonPrimitiveTypeException("${value.javaClass.simpleName} is not a primitive type")

    }

}
