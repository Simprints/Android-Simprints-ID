package com.simprints.id.exceptions.safe


class NonPrimitiveTypeException(message: String): RuntimeException(message) {

    companion object {

        fun <T: Any> forTypeOf(value: T) =
                NonPrimitiveTypeException("${value.javaClass.simpleName} is not a primitive type")

    }

}
