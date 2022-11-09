package com.simprints.id.activities.coreexitform

import com.simprints.eventsystem.event.EventRepository
import com.simprints.eventsystem.event.domain.models.RefusalEvent
import com.simprints.id.data.exitform.CoreExitFormReason
import com.simprints.id.data.exitform.toRefusalEventAnswer
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CoreExitFormViewModelTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var eventRepository: EventRepository

    private lateinit var viewModel: CoreExitFormViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        viewModel = CoreExitFormViewModel(
            eventRepository,
            CoroutineScope(testCoroutineRule.testCoroutineDispatcher)
        )
    }

    @Test
    fun `addExitFormEvent adds event to the repository`() = runTest {

        val eventSlot = slot<RefusalEvent>()
        coEvery { eventRepository.addOrUpdateEvent(capture(eventSlot)) } returns Unit

        val startTime = 100L
        val endTime = 101L
        val otherText = "SomeText"
        val reason = CoreExitFormReason.OTHER

        viewModel.addExitFormEvent(
            startTime,
            endTime,
            otherText,
            reason
        )

        coVerify { eventRepository.addOrUpdateEvent(any()) }

        assert(eventSlot.captured.payload.createdAt == startTime)
        assert(eventSlot.captured.payload.endedAt == endTime)
        assert(eventSlot.captured.payload.otherText == otherText)
        assert(eventSlot.captured.payload.reason == reason.toRefusalEventAnswer())
    }
}
