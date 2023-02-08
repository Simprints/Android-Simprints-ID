package com.simprints.id.services.location

import com.simprints.eventsystem.event.EventRepository
import com.simprints.eventsystem.event.domain.models.session.SessionCaptureEvent
import com.simprints.eventsystem.sampledata.createSessionCaptureEvent
import com.simprints.id.testtools.TestData
import com.simprints.id.tools.LocationManager
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class StoreUserLocationIntoCurrentSessionWorkerTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val locationManager = mockk<LocationManager>()
    private val eventRepository = mockk<EventRepository> {
        coEvery { getCurrentCaptureSessionEvent() } returns createSessionCaptureEvent()
    }
    private val worker = StoreUserLocationIntoCurrentSessionWorker(
        mockk(relaxed = true),
        mockk(relaxed = true),
        eventRepository,
        locationManager,
        testCoroutineRule.testCoroutineDispatcher,
    )


    @Test
    fun storeUserLocationIntoCurrentSession() = runTest {
        every { locationManager.requestLocation(any()) } returns flowOf(TestData.buildFakeLocation())
        worker.doWork()
        coVerify(exactly = 1) { eventRepository.getCurrentCaptureSessionEvent() }
        coVerify(exactly = 1) { eventRepository.addOrUpdateEvent(any<SessionCaptureEvent>()) }
    }

    @Test
    fun `storeUserLocationIntoCurrentSession requestLocation throw exception`() = runTest {
        every { locationManager.requestLocation(any()) } throws Exception("Location collect exception")
        worker.doWork()
        coVerify(exactly = 0) { eventRepository.getCurrentCaptureSessionEvent() }
        coVerify(exactly = 0) { eventRepository.addOrUpdateEvent(any<SessionCaptureEvent>()) }
    }

    @Test(expected = Test.None::class)
    fun `storeUserLocationIntoCurrentSession can't save event should not crash the app`() =
        runTest {
            every { locationManager.requestLocation(any()) } returns flowOf(TestData.buildFakeLocation())
            coEvery {
                eventRepository.getCurrentCaptureSessionEvent()
            } throws Exception("No session capture event found")
            worker.doWork()
            coVerify(exactly = 0) { eventRepository.addOrUpdateEvent(any<SessionCaptureEvent>()) }
        }

    @Test
    fun `storeUserLocationIntoCurrentSession can't save events if the worker is canceled`() =
        runTest {
            every { locationManager.requestLocation(any()) } returns flowOf(TestData.buildFakeLocation())
            worker.stop()
            worker.doWork()
            coVerify(exactly = 0) { eventRepository.addOrUpdateEvent(any<SessionCaptureEvent>()) }
        }

}
