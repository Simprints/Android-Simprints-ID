package com.simprints.id.domain.sessionParameters.readers

import com.simprints.id.domain.callout.Callout
import com.simprints.id.domain.callout.CalloutAction
import com.simprints.id.domain.callout.CalloutParameters
import com.simprints.id.testUtils.mock
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
