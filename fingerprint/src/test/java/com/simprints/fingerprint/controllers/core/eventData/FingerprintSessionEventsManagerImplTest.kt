package com.simprints.fingerprint.controllers.core.eventData

import com.google.common.truth.Truth
import com.simprints.fingerprint.activities.matching.MatchTask.Companion.MATCHER_NAME
import com.simprints.fingerprint.controllers.core.eventData.model.FingerComparisonStrategy
import com.simprints.fingerprint.controllers.core.eventData.model.MatchEntry
import com.simprints.fingerprint.controllers.core.eventData.model.OneToOneMatchEvent
import com.simprints.infra.enrolment.records.domain.models.SubjectQuery
import com.simprints.infra.events.EventRepository
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.CapturingSlot
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import com.simprints.infra.events.event.domain.models.FingerComparisonStrategy as CoreFingerComparisonStrategy
import com.simprints.infra.events.event.domain.models.OneToOneMatchEvent as CoreOneToOneMatchEvent

class FingerprintSessionEventsManagerImplTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()


    @Test
    fun addEventInBackground() = runBlocking {
        //Given
        val scope = CoroutineScope(testCoroutineRule.testCoroutineDispatcher)
        val eventRepository: EventRepository = mockk()
        coEvery { eventRepository.addOrUpdateEvent(any()) } just Runs
        val eventSlot = CapturingSlot<CoreOneToOneMatchEvent>()
        val fingerprintSessionEventsManager = FingerprintSessionEventsManagerImpl(
            eventRepository,
             scope
        )
        val event = OneToOneMatchEvent(
            1L,
            1L,
            mockk<SubjectQuery>(relaxed = true),
            MATCHER_NAME,
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
                .isEqualTo(MATCHER_NAME)
        }
    }
}
