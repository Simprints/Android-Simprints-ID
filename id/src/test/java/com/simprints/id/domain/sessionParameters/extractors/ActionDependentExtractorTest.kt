package com.simprints.id.domain.sessionParameters.extractors

import com.simprints.id.domain.callout.Callout
import com.simprints.id.domain.callout.CalloutAction
import com.simprints.id.testUtils.mock
import com.simprints.id.testUtils.whenever
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
