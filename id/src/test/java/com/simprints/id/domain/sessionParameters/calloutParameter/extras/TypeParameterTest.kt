package com.simprints.id.domain.sessionParameters.calloutParameter.extras

import com.simprints.id.domain.callout.CalloutAction
import com.simprints.id.domain.sessionParameters.calloutParameter.TypeParameter
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.model.ALERT_TYPE
import com.simprints.id.testUtils.assertThrows
import org.junit.Assert.assertEquals
import org.junit.Test

class TypeParameterTest {

    @Test
    fun testValidateThrowsExpectedExceptionWhenActionIsMISSING() {
        val typeParameter = TypeParameter(CalloutAction.MISSING)
        val exception = assertThrows<InvalidCalloutError> {
            typeParameter.validate()
        }
        assertEquals(ALERT_TYPE.INVALID_INTENT_ACTION, exception.alertType)
    }

    @Test
    fun testValidateThrowsExpectedExceptionWhenActionIsINVALID() {
        val typeParameter = TypeParameter(CalloutAction.INVALID)
        val exception = assertThrows<InvalidCalloutError> {
            typeParameter.validate()
        }
        assertEquals(ALERT_TYPE.INVALID_INTENT_ACTION, exception.alertType)
    }

    @Test
    fun testValidateDoesNotThrowsExceptionWhenActionIsValid() {
        CalloutAction.validValues
            .map { TypeParameter(it) }
            .forEach { it.validate() }
    }

}
