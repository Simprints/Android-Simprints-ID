package com.simprints.id.session.sessionParameters.validators

import com.simprints.testtools.common.syntax.assertThrows
import org.junit.Test

class ModuleIdValidatorTest {

    private val validModuleId = "this is a valid module\n\r!£$%^&*(){}{::@?><,./#';[]'}"
    private val invalidModuleId = "this is not a valid module |"

    private val invalidModuleIdError = SafeException("invalid moduleid error")
    private val validator = ModuleIdValidator(invalidModuleIdError)

    @Test
    fun validModuleId_shouldNotThrowException() {
        validator.validate(validModuleId)
    }

    @Test
    fun invalidModuleId_shouldThrowException() {
        assertThrows(invalidModuleIdError) {
            validator.validate(invalidModuleId)
        }
    }
}
