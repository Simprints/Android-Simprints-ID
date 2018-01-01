package com.simprints.id.domain.sessionParameters.calloutParameter

import com.simprints.id.domain.callout.CalloutParameter
import com.simprints.id.domain.callout.CalloutParameters
import com.simprints.id.domain.sessionParameters.calloutParameter.extras.CalloutExtraParameter
import org.junit.Assert.*
import org.junit.Test

class CalloutExtraParameterTest {

    companion object {
        private val key = "key"
        private val stringValue = "value"
        private val stringDefaultValue = "defaultValue"
        private val intDefaultValue = 314
    }

    class StringParameterWithoutValidation(calloutParameters: CalloutParameters)
        : CalloutExtraParameter<String>(calloutParameters, key, stringDefaultValue)  {

        override fun validate() { }
    }

    class IntParameterWithoutValidation(calloutParameters: CalloutParameters)
        : CalloutExtraParameter<Int>(calloutParameters, key, intDefaultValue)  {

        override fun validate() { }
    }

    private val emptyCalloutParameters = CalloutParameters(emptySet())

    private val stringParam = CalloutParameter(key, stringValue)
    private val calloutParametersWithStringParam = CalloutParameters(setOf(stringParam))

    @Test
    fun testParameterValueIsExtraValueWhenExtraIsInIntent() {
        val stringParameter = StringParameterWithoutValidation(calloutParametersWithStringParam)
        assertEquals(stringValue, stringParameter.value)
    }

    @Test
    fun testParameterValueIsDefaultValueWhenExtraIsNotInIntent() {
        val stringParameter = StringParameterWithoutValidation(emptyCalloutParameters)
        assertEquals(stringDefaultValue, stringParameter.value)
    }

    @Test
    fun testParameterValueIsDefaultValueWhenExtraHasInvalidType() {
        val intParameter = IntParameterWithoutValidation(calloutParametersWithStringParam)
        assertEquals(intDefaultValue, intParameter.value)

    }

    @Test
    fun testIsMissingIsTrueWhenExtraIsNotInIntent() {
        val stringParameter = StringParameterWithoutValidation(emptyCalloutParameters)
        assertTrue(stringParameter.isMissing)
    }

    @Test
    fun testIsMissingIsFalseWhenExtraIsInIntent() {
        val stringParameter = StringParameterWithoutValidation(calloutParametersWithStringParam)
        assertFalse(stringParameter.isMissing)
    }

    @Test
    fun testIsMismatchedTypeIsTrueWhenExtraHasInvalidType() {
        val intParameter = IntParameterWithoutValidation(calloutParametersWithStringParam)
        assertTrue(intParameter.isMismatchedType)
    }

    @Test
    fun testIsMismatchedTypeIsTrueWhenExtraHasValidType() {
        val stringParameter = StringParameterWithoutValidation(calloutParametersWithStringParam)
        assertFalse(stringParameter.isMismatchedType)
    }

}
