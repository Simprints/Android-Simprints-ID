package com.simprints.id.session.sessionParameters.readers

import com.simprints.id.session.callout.Callout
import com.simprints.id.session.callout.CalloutAction
import com.simprints.id.session.callout.CalloutParameters
import com.simprints.testtools.common.syntax.mock
import org.junit.Assert.assertEquals
import org.junit.Test

class ActionReaderTest {

    private val anyCalloutAction = mock<CalloutAction>()
    private val anyCalloutParameters = mock<CalloutParameters>()
    private val callout = Callout(anyCalloutAction, anyCalloutParameters)
    private val actionReader: Reader<CalloutAction> = ActionReader()

    @Test
    fun testReadFromCalloutReturnsCalloutAction() {
        assertEquals(anyCalloutAction, actionReader.readFrom(callout))
    }

}
