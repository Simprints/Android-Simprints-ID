package com.simprints.id.data.model.calloutParameter.concrete

import android.content.Intent
import com.simprints.id.data.model.calloutParameter.mockIntent
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.model.ALERT_TYPE
import com.simprints.id.testUtils.assertThrows
import com.simprints.libsimprints.Constants.SIMPRINTS_ODK_RESULT_FORMAT_V01
import com.simprints.libsimprints.Constants.SIMPRINTS_RESULT_FORMAT
import org.junit.Assert
import org.junit.Test


class ResultFormatParameterTest {

    private val validResultFormat: String = SIMPRINTS_ODK_RESULT_FORMAT_V01
    private val invalidResultFormat: String = "invalidResultFormat"

    private val emptyIntent: Intent = mockIntent()
    private val invalidResultFormatIntent: Intent = mockIntent(SIMPRINTS_RESULT_FORMAT to invalidResultFormat)
    private val validResultFormatIntent: Intent = mockIntent(SIMPRINTS_RESULT_FORMAT to validResultFormat)

    @Test
    fun testValidateSucceedsWhenValueIsMissing() {
        val resultFormatParam = ResultFormatParameter(emptyIntent)
        resultFormatParam.validate()
    }

    @Test
    fun testValidateSucceedsWhenValueIsValid() {
        val resultFormatParam = ResultFormatParameter(validResultFormatIntent)
        resultFormatParam.validate()
    }

    @Test
    fun testValidateThrowsErrorWhenValueIsInvalid() {
        val resultFormatParam = ResultFormatParameter(invalidResultFormatIntent)
        val throwable = assertThrows<InvalidCalloutError> {
            resultFormatParam.validate()
        }
        Assert.assertEquals(ALERT_TYPE.INVALID_RESULT_FORMAT, throwable.alertType)
    }

}
