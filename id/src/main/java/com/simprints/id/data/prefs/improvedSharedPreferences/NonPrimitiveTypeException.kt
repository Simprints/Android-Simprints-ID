package com.simprints.id.data.prefs.improvedSharedPreferences


class NonPrimitiveTypeException(message: String): Exception(message) {

    companion object {

        fun <T: Any> forTypeOf(value: T) =
                NonPrimitiveTypeException("${value.javaClass.simpleName} is not a primitive type")

    }

}
