package com.simprints.id.session.sessionParameters.validators

import com.simprints.id.shared.assertThrows
import org.junit.Test

class GuidValidatorTest {

    private val validGuid: String = "6dfd1cee-0fe0-4894-9518-ea39e26d9bec"
    private val invalidGuid: String = "invalidGuid"
    private val invalidGuidError = Error()
    private val guidValidator = GuidValidator(invalidGuidError)

    @Test
    fun testValidateSucceedsOnValidGuid() {
        guidValidator.validate(validGuid)
    }

    @Test
    fun testValidateThrowsErrorOnInvalidGuid() {
        assertThrows(invalidGuidError) {
            guidValidator.validate(invalidGuid)
        }
    }

}
