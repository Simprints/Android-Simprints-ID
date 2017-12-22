package com.simprints.id.domain.calloutValidation.calloutParameter.concrete

import android.content.Intent
import com.simprints.id.domain.calloutValidation.calloutParameter.mockIntent
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.model.ALERT_TYPE
import com.simprints.id.testUtils.assertThrows
import com.simprints.libsimprints.Constants.SIMPRINTS_API_KEY
import org.junit.Assert.assertEquals
import org.junit.Test


class ApiKeyParameterTest {

    private val validGuid: String = "6dfd1cee-0fe0-4894-9518-ea39e26d9bec"
    private val invalidGuid: String = "api-key-001"

    private val emptyIntent: Intent = mockIntent()
    private val invalidApiKeyIntent: Intent = mockIntent(SIMPRINTS_API_KEY to invalidGuid)
    private val validApiKeyIntent: Intent = mockIntent(SIMPRINTS_API_KEY to validGuid)

    @Test
    fun testValidateThrowsErrorWhenValueIsMissing() {
        val apiKeyParameter = ApiKeyParameter(emptyIntent)
        val throwable = assertThrows<InvalidCalloutError> {
            apiKeyParameter.validate()
        }
        assertEquals(ALERT_TYPE.MISSING_API_KEY, throwable.alertType)
    }

    @Test
    fun testValidateThrowsErrorWhenValueIsNotAGuid() {
        val apiKeyParameter = ApiKeyParameter(invalidApiKeyIntent)
        val throwable = assertThrows<InvalidCalloutError> {
            apiKeyParameter.validate()
        }
        assertEquals(ALERT_TYPE.INVALID_API_KEY, throwable.alertType)
    }

    @Test
    fun testValidateSucceedsWhenValueIsValid() {
        val apiKeyParameter = ApiKeyParameter(validApiKeyIntent)
        apiKeyParameter.validate()
    }

}
