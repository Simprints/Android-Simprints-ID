package com.simprints.id.domain.calloutValidation.calloutParameter.concrete

import android.content.Intent
import com.simprints.id.domain.calloutValidation.calloutParameter.mockIntent
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.model.ALERT_TYPE
import com.simprints.id.testUtils.assertThrows
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test


class UnexpectedExtrasParameterTest {

    private val expectedKey = "expectedKey"
    private val expectedValue = "expectedValue"
    private val unexpectedKey1 = "unexpectedKey1"
    private val unexpectedValue1 = "unexpectedValue1"
    private val unexpectedKey2 = "unexpectedKey2"
    private val unexpectedValue2 = "unexpectedValue2"

    private val expectedExtraKeys = listOf(expectedKey)
    private val expectedExtras = arrayOf(expectedKey to expectedValue)
    private val oneUnexpectedExtra = arrayOf(unexpectedKey1 to unexpectedValue1)
    private val severalUnexpectedExtras = arrayOf(unexpectedKey1 to unexpectedValue1, unexpectedKey2 to unexpectedValue2)

    private val noUnexpectedExtrasIntent: Intent = mockIntent(*expectedExtras)
    private val oneUnexpectedExtrasIntent: Intent = mockIntent(*expectedExtras, *oneUnexpectedExtra)
    private val severalUnexpectedExtrasIntent: Intent = mockIntent(*expectedExtras, *severalUnexpectedExtras)

    @Test
    fun testValueIsEmptyWhenIntentHasNoUnexpectedExtras() {
        val unexpectedExtraParam = UnexpectedExtrasParameter(noUnexpectedExtrasIntent, expectedExtraKeys)
        assertTrue(unexpectedExtraParam.value.isEmpty())
    }

    @Test
    fun testValueContainsUnexpectedExtraWhenIntentHasOneUnexpectedExtra() {
        val unexpectedExtraParam = UnexpectedExtrasParameter(oneUnexpectedExtrasIntent, expectedExtraKeys)
        assertEquals(mapOf(*oneUnexpectedExtra), unexpectedExtraParam.value)
    }

    @Test
    fun testValueContainsUnexpectedExtrasWhenIntentHasSeveralUnexpectedExtra() {
        val unexpectedExtraParam = UnexpectedExtrasParameter(severalUnexpectedExtrasIntent, expectedExtraKeys)
        assertEquals(mapOf(*severalUnexpectedExtras), unexpectedExtraParam.value)
    }

    @Test
    fun testValidateSucceedsWhenIntentHasNoUnexpectedExtras() {
        val unexpectedExtraParam = UnexpectedExtrasParameter(noUnexpectedExtrasIntent, expectedExtraKeys)
        unexpectedExtraParam.validate()
    }

    @Test
    fun testValidateThrowsErrorWhenIntentHasUnexpectedExtra() {
        val unexpectedExtraParam = UnexpectedExtrasParameter(oneUnexpectedExtrasIntent, expectedExtraKeys)
        val throwable = assertThrows<InvalidCalloutError> {
            unexpectedExtraParam.validate()
        }
        assertEquals(ALERT_TYPE.UNEXPECTED_PARAMETER, throwable.alertType)
    }

}
