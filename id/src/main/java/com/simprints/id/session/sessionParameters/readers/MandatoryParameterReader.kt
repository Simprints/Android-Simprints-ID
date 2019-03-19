package com.simprints.id.session.sessionParameters.readers

import com.simprints.id.exceptions.safe.SafeException
import com.simprints.id.exceptions.safe.callout.InvalidCalloutParameterTypeError
import com.simprints.id.exceptions.safe.callout.MissingCalloutParameterError
import com.simprints.id.session.callout.Callout
import com.simprints.id.session.callout.CalloutParameter
import com.simprints.id.session.callout.CalloutParameters
import kotlin.reflect.KClass


class MandatoryParameterReader<out T: Any>(private val key: String,
                                           private val kClass: KClass<T>,
                                           private val errorWhenMissing: SafeException,
                                           private val errorWhenInvalidType: SafeException): Reader<T> {

    override fun readFrom(callout: Callout): T =
        callout.parameters
            .tryGet()
            .tryCast()

    private fun CalloutParameters.tryGet(): CalloutParameter =
        try {
            get(key)
        } catch (error: MissingCalloutParameterError) {
            throw errorWhenMissing
        }

    private fun CalloutParameter.tryCast() =
        try {
            castAs(kClass)
        } catch (error: InvalidCalloutParameterTypeError) {
            throw errorWhenInvalidType
        }

}
