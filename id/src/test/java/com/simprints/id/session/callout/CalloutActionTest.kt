package com.simprints.id.session.callout

import android.content.Intent
import com.simprints.id.session.callout.CalloutAction.Companion.calloutAction
import com.simprints.testtools.common.android.mockIntent
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
