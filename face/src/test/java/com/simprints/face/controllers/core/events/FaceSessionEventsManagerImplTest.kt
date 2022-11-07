package com.simprints.face.controllers.core.events

import com.simprints.eventsystem.event.EventRepository
import com.simprints.face.controllers.core.events.model.EventType
import com.simprints.face.controllers.core.events.model.FaceOnboardingCompleteEvent as FaceEvent
import com.simprints.eventsystem.event.domain.models.face.FaceOnboardingCompleteEvent as CoreEvent
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FaceSessionEventsManagerImplTest {

    @MockK
    private lateinit var eventRepository: EventRepository

    @MockK
    private lateinit var faceEvent: FaceEvent

    @MockK
    private lateinit var coreEvent: CoreEvent

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private lateinit var faceSessionEventsManagerImpl: FaceSessionEventsManagerImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        setUpMocks()

        val scope = CoroutineScope(testCoroutineRule.testCoroutineDispatcher)
        faceSessionEventsManagerImpl = FaceSessionEventsManagerImpl(
            eventRepository,
            scope
        )
    }

    private fun setUpMocks() {
        every { faceEvent.type } returns EventType.FACE_ONBOARDING_COMPLETE
        every { faceEvent.fromDomainToCore() } returns coreEvent
    }

    @Test
    fun `calling AddEventInBackground adds event in repository`() = runTest {
        faceSessionEventsManagerImpl.addEventInBackground(faceEvent)

        coVerify { eventRepository.addOrUpdateEvent(coreEvent) }
    }

    @Test
    fun `calling AddEvent adds event in repository`() = runTest {
        faceSessionEventsManagerImpl.addEvent(faceEvent)

        coVerify { eventRepository.addOrUpdateEvent(coreEvent) }
    }
}
