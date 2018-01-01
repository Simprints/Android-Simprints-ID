package com.simprints.id.domain.sessionParameters.calloutParameter.extras

import com.simprints.id.domain.callout.CalloutParameter
import com.simprints.id.domain.callout.CalloutParameters
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.model.ALERT_TYPE
import com.simprints.id.testUtils.assertThrows
import org.junit.Assert.assertEquals
import org.junit.Test


class UnexpectedExtrasParameterTest {

    private val expectedCalloutParameter = CalloutParameter("expectedKey", "expectedValue")
    private val unexpectedCalloutParameter1 = CalloutParameter("unexpectedKey1", "unexpectedValue1")
    private val unexpectedCalloutParameter2 = CalloutParameter("unexpectedKey2", "unexpectedValue2")

    private val expectedKeys = setOf(expectedCalloutParameter.key)

    private val expectedCalloutParameters = CalloutParameters(setOf(expectedCalloutParameter))
    private val unexpectedCalloutParameters = CalloutParameters(setOf(expectedCalloutParameter,
        unexpectedCalloutParameter1, unexpectedCalloutParameter2))

    private val unexpectedParametersMap = mapOf(
        unexpectedCalloutParameter1.key to unexpectedCalloutParameter1.value,
        unexpectedCalloutParameter2.key to unexpectedCalloutParameter2.value)

    @Test
    fun testValueIsEmptyWhenNoUnexpectedCalloutParameters() {
        val unexpectedExtraParam = UnexpectedExtrasParameter(expectedCalloutParameters, expectedKeys)
        assertEquals(emptyMap<String, Any?>(), unexpectedExtraParam.value)
    }

    @Test
    fun testValueContainsUnexpectedExtrasWhenUnexpectedCalloutParameters() {
        val unexpectedExtraParam = UnexpectedExtrasParameter(unexpectedCalloutParameters, expectedKeys)
        assertEquals(unexpectedParametersMap, unexpectedExtraParam.value)
    }

    @Test
    fun testValidateSucceedsWhenNoUnexpectedCalloutParameters() {
        val unexpectedExtraParam = UnexpectedExtrasParameter(expectedCalloutParameters, expectedKeys)
        unexpectedExtraParam.validate()
    }

    @Test
    fun testValidateThrowsErrorWhenUnexpectedCalloutParameters() {
        val unexpectedExtraParam = UnexpectedExtrasParameter(unexpectedCalloutParameters, expectedKeys)
        val throwable = assertThrows<InvalidCalloutError> {
            unexpectedExtraParam.validate()
        }
        assertEquals(ALERT_TYPE.UNEXPECTED_PARAMETER, throwable.alertType)
    }

}
