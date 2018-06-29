package com.simprints.id.session.sessionParameters.validators


class ValueValidator<in T: Any>(private val validValues: List<T>,
                                private val errorWhenInvalid: Error) : Validator<T>{

    override fun validate(value: T) {
        if (value !in validValues) {
            throw errorWhenInvalid
        }
    }

}

