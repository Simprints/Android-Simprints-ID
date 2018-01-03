package com.simprints.id.domain.sessionParameters.readers

import com.simprints.id.domain.callout.Callout
import com.simprints.id.domain.callout.CalloutAction
import com.simprints.id.testUtils.mock
import com.simprints.id.testUtils.whenever
import org.junit.Assert.assertEquals
import org.junit.Test


class ActionDependentReaderTest {

    private val registerCallout = Callout(CalloutAction.REGISTER, mock())
    private val identifyCallout = Callout(CalloutAction.IDENTIFY, mock())

    private val registerValue = "registerValue"
    private val defaultValue = "defaultValue"

    private val reader = mockReader()

    private val actionDependentReader = ActionDependentReader(
        mapOf(registerCallout.action to reader),
        defaultValue
    )

    private fun mockReader(): Reader<String> {
        val reader = mock<Reader<String>>()
        whenever(reader.readFrom(registerCallout)).thenReturn(registerValue)
        return reader
    }

    @Test
    fun testReadFromUsesCorrectReaderFromSwitch() {
        assertEquals(registerValue, actionDependentReader.readFrom(registerCallout))
    }

    @Test
    fun testReadFromReturnsDefaultValueWhenActionNotInSwitch() {
        assertEquals(defaultValue, actionDependentReader.readFrom(identifyCallout))
    }

}
