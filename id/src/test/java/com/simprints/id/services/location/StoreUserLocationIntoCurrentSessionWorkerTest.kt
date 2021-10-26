package com.simprints.id.services.location

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.testing.TestListenableWorkerBuilder
import com.simprints.eventsystem.event.domain.models.session.SessionCaptureEvent
import com.simprints.eventsystem.sampledata.createSessionCaptureEvent
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.TestData
import com.simprints.id.tools.LocationManager
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

import org.junit.runner.RunWith
import org.robolectric.annotation.Config


@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
@ExperimentalCoroutinesApi
internal class StoreUserLocationIntoCurrentSessionWorkerTest {
    private val app = ApplicationProvider.getApplicationContext() as TestApplication
    private lateinit var worker: StoreUserLocationIntoCurrentSessionWorker

    @RelaxedMockK
    lateinit var mockEventRepository: com.simprints.eventsystem.event.EventRepository

    @RelaxedMockK
    lateinit var mockLocationManager: LocationManager

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(Dispatchers.Unconfined)
        worker = TestListenableWorkerBuilder<StoreUserLocationIntoCurrentSessionWorker>(app).build()
        app.component = mockk(relaxed = true)
        mockDependencies()
    }

    private fun mockDependencies() {
        coEvery { mockEventRepository.getCurrentCaptureSessionEvent() } returns createSessionCaptureEvent()
        with(worker) {
            locationManager = mockLocationManager
            eventRepository = mockEventRepository
        }
    }

    @Test
    fun storeUserLocationIntoCurrentSession() = runBlocking {
        every { mockLocationManager.requestLocation(any()) } returns flowOf(TestData.buildFakeLocation())
        worker.doWork()
        coVerify(exactly = 1) { mockEventRepository.getCurrentCaptureSessionEvent() }
        coVerify(exactly = 1) { mockEventRepository.addOrUpdateEvent(any<SessionCaptureEvent>()) }
    }

    @Test
    fun `storeUserLocationIntoCurrentSession requestLocation throw exception`() = runBlocking {
        every { mockLocationManager.requestLocation(any()) } throws Exception("Location collect exception")
        worker.doWork()
        coVerify(exactly = 0) { mockEventRepository.getCurrentCaptureSessionEvent() }
        coVerify(exactly = 0) { mockEventRepository.addOrUpdateEvent(any<SessionCaptureEvent>()) }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}
