package com.simprints.id.exceptions.safe.callout

import com.simprints.id.exceptions.safe.SafeException
import kotlin.reflect.KClass

class InvalidCalloutParameterTypeError(message: String = "InvalidCalloutParameterTypeError") : SafeException(message) {

    companion object {

        fun <T: Any, U: Any> forClasses(excepted: KClass<T>, actual: KClass<U>?): InvalidCalloutParameterTypeError {
            return InvalidCalloutParameterTypeError("Got $actual, expected $excepted")
        }

    }

}
