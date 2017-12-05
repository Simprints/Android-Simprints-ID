package com.simprints.id.data.model.calloutParameter

import android.content.Intent
import android.os.Bundle
import com.simprints.id.testUtils.mock
import com.simprints.id.testUtils.whenever
import org.junit.Assert.*
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString

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
            mockIntentContaining(mockEmptyBundle())

    private val intentWithStringParam: Intent =
            mockIntentContaining(mockBundleWithSingleParam(paramKey, stringParamValue))

    private fun mockIntentContaining(bundle: Bundle): Intent {
        val intent = mock<Intent>()
        whenever(intent.extras).thenReturn(bundle)
        return intent
    }

    private fun mockEmptyBundle(): Bundle {
        val bundle = mock<Bundle>()
        whenever(bundle.get(anyString())).thenReturn(null)
        return bundle
    }

    private fun <T: Any> mockBundleWithSingleParam(paramKey: String, paramValue: T): Bundle {
        val bundle = mock<Bundle>()
        whenever(bundle.get(paramKey)).thenReturn(paramValue)
        return bundle
    }

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