package com.simprints.id.activities.fingerprintexitform

import com.simprints.eventsystem.event.EventRepository
import com.simprints.eventsystem.event.domain.models.RefusalEvent
import com.simprints.id.data.exitform.FingerprintExitFormReason
import com.simprints.id.data.exitform.toRefusalEventAnswer
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FingerprintExitFormViewModelTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var eventRepository: EventRepository

    private lateinit var viewModel: FingerprintExitFormViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        viewModel = FingerprintExitFormViewModel(
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
        val reason = FingerprintExitFormReason.OTHER

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
