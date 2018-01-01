package com.simprints.id.domain.callout

import android.content.Intent
import com.simprints.id.domain.callout.CalloutAction
import com.simprints.id.domain.callout.CalloutAction.Companion.calloutAction
import com.simprints.id.domain.sessionParameters.calloutParameter.mockIntent
import org.junit.Assert.assertEquals
import org.junit.Test


class CalloutActionTest {

    @Test
    fun testGetCalloutActionOnNullReturnsMISSING() {
        val nullIntent: Intent? = null
        assertEquals(CalloutAction.MISSING, nullIntent.calloutAction)
    }

    @Test
    fun testGetCalloutActionOnIntentWithoutActionReturnsMISSING() {
        val intentWithoutAction: Intent = mockIntent(null)
        assertEquals(CalloutAction.MISSING, intentWithoutAction.calloutAction)
    }

    @Test
    fun testGetCalloutActionOnIntentWithValidActionReturnsCorrespondingCalloutAction() {
        for (calloutAction in CalloutAction.values()) {
            val validAction = calloutAction.toString()
            val intentWithValidAction = mockIntent(validAction)
            assertEquals(calloutAction, intentWithValidAction.calloutAction)
        }
    }

    @Test
    fun testGetCalloutActionOnIntentWithInvalidActionReturnsINVALID() {
        val invalidAction = "invalidAction"
        val intentWithInvalidAction = mockIntent(invalidAction)
        assertEquals(CalloutAction.INVALID, intentWithInvalidAction.calloutAction)
    }

}
