package com.simprints.id.data.prefs.improvedSharedPreferences

/**
 * @author: Etienne Thiery (etienne@simprints.com)
 */
class NonPrimitiveTypeException(message: String): Exception(message) {

    companion object {

        fun <T: Any> forTypeOf(value: T) =
                NonPrimitiveTypeException("${value.javaClass.simpleName} is not a primitive type")

    }

}
