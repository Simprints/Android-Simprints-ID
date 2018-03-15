package com.simprints.id.session.sessionParameters.readers

import com.simprints.id.session.callout.Callout
import com.simprints.id.session.callout.CalloutAction
import com.simprints.id.session.callout.CalloutParameter
import com.simprints.id.session.callout.CalloutParameters
import com.simprints.id.testUtils.assertThrows
import com.simprints.id.testUtils.mock
import org.junit.Assert
import org.junit.Test


class OptionalParameterReaderTest {

    private val anyCalloutAction = mock<CalloutAction>()

    private val key = "key"
    private val value = "value"
    private val defaultStringValue = "defaultValue"
    private val defaultIntValue = 42

    private val calloutParameter = CalloutParameter(key, value)

    private val emptyCallout = Callout(anyCalloutAction, CalloutParameters(setOf()))
    private val calloutWithStringParameter = Callout(anyCalloutAction, CalloutParameters(setOf(calloutParameter)))

    private val invalidParameterTypeError = Error()

    private val optionalStringParameterReader = OptionalParameterReader(key, defaultStringValue,
        invalidParameterTypeError)

    private val optionalIntParameterReader = OptionalParameterReader(key, defaultIntValue,
        invalidParameterTypeError)

    @Test
    fun testReadFromSucceedsWhenCalloutContainsParameterWithValidType() {
        Assert.assertEquals(value, optionalStringParameterReader.readFrom(calloutWithStringParameter))
    }

    @Test
    fun testReadFromReturnsDefaultWhenCalloutDoesNotContainParameter() {
        Assert.assertEquals(defaultStringValue, optionalStringParameterReader.readFrom(emptyCallout))
    }

    @Test
    fun testReadFromThrowsErrorWhenCalloutContainsParameterWithInvalidType() {
        assertThrows(invalidParameterTypeError) {
            optionalIntParameterReader.readFrom(calloutWithStringParameter)
        }
    }

}
