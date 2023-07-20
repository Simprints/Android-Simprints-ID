package com.simprints.clientapi.clientrequests.validators

import com.simprints.clientapi.exceptions.InvalidSessionIdException
import com.simprints.clientapi.exceptions.InvalidStateForIntentAction
import com.simprints.clientapi.requestFactories.EnrolLastBiometricsFactory
import com.simprints.clientapi.requestFactories.RequestFactory
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.every
import org.junit.Test

class EnrolLastBiometricsValidatorTest : AppRequestValidatorTest(EnrolLastBiometricsFactory) {

    private val mockExtractor = EnrolLastBiometricsFactory.getMockExtractor()

    @Test
    fun givenNotIdentificationAsLastFlow_enrolLastBiometricsReceived_shouldThrowAnException() {
        val validator =
            EnrolLastBiometricsValidator(mockExtractor, RequestFactory.MOCK_SESSION_ID, false)
        assertThrows<InvalidStateForIntentAction> {
            validator.validateClientRequest()
        }
    }

    @Test
    fun aRequestWithoutSessionIdReceived_shouldThrowAnException() {
        every { mockExtractor.getSessionId() } returns ""
        assertThrows<InvalidSessionIdException> {
            EnrolLastBiometricsFactory.getValidator(mockExtractor).validateClientRequest()
        }
    }
}
