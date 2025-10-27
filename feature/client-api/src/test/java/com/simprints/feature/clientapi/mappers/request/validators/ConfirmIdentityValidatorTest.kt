package com.simprints.feature.clientapi.mappers.request.validators

import com.simprints.feature.clientapi.exceptions.InvalidRequestException
import com.simprints.feature.clientapi.mappers.request.requestFactories.ConfirmIdentityActionFactory
import com.simprints.feature.clientapi.mappers.request.requestFactories.RequestActionFactory
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.callback.CallbackComparisonScore
import com.simprints.infra.events.event.domain.models.callback.IdentificationCallbackEvent
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

internal class ConfirmIdentityValidatorTest : ActionRequestValidatorTest(ConfirmIdentityActionFactory) {
    override fun `should fail if no moduleId`() {}

    override fun `should fail with illegal moduleId`() {}

    override fun `should fail if no userId`() {}

    override fun `should fail with illegal metadata`() {}

    @Test
    fun `should fail if no sessionId`() = runTest {
        val extractor = ConfirmIdentityActionFactory.getMockExtractor()
        every { extractor.getSessionId() } returns ""

        assertThrows<InvalidRequestException> {
            ConfirmIdentityActionFactory.getValidator(extractor).validate()
        }
    }

    @Test
    fun `should fail if no selectedGuid`() = runTest {
        val extractor = ConfirmIdentityActionFactory.getMockExtractor()
        every { extractor.getSelectedGuid() } returns ""

        assertThrows<InvalidRequestException> {
            ConfirmIdentityActionFactory.getValidator(extractor).validate()
        }
    }

    @Test
    fun `should fail if no identification callback in session`() = runTest {
        val extractor = ConfirmIdentityActionFactory.getMockExtractor()
        val mockEventRepository = mockk<EventRepository>()
        coEvery { mockEventRepository.getEventsFromScope(any()) } returns emptyList()
        val validator = ConfirmIdentityValidator(
            extractor,
            RequestActionFactory.MOCK_SESSION_ID,
            mockEventRepository,
        )
        assertThrows<InvalidRequestException> {
            validator.validate()
        }
    }

    @Test
    fun `should fail if invalid sessionId`() = runTest {
        val extractor = ConfirmIdentityActionFactory.getMockExtractor()
        val mockEventRepository = mockk<EventRepository>()
        coEvery { mockEventRepository.getEventsFromScope(any()) } returns emptyList()
        val validator = ConfirmIdentityValidator(
            extractor,
            "anotherSessionID",
            mockEventRepository,
        )
        assertThrows<InvalidRequestException> {
            validator.validate()
        }
    }

    @Test
    fun `should succeed when selected GUID is in identification results`() = runTest {
        val extractor = ConfirmIdentityActionFactory.getMockExtractor()
        every { extractor.getSelectedGuid() } returns "valid-guid-123"

        val mockEventRepository = mockk<EventRepository>()
        val mockEvent = mockk<IdentificationCallbackEvent>()
        val mockPayload = mockk<IdentificationCallbackEvent.IdentificationCallbackPayload>()
        val mockScore1 = mockk<CallbackComparisonScore>()
        val mockScore2 = mockk<CallbackComparisonScore>()

        every { mockScore1.guid } returns "other-guid-456"
        every { mockScore2.guid } returns "valid-guid-123"
        every { mockPayload.scores } returns listOf(mockScore1, mockScore2)
        every { mockEvent.payload } returns mockPayload

        coEvery { mockEventRepository.getEventsFromScope(any()) } returns listOf(mockEvent)

        val validator = ConfirmIdentityValidator(
            extractor,
            RequestActionFactory.MOCK_SESSION_ID,
            mockEventRepository,
        )

        // Should not throw
        validator.validate()
    }

    @Test
    fun `should fail when selected GUID is not in identification results`() = runTest {
        val extractor = ConfirmIdentityActionFactory.getMockExtractor()
        every { extractor.getSelectedGuid() } returns "invalid-guid-999"

        val mockEventRepository = mockk<EventRepository>()
        val mockEvent = mockk<IdentificationCallbackEvent>()
        val mockPayload = mockk<IdentificationCallbackEvent.IdentificationCallbackPayload>()
        val mockScore1 = mockk<CallbackComparisonScore>()
        val mockScore2 = mockk<CallbackComparisonScore>()

        every { mockScore1.guid } returns "valid-guid-123"
        every { mockScore2.guid } returns "valid-guid-456"
        every { mockPayload.scores } returns listOf(mockScore1, mockScore2)
        every { mockEvent.payload } returns mockPayload

        coEvery { mockEventRepository.getEventsFromScope(any()) } returns listOf(mockEvent)

        val validator = ConfirmIdentityValidator(
            extractor,
            RequestActionFactory.MOCK_SESSION_ID,
            mockEventRepository,
        )

        assertThrows<InvalidRequestException> {
            validator.validate()
        }
    }

    @Test
    fun `should fail when identification results have no scores`() = runTest {
        val extractor = ConfirmIdentityActionFactory.getMockExtractor()
        every { extractor.getSelectedGuid() } returns "any-guid"

        val mockEventRepository = mockk<EventRepository>()
        val mockEvent = mockk<IdentificationCallbackEvent>()
        val mockPayload = mockk<IdentificationCallbackEvent.IdentificationCallbackPayload>()

        every { mockPayload.scores } returns emptyList()
        every { mockEvent.payload } returns mockPayload

        coEvery { mockEventRepository.getEventsFromScope(any()) } returns listOf(mockEvent)

        val validator = ConfirmIdentityValidator(
            extractor,
            RequestActionFactory.MOCK_SESSION_ID,
            mockEventRepository,
        )

        assertThrows<InvalidRequestException> {
            validator.validate()
        }
    }

    @Test
    fun `should succeed if selectedGuid is NONE_SELECTED`() = runTest {
        val extractor = ConfirmIdentityActionFactory.getMockExtractor()
        every { extractor.getSelectedGuid() } returns "NONE_SELECTED"
        val mockEventRepository = mockk<EventRepository>()
        val mockEvent = mockk<IdentificationCallbackEvent>()
        val mockPayload = mockk<IdentificationCallbackEvent.IdentificationCallbackPayload>()
        every { mockPayload.scores } returns emptyList()
        every { mockEvent.payload } returns mockPayload
        coEvery { mockEventRepository.getEventsFromScope(any()) } returns listOf(mockEvent)
        val validator = ConfirmIdentityValidator(
            extractor,
            RequestActionFactory.MOCK_SESSION_ID,
            mockEventRepository,
        )
        // Should not throw
        validator.validate()
    }
}
