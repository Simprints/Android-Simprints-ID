package com.simprints.id.domain.sessionParameters.calloutParameter.extras

import com.simprints.id.domain.callout.CalloutParameter
import com.simprints.id.domain.callout.CalloutParameters
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.model.ALERT_TYPE
import com.simprints.id.testUtils.assertThrows
import com.simprints.libsimprints.Constants.SIMPRINTS_API_KEY
import org.junit.Assert.assertEquals
import org.junit.Test


class ApiKeyParameterTest {

    private val validGuid: String = "6dfd1cee-0fe0-4894-9518-ea39e26d9bec"
    private val invalidGuid: String = "api-key-001"

    private val validApiKey = CalloutParameter(SIMPRINTS_API_KEY, validGuid)
    private val invalidApiKey = CalloutParameter(SIMPRINTS_API_KEY, invalidGuid)

    private val emptyCalloutParameters = CalloutParameters(emptySet())
    private val calloutParametersWithValidApiKey = CalloutParameters(setOf(validApiKey))
    private val calloutParametersWithInvalidApiKey = CalloutParameters(setOf(invalidApiKey))

    @Test
    fun testValidateThrowsErrorWhenValueIsMissing() {
        val apiKeyParameter = ApiKeyParameter(emptyCalloutParameters)
        val throwable = assertThrows<InvalidCalloutError> {
            apiKeyParameter.validate()
        }
        assertEquals(ALERT_TYPE.MISSING_API_KEY, throwable.alertType)
    }

    @Test
    fun testValidateThrowsErrorWhenValueIsNotAGuid() {
        val apiKeyParameter = ApiKeyParameter(calloutParametersWithInvalidApiKey)
        val throwable = assertThrows<InvalidCalloutError> {
            apiKeyParameter.validate()
        }
        assertEquals(ALERT_TYPE.INVALID_API_KEY, throwable.alertType)
    }

    @Test
    fun testValidateSucceedsWhenValueIsValid() {
        val apiKeyParameter = ApiKeyParameter(calloutParametersWithValidApiKey)
        apiKeyParameter.validate()
    }

}
