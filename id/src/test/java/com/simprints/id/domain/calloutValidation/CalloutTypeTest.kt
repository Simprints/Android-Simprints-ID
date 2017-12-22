package com.simprints.id.domain.calloutValidation

import com.simprints.id.domain.calloutValidation.CalloutType
import com.simprints.id.testUtils.assertThrows
import org.junit.Assert.assertEquals
import org.junit.Test
import java.security.InvalidParameterException

class CalloutTypeTest {

    @Test
    fun forIntentActionReturnsCalloutTypeForValidInput() {
        for (calloutType in CalloutType.values()) {
            assertEquals(calloutType, CalloutType.forAction(calloutType.intentAction))
        }
    }

    @Test
    fun forIntentActionThrowsErrorForInvalidInput() {
        assertThrows<InvalidParameterException> {
            CalloutType.forAction("Invalid intent action")
        }
    }

}
