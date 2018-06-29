package com.simprints.id.session.sessionParameters.validators


class NoOpValidator<in T: Any> : Validator<T>{

    override fun validate(value: T) { }

}
