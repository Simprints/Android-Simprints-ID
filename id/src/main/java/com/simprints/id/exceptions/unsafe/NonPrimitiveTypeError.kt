package com.simprints.id.exceptions.unsafe


class NonPrimitiveTypeError(message: String = "NonPrimitiveTypeError"): SimprintsError(message) {

    companion object {

        fun <T: Any> forTypeOf(value: T) =
                NonPrimitiveTypeError("${value.javaClass.simpleName} is not a primitive type")

    }

}
