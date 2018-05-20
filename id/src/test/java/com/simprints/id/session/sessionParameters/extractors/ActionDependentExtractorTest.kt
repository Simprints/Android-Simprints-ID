package com.simprints.id.session.sessionParameters.extractors

import com.simprints.id.session.callout.Callout
import com.simprints.id.session.callout.CalloutAction
import shared.mock
import shared.whenever
import org.junit.Assert.assertEquals
import org.junit.Test

class ActionDependentExtractorTest {

    private val registerCallout = Callout(CalloutAction.REGISTER, mock())
    private val identifyCallout = Callout(CalloutAction.IDENTIFY, mock())

    private val registerValue = "registerValue"
    private val defaultValue = "defaultValue"

    private val extractor = mockExtractor()

    private val actionDependentExtractor = ActionDependentExtractor(
        mapOf(registerCallout.action to extractor),
        defaultValue
    )

    private fun mockExtractor(): Extractor<String> {
        val extractor = mock<Extractor<String>>()
        whenever(extractor.extractFrom(registerCallout)).thenReturn(registerValue)
        return extractor
    }

    @Test
    fun testExtractFromUsesCorrectExtractorFromSwitch() {
        assertEquals(registerValue, actionDependentExtractor.extractFrom(registerCallout))
    }

    @Test
    fun testExtractFromReturnsDefaultValueWhenActionNotInSwitch() {
        assertEquals(defaultValue, actionDependentExtractor.extractFrom(identifyCallout))
    }

}
