package com.simprints.id.domain.sessionParameters.validators


class NoOpValidator<in T: Any> : Validator<T>{

    override fun validate(value: T) { }

}
