package com.simprints.id.domain.sessionParameters.validators


interface Validator <in T: Any> {

    fun validate(value: T)

}
