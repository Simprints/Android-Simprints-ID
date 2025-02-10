package com.simprints.feature.clientapi.mappers.request.validators

import com.simprints.feature.clientapi.exceptions.InvalidRequestException
import com.simprints.feature.clientapi.mappers.request.requestFactories.EnrolLastBiometricsActionFactory
import com.simprints.feature.clientapi.mappers.request.requestFactories.RequestActionFactory
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.every
import org.junit.Test

internal class EnrolLastBiometricsValidatorTest : ActionRequestValidatorTest(EnrolLastBiometricsActionFactory) {
    private val mockExtractor = EnrolLastBiometricsActionFactory.getMockExtractor()

    @Test
    fun `should fail if no identification callback in session`() {
        val validator = EnrolLastBiometricsValidator(mockExtractor, RequestActionFactory.MOCK_SESSION_ID, false)
        assertThrows<InvalidRequestException> {
            validator.validate()
        }
    }

    @Test
    fun `should fail if no sessionId`() {
        every { mockExtractor.getSessionId() } returns ""
        assertThrows<InvalidRequestException> {
            EnrolLastBiometricsActionFactory.getValidator(mockExtractor).validate()
        }
    }

    @Test
    fun `should fail if sessionId does not match`() {
        every { mockExtractor.getSessionId() } returns "otherSessionId"
        assertThrows<InvalidRequestException> {
            EnrolLastBiometricsActionFactory.getValidator(mockExtractor).validate()
        }
    }
}
