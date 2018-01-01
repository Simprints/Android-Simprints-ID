package com.simprints.id.domain.sessionParameters.readers

import com.simprints.id.domain.callout.Callout
import com.simprints.id.domain.callout.CalloutParameter
import com.simprints.id.domain.callout.CalloutParameters
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.exceptions.unsafe.InvalidCalloutParameterTypeError
import com.simprints.id.exceptions.unsafe.MissingCalloutParameterError
import com.simprints.id.model.ALERT_TYPE
import kotlin.reflect.KClass


class MandatoryParameterReader<out T: Any>(private val key: String,
                                           private val kClass: KClass<T>,
                                           private val alertWhenMissing: ALERT_TYPE,
                                           private val alertWhenInvalidType: ALERT_TYPE): Reader<T> {

    override fun readFrom(callout: Callout): T =
        callout.parameters
            .tryGet()
            .tryCast()

    private fun CalloutParameters.tryGet(): CalloutParameter =
        try {
            get(key)
        } catch (error: MissingCalloutParameterError) {
            throw InvalidCalloutError(alertWhenMissing)
        }

    private fun CalloutParameter.tryCast() =
        try {
            castAs(kClass)
        } catch (error: InvalidCalloutParameterTypeError) {
            throw InvalidCalloutError(alertWhenInvalidType)
        }

}
