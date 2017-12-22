package com.simprints.id.domain.calloutValidation.calloutParameter

import android.content.Intent
import org.junit.Assert.*
import org.junit.Test

class CalloutExtraParameterTest {

    companion object {
        val paramKey = "key"
        val stringParamValue = "value"
        val stringParamDefaultValue = "defaultValue"
        val intParamDefaultValue = 314
    }

    class StringParameterWithoutValidation(intent: Intent)
        : CalloutExtraParameter<String>(intent, paramKey, stringParamDefaultValue)  {

        override fun validate() { }
    }

    class IntParameterWithoutValidation(intent: Intent)
        : CalloutExtraParameter<Int>(intent, paramKey, intParamDefaultValue)  {

        override fun validate() {

        }
    }

    private val emptyIntent: Intent =
            mockIntent()

    private val intentWithStringParam: Intent =
            mockIntent(paramKey to stringParamValue)

    @Test
    fun testParameterValueIsExtraValueWhenExtraIsInIntent() {
        val stringParameter = StringParameterWithoutValidation(intentWithStringParam)
        assertEquals(stringParamValue, stringParameter.value)
    }

    @Test
    fun testParameterValueIsDefaultValueWhenExtraIsNotInIntent() {
        val stringParameter = StringParameterWithoutValidation(emptyIntent)
        assertEquals(stringParamDefaultValue, stringParameter.value)
    }

    @Test
    fun testParameterValueIsDefaultValueWhenExtraHasInvalidType() {
        val intParameter = IntParameterWithoutValidation(intentWithStringParam)
        assertEquals(intParamDefaultValue, intParameter.value)

    }

    @Test
    fun testIsMissingIsTrueWhenExtraIsNotInIntent() {
        val stringParameter = StringParameterWithoutValidation(emptyIntent)
        assertTrue(stringParameter.isMissing)
    }

    @Test
    fun testIsMissingIsFalseWhenExtraIsInIntent() {
        val stringParameter = StringParameterWithoutValidation(intentWithStringParam)
        assertFalse(stringParameter.isMissing)
    }

    @Test
    fun testIsMismatchedTypeIsTrueWhenExtraHasInvalidType() {
        val intParameter = IntParameterWithoutValidation(intentWithStringParam)
        assertTrue(intParameter.isMismatchedType)
    }

    @Test
    fun testIsMismatchedTypeIsTrueWhenExtraHasValidType() {
        val stringParameter = StringParameterWithoutValidation(intentWithStringParam)
        assertFalse(stringParameter.isMismatchedType)
    }

}
