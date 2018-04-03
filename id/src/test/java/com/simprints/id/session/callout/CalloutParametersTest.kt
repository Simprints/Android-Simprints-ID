package com.simprints.id.session.callout

import android.content.Intent
import com.simprints.id.session.callout.CalloutParameters.Companion.calloutParameters
import com.simprints.id.exceptions.unsafe.MissingCalloutParameterError
import com.simprints.id.testUtils.assertThrows
import org.junit.Assert.*
import org.junit.Test


class CalloutParametersTest {

    private val emptySet = emptySet<CalloutParameter>()

    private val missingKey = "missingKey"
    private val defaultValue = "defaultValue"
    private val defaultCalloutParameter = CalloutParameter(missingKey, defaultValue)

    private val key1 = "key1"
    private val value1 = "value1"
    private val calloutParameter1 = CalloutParameter(key1, value1)

    private val key2 = "key2"
    private val value2 = 42
    private val calloutParameter2 = CalloutParameter(key2, value2)

    private val key3 = "key3"
    private val value3 = null
    private val calloutParameter3 = CalloutParameter(key3, value3)


    private val calloutParameters = setOf(calloutParameter1, calloutParameter2, calloutParameter3)
    private val keyValuePairs = arrayOf(key1 to value1, key2 to value2, key3 to value3)

    @Test
    fun testGetThrowsExceptionOnEmptyCalloutParameters() {
        val emptyCalloutParameters = CalloutParameters(emptySet)
        assertThrows<MissingCalloutParameterError> {
            emptyCalloutParameters[missingKey]
        }
    }

    @Test
    fun testGetThrowsExceptionWhenKeyNotInCalloutParameters() {
        val calloutParameters = CalloutParameters(calloutParameters)
        assertThrows<MissingCalloutParameterError> {
            calloutParameters[missingKey]
        }
    }

    @Test
    fun testGetReturnsCorrespondingValueWhenKeyInCalloutParameters() {
        val calloutParameters = CalloutParameters(calloutParameters)
        assertEquals(calloutParameter1, calloutParameters[key1])
    }

    @Test
    fun testGetReturnsCorrespondingValueWhenKeyInCalloutParametersAndValueIsNull() {
        val calloutParameters = CalloutParameters(calloutParameters)
        assertEquals(calloutParameter3, calloutParameters[key3])
    }

    @Test
    fun testGetOrDefaultReturnsDefaultOnEmptyCalloutParameters() {
        val emptyCalloutParameters = CalloutParameters(emptySet)
        assertEquals(defaultCalloutParameter,
            emptyCalloutParameters.getOrDefault(missingKey, defaultValue))
    }

    @Test
    fun testGetOrDefaultReturnsDefaultWhenKeyNotInCalloutParameters() {
        val calloutParameters = CalloutParameters(calloutParameters)
        assertEquals(defaultCalloutParameter,
            calloutParameters.getOrDefault(missingKey, defaultValue))
    }

    @Test
    fun testGetOrDefaultReturnsCorrespondingValueWhenKeyInCalloutParameters() {
        val calloutParameters = CalloutParameters(calloutParameters)
        assertEquals(calloutParameter1, calloutParameters.getOrDefault(key1, defaultValue))
    }

    @Test
    fun testGetOrDefaultReturnsCorrespondingValueWhenKeyInCalloutParametersAndValueIsNull() {
        val calloutParameters = CalloutParameters(calloutParameters)
        assertEquals(calloutParameter3, calloutParameters.getOrDefault(key3, defaultValue))
    }

    @Test
    fun testContainsReturnsFalseWhenKeyNotInCalloutParameters() {
        val calloutParameters = CalloutParameters(calloutParameters)
        assertFalse(missingKey in calloutParameters)
    }

    @Test
    fun testContainsReturnsTrueWhenKeyInCalloutParameters() {
        val calloutParameters = CalloutParameters(calloutParameters)
        assertTrue(key1 in calloutParameters)
    }

    @Test
    fun testGetCalloutParametersOnNullReturnsEmptyCalloutParameters() {
        val nullIntent: Intent? = null
        assertEquals(CalloutParameters(emptySet), nullIntent.calloutParameters)
    }

    @Test
    fun testGetCalloutParametersOnEmptyIntentReturnsEmptyCalloutParameters() {
        val emptyIntent: Intent = mockIntent()
        assertEquals(CalloutParameters(emptySet), emptyIntent.calloutParameters)
    }

    @Test
    fun testGetCalloutParametersOnIntentWithExtraReturnsCalloutParametersWithCorrespondingValue() {
        val intentWithExtra = mockIntent(*keyValuePairs)
        assertEquals(CalloutParameters(calloutParameters), intentWithExtra.calloutParameters)
    }

}
