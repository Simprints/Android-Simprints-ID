package com.simprints.feature.datagenerator.events

import com.google.common.truth.Truth.*
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.scope.EventScope
import com.simprints.infra.events.event.domain.models.scope.EventScopeType
import io.mockk.*
import io.mockk.impl.annotations.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

class InsertSessionEventsUseCaseTest {
    private val testDispatcher = StandardTestDispatcher()

    @RelaxedMockK
    private lateinit var mockEventRepository: EventRepository

    @MockK
    private lateinit var mockSessionGenerator: SessionGenerator

    private lateinit var useCase: InsertSessionEventsUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkObject(Random.Default)
        useCase = InsertSessionEventsUseCase(
            eventRepository = mockEventRepository,
            sessionGenerator = mockSessionGenerator,
            dispatcher = testDispatcher,
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `invoke SHOULD insert all event types and emit correct progress messages`() = runTest(testDispatcher) {
        // GIVEN
        val projectId = "project-1"
        val moduleId = "module-1"
        val attendantId = "attendant-1"
        val enrolCount = 2
        val identifyCount = 1
        val verifyCount = 1
        val confirmIdentifyCount = 1
        val enrolLastCount = 1
        val totalNewSessions = enrolCount + identifyCount + verifyCount + confirmIdentifyCount + enrolLastCount

        val initialEventCount = 10
        val finalEventCount = 50 // Assuming we expect 40 new events to be generated
        val mockEventScope = mockk<EventScope>(relaxed = true)
        val mockDbCommands = listOf("INSERT INTO ...")

        // Mock initial and final event counts from the repository
        coEvery { mockEventRepository.observeEventCount(null) } returns flowOf(initialEventCount) andThen flowOf(finalEventCount)

        // Mock event scope creation and closing (suspend functions)
        coEvery { mockEventRepository.createEventScope(EventScopeType.SESSION) } returns mockEventScope
        coJustRun { mockEventRepository.closeEventScope(any<String>(), null) }

        // Mock event insertion (suspend function)
        coEvery { mockEventRepository.executeRawEventInsertions(any()) } returns Unit

        // Mock the Random generator to always choose the first path (ISO) in insertEnrollmentEvents
        every { Random.nextInt(2) } returns 0

        // Mock all session generator calls
        every { mockSessionGenerator.generateEnrolmentIso(any(), any(), any(), any()) } returns mockDbCommands
        every { mockSessionGenerator.generateIdentificationRoc3(any(), any(), any(), any()) } returns mockDbCommands
        every { mockSessionGenerator.generateVerificationRoc3(any(), any(), any(), any()) } returns mockDbCommands
        every { mockSessionGenerator.generateConfirmationRoc3(any(), any(), any(), any()) } returns mockDbCommands
        every { mockSessionGenerator.generateEnrolLastBioRoc3(any(), any(), any(), any()) } returns mockDbCommands
        every { mockSessionGenerator.clearCache() } returns Unit

        // WHEN: Execute the use case and collect all emitted values from the flow
        val emissions = useCase(
            projectId = projectId,
            moduleId = moduleId,
            attendantId = attendantId,
            enrolCount = enrolCount,
            identifyCount = identifyCount,
            verifyCount = verifyCount,
            confirmIdentifyCount = confirmIdentifyCount,
            enrolLastCount = enrolLastCount,
        ).toList()

        // THEN

        assertThat(emissions)
            .containsExactly(
                "$enrolCount Enrollment sessions inserted successfully",
                "$identifyCount Identify sessions inserted successfully",
                "$verifyCount Verify sessions inserted successfully",
                "$confirmIdentifyCount Confirm Identify sessions inserted successfully",
                "$enrolLastCount Enrol Last sessions inserted successfully",
                "Generated a total of ${finalEventCount - initialEventCount} new events",
            ).inOrder()

        coVerify(exactly = enrolCount) { mockSessionGenerator.generateEnrolmentIso(any(), any(), any(), any()) }
        coVerify(exactly = identifyCount) { mockSessionGenerator.generateIdentificationRoc3(any(), any(), any(), any()) }
        coVerify(exactly = verifyCount) { mockSessionGenerator.generateVerificationRoc3(any(), any(), any(), any()) }
        coVerify(exactly = confirmIdentifyCount) { mockSessionGenerator.generateConfirmationRoc3(any(), any(), any(), any()) }
        coVerify(exactly = enrolLastCount) { mockSessionGenerator.generateEnrolLastBioRoc3(any(), any(), any(), any()) }
        coVerify(exactly = totalNewSessions) { mockEventRepository.createEventScope(EventScopeType.SESSION) }
        coVerify(exactly = 1) { mockSessionGenerator.clearCache() }
    }
}
