package com.simprints.id.session.sessionParameters.readers.unexpectedParameters

import com.simprints.id.session.callout.Callout
import com.simprints.id.session.callout.CalloutAction
import com.simprints.id.session.callout.CalloutParameter
import com.simprints.id.session.callout.CalloutParameters
import shared.mock
import org.junit.Assert
import org.junit.Test

class UnexpectedParametersReaderTest {

    private val anyCalloutAction = mock<CalloutAction>()

    private val unexpectedCalloutParameter1 = CalloutParameter("unexpectedKey1", "unexpectedValue1")
    private val unexpectedCalloutParameter2 = CalloutParameter("unexpectedKey2", 42)
    private val unexpectedCalloutParameters = arrayOf(unexpectedCalloutParameter1,
        unexpectedCalloutParameter2)

    private val calloutWithoutUnexpectedParameters =
        Callout(anyCalloutAction, CalloutParameters(emptySet()))

    private val calloutWithUnexpectedParameters =
        Callout(anyCalloutAction, CalloutParameters(setOf(*unexpectedCalloutParameters)))

    private val expectedKeys = setOf("expectedKey1", "expectedKey2")

    private val expectedParametersLister = object : ExpectedParametersLister {

        override fun listKeysOfExpectedParametersIn(callout: Callout): Set<String> =
            expectedKeys

    }

    private val unexpectedParametersReader = UnexpectedParametersReader(expectedParametersLister)

    @Test
    fun testReadFromReturnsSetOfUnexpectedParameterWhenCalloutContainsUnexpectedParameter() {
        Assert.assertEquals(setOf(*unexpectedCalloutParameters),
            unexpectedParametersReader.readFrom(calloutWithUnexpectedParameters))
    }

    @Test
    fun testReadFromReturnsEmptySetWhenCalloutDoesNotContainUnexpectedParameter() {
        Assert.assertEquals(emptySet<CalloutParameter>(),
            unexpectedParametersReader.readFrom(calloutWithoutUnexpectedParameters))
    }

}
