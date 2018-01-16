package com.simprints.id.domain.sessionParameters.validators

import org.junit.Test


class NoOpValidatorTest {

    private val anyValue: Any = "anyValue"
    private val noOpValidator = NoOpValidator<Any>()

    @Test
    fun testValidateAlwaysSucceeds() {
        noOpValidator.validate(anyValue)
    }
}
