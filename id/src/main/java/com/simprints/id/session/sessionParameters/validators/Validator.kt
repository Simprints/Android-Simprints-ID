package com.simprints.id.session.sessionParameters.validators


interface Validator <in T: Any> {

    fun validate(value: T)

}
