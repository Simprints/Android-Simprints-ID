package com.simprints.id.domain.sessionParameters.readers

import com.simprints.id.domain.callout.Callout
import com.simprints.id.domain.callout.CalloutParameter
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.exceptions.unsafe.InvalidCalloutParameterTypeError
import com.simprints.id.model.ALERT_TYPE


class OptionalParameterReader<out T: Any>(private val key: String,
                                          private val defaultValue: T,
                                          private val alertWhenInvalidType: ALERT_TYPE): Reader<T> {

    override fun readFrom(callout: Callout): T =
        callout.parameters
            .getOrDefault(key, defaultValue)
            .tryCast()

    private fun CalloutParameter.tryCast() =
        try {
            castAs(defaultValue::class)
        } catch (error: InvalidCalloutParameterTypeError) {
            throw InvalidCalloutError(alertWhenInvalidType)
        }

}
