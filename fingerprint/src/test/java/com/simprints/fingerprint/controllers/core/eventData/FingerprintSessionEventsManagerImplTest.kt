package com.simprints.fingerprint.controllers.core.eventData

import com.google.common.truth.Truth
import com.simprints.eventsystem.event.EventRepository
import com.simprints.fingerprint.controllers.core.eventData.model.FingerComparisonStrategy
import com.simprints.fingerprint.controllers.core.eventData.model.MatchEntry
import com.simprints.fingerprint.controllers.core.eventData.model.Matcher
import com.simprints.fingerprint.controllers.core.eventData.model.OneToOneMatchEvent
import com.simprints.id.data.db.subject.local.SubjectQuery
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import com.simprints.eventsystem.event.domain.models.FingerComparisonStrategy as CoreFingerComparisonStrategy
import com.simprints.eventsystem.event.domain.models.Matcher as CoreMatcher
import com.simprints.eventsystem.event.domain.models.OneToOneMatchEvent as CoreOneToOneMatchEvent

class FingerprintSessionEventsManagerImplTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()


    @Test
    fun addEventInBackground() = runBlocking {
        //Given
        val eventRepository: EventRepository = mockk()
        coEvery { eventRepository.addOrUpdateEvent(any()) } just Runs
        val eventSlot = CapturingSlot<CoreOneToOneMatchEvent>()
        val fingerprintSessionEventsManager = FingerprintSessionEventsManagerImpl(
            eventRepository,
             testCoroutineRule.testCoroutineDispatcher
        )
        val event = OneToOneMatchEvent(
            1L,
            1L,
            mockk<SubjectQuery>(relaxed = true),
            Matcher.SIM_AFIS,
            MatchEntry("candidateId", 1F),
            FingerComparisonStrategy.SAME_FINGER
        )
        // When
        fingerprintSessionEventsManager.addEventInBackground(event)

        // Then
        coVerify { eventRepository.addOrUpdateEvent(capture(eventSlot)) }
        with(eventSlot.captured.payload) {
            Truth.assertThat(fingerComparisonStrategy)
                .isEqualTo(CoreFingerComparisonStrategy.SAME_FINGER)
            Truth.assertThat(this.matcher)
                .isEqualTo(CoreMatcher.SIM_AFIS)
        }
    }
}
