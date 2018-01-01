package com.simprints.id.domain.sessionParameters.calloutParameter.extras

import com.simprints.id.domain.callout.CalloutParameter
import com.simprints.id.domain.callout.CalloutParameters
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.model.ALERT_TYPE
import com.simprints.id.testUtils.assertThrows
import com.simprints.libsimprints.Constants.SIMPRINTS_ODK_RESULT_FORMAT_V01
import com.simprints.libsimprints.Constants.SIMPRINTS_RESULT_FORMAT
import org.junit.Assert
import org.junit.Test


class ResultFormatParameterTest {

    private val validResultFormat =
        CalloutParameter(SIMPRINTS_RESULT_FORMAT, SIMPRINTS_ODK_RESULT_FORMAT_V01)
    private val invalidResultFormat =
        CalloutParameter(SIMPRINTS_RESULT_FORMAT, "invalidResultFormat")

    private val emptyCalloutParameters = CalloutParameters(emptySet())
    private val calloutParametersWithValidResultFormat =
        CalloutParameters(setOf(validResultFormat))
    private val calloutParametersWithInvalidResultFormat =
        CalloutParameters(setOf(invalidResultFormat))

    @Test
    fun testValidateSucceedsWhenValueIsMissing() {
        val resultFormatParam = ResultFormatParameter(emptyCalloutParameters)
        resultFormatParam.validate()
    }

    @Test
    fun testValidateSucceedsWhenValueIsValid() {
        val resultFormatParam = ResultFormatParameter(calloutParametersWithValidResultFormat)
        resultFormatParam.validate()
    }

    @Test
    fun testValidateThrowsErrorWhenValueIsInvalid() {
        val resultFormatParam = ResultFormatParameter(calloutParametersWithInvalidResultFormat)
        val throwable = assertThrows<InvalidCalloutError> {
            resultFormatParam.validate()
        }
        Assert.assertEquals(ALERT_TYPE.INVALID_RESULT_FORMAT, throwable.alertType)
    }

}
