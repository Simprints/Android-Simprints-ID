package com.simprints.id.session.sessionParameters.validators

import com.simprints.id.shared.assertThrows
import org.junit.Test

class ValueValidatorTest {

    private val validValue1 = "validValue1"
    private val validValue2 = "validValue2"
    private val invalidValue = "invalidValue"

    private val validValues = listOf(validValue1, validValue2)

    private val invalidValueError = Error()
    private val valueValidator = ValueValidator(validValues, invalidValueError)

    @Test
    fun testValidateSucceedsOnValidValue() {
        valueValidator.validate(validValue1)
    }

    @Test
    fun testValidateThrowsErrorOnInvalidMetadata() {
        assertThrows(invalidValueError) {
            valueValidator.validate(invalidValue)
        }
    }
}
