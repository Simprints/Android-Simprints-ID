package com.simprints.id.exceptions.unsafe

import kotlin.reflect.KClass

class InvalidCalloutParameterTypeError(message: String = "InvalidCalloutParameterTypeError") : Error(message) {

    companion object {

        fun <T: Any, U: Any> forClasses(excepted: KClass<T>, actual: KClass<U>?): InvalidCalloutParameterTypeError {
            return InvalidCalloutParameterTypeError("Got $actual, expected $excepted")
        }

    }

}
