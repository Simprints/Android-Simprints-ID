package com.simprints.id.activities.faceexitform

import com.google.common.truth.Truth.assertThat
import com.simprints.eventsystem.event.EventRepository
import com.simprints.eventsystem.event.domain.models.EventType
import com.simprints.id.data.exitform.FaceExitFormReason
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.Test

class FaceExitFormViewModelTest {

    private val eventRepository = mockk<EventRepository>(relaxed = true)
    private val faceExitFormViewModel = FaceExitFormViewModel(eventRepository)

    @Test
    fun `addEvent should call the correct method`() {
        faceExitFormViewModel.addExitFormEvent(2, 4, "test", FaceExitFormReason.OTHER)

        coVerify(exactly = 1) {
            eventRepository.addOrUpdateEvent(match {
                assertThat(it.type).isEqualTo(EventType.REFUSAL)
                assertThat(it.payload.createdAt).isEqualTo(2)
                assertThat(it.payload.endedAt).isEqualTo(4)
                assertThat(it.payload.type).isEqualTo(EventType.REFUSAL)
                true
            })
        }
        coVerify(exactly = 1) { eventRepository.removeLocationDataFromCurrentSession() }
    }
}
