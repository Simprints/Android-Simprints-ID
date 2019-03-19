package com.simprints.id.session.sessionParameters.validators

import com.simprints.id.exceptions.safe.SafeException


class ValueValidator<in T: Any>(private val validValues: List<T>,
                                private val errorWhenInvalid: SafeException) : Validator<T>{

    override fun validate(value: T) {
        if (value !in validValues) {
            throw errorWhenInvalid
        }
    }

}

