package com.simprints.feature.clientapi.mappers.request.validators

import com.simprints.feature.clientapi.exceptions.InvalidRequestException
import com.simprints.feature.clientapi.mappers.request.requestFactories.EnrolLastBiometricsActionFactory
import com.simprints.feature.clientapi.mappers.request.requestFactories.RequestActionFactory
import com.simprints.infra.events.EventRepository
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

internal class EnrolLastBiometricsValidatorTest : ActionRequestValidatorTest(EnrolLastBiometricsActionFactory) {
    private val mockExtractor = EnrolLastBiometricsActionFactory.getMockExtractor()

    @Test
    fun `should fail if no identification callback in session`() = runTest {
        val mockEventRepository = mockk<EventRepository>()
        coEvery { mockEventRepository.getEventsFromScope(any()) } returns emptyList()
        val validator = EnrolLastBiometricsValidator(mockExtractor, RequestActionFactory.MOCK_SESSION_ID, mockEventRepository)
        assertThrows<InvalidRequestException> {
            validator.validate()
        }
    }

    @Test
    fun `should fail if no sessionId`() = runTest {
        every { mockExtractor.getSessionId() } returns ""
        assertThrows<InvalidRequestException> {
            EnrolLastBiometricsActionFactory.getValidator(mockExtractor).validate()
        }
    }

    @Test
    fun `should fail if sessionId does not match`() = runTest {
        every { mockExtractor.getSessionId() } returns "otherSessionId"
        assertThrows<InvalidRequestException> {
            EnrolLastBiometricsActionFactory.getValidator(mockExtractor).validate()
        }
    }
}
