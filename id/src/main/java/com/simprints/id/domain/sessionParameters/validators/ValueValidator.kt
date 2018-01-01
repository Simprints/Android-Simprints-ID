package com.simprints.id.domain.sessionParameters.validators

import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.model.ALERT_TYPE


class ValueValidator<in T: Any>(private val validValues: List<T>,
                                private val alertWhenInvalid: ALERT_TYPE) : Validator<T>{

    override fun validate(value: T) {
        if (value !in validValues) {
            throw InvalidCalloutError(alertWhenInvalid)
        }
    }

}

