package com.simprints.id.domain.calloutValidation.calloutParameter.concrete

import android.content.Intent
import com.simprints.id.domain.calloutValidation.CalloutType
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.model.ALERT_TYPE
import com.simprints.id.testUtils.assertThrows
import com.simprints.id.testUtils.mock
import com.simprints.id.testUtils.whenever
import org.junit.Assert.assertEquals
import org.junit.Test

class TypeParameterTest {

    companion object {
        private val invalidIntentAction = "haltAndCatchFire"
    }

    private fun mockIntentWithAction(action: String?): Intent {
        val intent = mock<Intent>()
        whenever(intent.action).thenReturn(action)
        return intent
    }

    @Test
    fun testTypeValueIsInvalidOrMissingValueWhenIntentActionIsNull() {
        val intentWithNullAction: Intent = mockIntentWithAction(null)
        val typeParameter = TypeParameter(intentWithNullAction)
        assertEquals(CalloutType.INVALID_OR_MISSING, typeParameter.value)
    }

    @Test
    fun testTypeValueIsInvalidOrMissingValueWhenIntentActionIsInvalid() {
        val intentWithInvalidAction: Intent = mockIntentWithAction(invalidIntentAction)
        val typeParameter = TypeParameter(intentWithInvalidAction)
        assertEquals(CalloutType.INVALID_OR_MISSING, typeParameter.value)
    }

    @Test
    fun testTypeValueIsCorrespondingCalloutTypeWhenIntentActionIsValid() {
        val type = CalloutType.UPDATE
        val intentWithValidAction: Intent = mockIntentWithAction(type.intentAction)
        val typeParameter = TypeParameter(intentWithValidAction)
        assertEquals(type, typeParameter.value)
    }

    @Test
    fun testValidateThrowsExpectedExceptionWhenIntentActionIsNull() {
        val intentWithNullAction: Intent = mockIntentWithAction(null)
        val typeParameter = TypeParameter(intentWithNullAction)
        val exception = assertThrows<InvalidCalloutError> {
            typeParameter.validate()
        }
        assertEquals(ALERT_TYPE.INVALID_INTENT_ACTION, exception.alertType)
    }

    @Test
    fun testValidateThrowsExpectedExceptionWhenIntentActionIsInvalid() {
        val intentWithInvalidAction: Intent = mockIntentWithAction(invalidIntentAction)
        val typeParameter = TypeParameter(intentWithInvalidAction)
        val exception = assertThrows<InvalidCalloutError> {
            typeParameter.validate()
        }
        assertEquals(ALERT_TYPE.INVALID_INTENT_ACTION, exception.alertType)
    }

    @Test
    fun testValidateDoesNotThrowsExceptionWhenIntentActionIsValid() {
        val type = CalloutType.UPDATE
        val intentWithValidAction: Intent = mockIntentWithAction(type.intentAction)
        val typeParameter = TypeParameter(intentWithValidAction)
        typeParameter.validate()
    }

}
