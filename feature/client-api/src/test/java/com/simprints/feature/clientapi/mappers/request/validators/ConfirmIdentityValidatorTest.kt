package com.simprints.feature.clientapi.mappers.request.validators

import com.simprints.feature.clientapi.exceptions.InvalidRequestException
import com.simprints.feature.clientapi.mappers.request.requestFactories.ConfirmIdentityActionFactory
import com.simprints.feature.clientapi.mappers.request.requestFactories.RequestActionFactory
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.CallbackComparisonScore
import com.simprints.infra.events.event.domain.models.IdentificationCallbackEvent
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Before
import org.junit.Test

internal class ConfirmIdentityValidatorTest : ActionRequestValidatorTest(ConfirmIdentityActionFactory) {
    @MockK
    private lateinit var mockEventRepository: EventRepository

    @MockK
    private lateinit var mockCallback: IdentificationCallbackEvent

    @MockK
    private lateinit var mockCallbackPayload: IdentificationCallbackEvent.IdentificationCallbackPayload

    @MockK
    private lateinit var mockScore1: CallbackComparisonScore

    @MockK
    private lateinit var mockScore2: CallbackComparisonScore

    @MockK
    private lateinit var mockProjectConfig: ProjectConfiguration

    @MockK(relaxed = true)
    private lateinit var mockConfigRepository: ConfigRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

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
        coEvery { mockEventRepository.getEventsFromScope(any()) } returns emptyList()
        val validator = ConfirmIdentityValidator(
            extractor,
            RequestActionFactory.MOCK_SESSION_ID,
            mockEventRepository,
            configRepository = mockConfigRepository,
        )
        assertThrows<InvalidRequestException> {
            validator.validate()
        }
    }

    @Test
    fun `should fail if invalid sessionId`() = runTest {
        val extractor = ConfirmIdentityActionFactory.getMockExtractor()
        coEvery { mockEventRepository.getEventsFromScope(any()) } returns emptyList()
        val validator = ConfirmIdentityValidator(
            extractor,
            "anotherSessionID",
            mockEventRepository,
            configRepository = mockConfigRepository,
        )
        assertThrows<InvalidRequestException> {
            validator.validate()
        }
    }

    @Test
    fun `should succeed when selected GUID is in identification results`() = runTest {
        val extractor = ConfirmIdentityActionFactory.getMockExtractor()
        every { extractor.getSelectedGuid() } returns "valid-guid-123"

        every { mockScore1.guid } returns "other-guid-456"
        every { mockScore2.guid } returns "valid-guid-123"
        every { mockCallbackPayload.scores } returns listOf(mockScore1, mockScore2)
        every { mockCallback.payload } returns mockCallbackPayload

        coEvery { mockEventRepository.getEventsFromScope(any()) } returns listOf(mockCallback)

        val validator = ConfirmIdentityValidator(
            extractor,
            RequestActionFactory.MOCK_SESSION_ID,
            mockEventRepository,
            configRepository = mockConfigRepository,
        )

        // Should not throw
        validator.validate()
    }

    @Test
    fun `should fail when selected GUID is not in identification results`() = runTest {
        val extractor = ConfirmIdentityActionFactory.getMockExtractor()
        every { extractor.getSelectedGuid() } returns "invalid-guid-999"

        every { mockScore1.guid } returns "valid-guid-123"
        every { mockScore2.guid } returns "valid-guid-456"
        every { mockCallbackPayload.scores } returns listOf(mockScore1, mockScore2)
        every { mockCallback.payload } returns mockCallbackPayload

        coEvery { mockEventRepository.getEventsFromScope(any()) } returns listOf(mockCallback)

        val validator = ConfirmIdentityValidator(
            extractor,
            RequestActionFactory.MOCK_SESSION_ID,
            mockEventRepository,
            configRepository = mockConfigRepository,
        )

        assertThrows<InvalidRequestException> {
            validator.validate()
        }
    }

    @Test
    fun `should fail when identification results have no scores`() = runTest {
        val extractor = ConfirmIdentityActionFactory.getMockExtractor()
        every { extractor.getSelectedGuid() } returns "any-guid"

        every { mockCallbackPayload.scores } returns emptyList()
        every { mockCallback.payload } returns mockCallbackPayload

        coEvery { mockEventRepository.getEventsFromScope(any()) } returns listOf(mockCallback)

        val validator = ConfirmIdentityValidator(
            extractor,
            RequestActionFactory.MOCK_SESSION_ID,
            mockEventRepository,
            configRepository = mockConfigRepository,
        )

        assertThrows<InvalidRequestException> {
            validator.validate()
        }
    }

    @Test
    fun `should succeed if selectedGuid is NONE_SELECTED`() = runTest {
        val extractor = ConfirmIdentityActionFactory.getMockExtractor()
        every { extractor.getSelectedGuid() } returns "NONE_SELECTED"
        every { mockCallbackPayload.scores } returns emptyList()
        every { mockCallback.payload } returns mockCallbackPayload
        coEvery { mockEventRepository.getEventsFromScope(any()) } returns listOf(mockCallback)
        val validator = ConfirmIdentityValidator(
            extractor,
            RequestActionFactory.MOCK_SESSION_ID,
            mockEventRepository,
            configRepository = mockConfigRepository,
        )
        // Should not throw
        validator.validate()
    }

    @Test
    fun `should succeed when GUID not in results but skip feature flag is enabled`() = runTest {
        val extractor = ConfirmIdentityActionFactory.getMockExtractor()
        every { extractor.getSelectedGuid() } returns "guid-not-in-results"

        every { mockScore1.guid } returns "different-guid"
        every { mockCallbackPayload.scores } returns listOf(mockScore1)
        every { mockCallback.payload } returns mockCallbackPayload
        coEvery { mockEventRepository.getEventsFromScope(any()) } returns listOf(mockCallback)

        // Mock ConfigManager with feature flag enabled
        every { mockProjectConfig.custom } returns mapOf("allowConfirmingGuidsNotInCallback" to JsonPrimitive(true))
        coEvery { mockConfigRepository.getProjectConfiguration() } returns mockProjectConfig

        val validator = ConfirmIdentityValidator(
            extractor,
            RequestActionFactory.MOCK_SESSION_ID,
            mockEventRepository,
            mockConfigRepository,
        )

        // Should not throw despite GUID not being in results
        validator.validate()
    }

    @Test
    fun `should fail when GUID not in results and feature flag is disabled`() = runTest {
        val extractor = ConfirmIdentityActionFactory.getMockExtractor()
        every { extractor.getSelectedGuid() } returns "guid-not-in-results"

        every { mockScore1.guid } returns "different-guid"
        every { mockCallbackPayload.scores } returns listOf(mockScore1)
        every { mockCallback.payload } returns mockCallbackPayload
        coEvery { mockEventRepository.getEventsFromScope(any()) } returns listOf(mockCallback)

        // Mock ConfigManager with feature flag disabled
        every { mockProjectConfig.custom } returns mapOf("allowConfirmingGuidsNotInCallback" to JsonPrimitive(false))
        coEvery { mockConfigRepository.getProjectConfiguration() } returns mockProjectConfig

        val validator = ConfirmIdentityValidator(
            extractor,
            RequestActionFactory.MOCK_SESSION_ID,
            mockEventRepository,
            mockConfigRepository,
        )

        // Should throw because GUID is not in results and flag is disabled
        assertThrows<InvalidRequestException> {
            validator.validate()
        }
    }
}
