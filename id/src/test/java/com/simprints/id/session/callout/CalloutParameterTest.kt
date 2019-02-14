package com.simprints.id.session.callout

import com.simprints.id.exceptions.safe.callout.InvalidCalloutParameterTypeError
import com.simprints.id.shared.assertThrows
import org.junit.Assert.assertEquals
import org.junit.Test

class CalloutParameterTest {

    @Test
    fun testCastAsThrowExceptionWhenActualTypeIsNotRequiredType() {
        val intCalloutParameter = CalloutParameter("", 42)
        assertThrows<InvalidCalloutParameterTypeError> {
            intCalloutParameter.castAs(String::class)
        }
    }

    @Test
    fun testCastAsReturnsCorrectValueWhenActualTypeIsExactlyRequiredType() {
        val intValue = 42
        val intCalloutParameter = CalloutParameter("", intValue)
        assertEquals(intValue, intCalloutParameter.castAs(Int::class))
    }

    @Test
    fun testCastAsReturnsCorrectValueWhenActualTypeIsRequiredType() {
        val intValue = 42
        val intCalloutParameter = CalloutParameter("", intValue)
        assertEquals(intValue as Number, intCalloutParameter.castAs(Number::class))
    }

}
