package com.simprints.feature.setup.location

import com.simprints.infra.events.sampledata.createSessionScope
import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class StoreUserLocationIntoCurrentSessionWorkerTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var locationManager: LocationManager

    @MockK
    private lateinit var eventRepository: SessionEventRepository

    private lateinit var worker: StoreUserLocationIntoCurrentSessionWorker

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        coEvery { eventRepository.getCurrentSessionScope() } returns createSessionScope()

        worker = StoreUserLocationIntoCurrentSessionWorker(
            mockk(relaxed = true),
            mockk(relaxed = true),
            eventRepository,
            locationManager,
            testCoroutineRule.testCoroutineDispatcher,
        )
    }

    @Test
    fun storeUserLocationIntoCurrentSession() = runTest {
        every { locationManager.requestLocation(any()) } returns flowOf(TestLocationData.buildFakeLocation())
        worker.doWork()
        coVerify(exactly = 1) { eventRepository.getCurrentSessionScope() }
        coVerify(exactly = 1) { eventRepository.saveSessionScope(any()) }
    }

    @Test
    fun `storeUserLocationIntoCurrentSession requestLocation throw exception`() = runTest {
        every { locationManager.requestLocation(any()) } throws Exception("Location collect exception")
        worker.doWork()
        coVerify(exactly = 0) { eventRepository.getCurrentSessionScope() }
    }

    @Test(expected = Test.None::class)
    fun `storeUserLocationIntoCurrentSession can't save event should not crash the app`() = runTest {
        every { locationManager.requestLocation(any()) } returns flowOf(TestLocationData.buildFakeLocation())
        coEvery {
            eventRepository.getCurrentSessionScope()
        } throws Exception("No session capture event found")
        worker.doWork()
        coVerify(exactly = 0) { eventRepository.saveSessionScope(any()) }
    }

    @Test
    fun `storeUserLocationIntoCurrentSession can't save events if the worker is canceled`() = runTest {
        every { locationManager.requestLocation(any()) } returns flowOf(TestLocationData.buildFakeLocation())
        worker.stop(0)
        worker.doWork()
        coVerify(exactly = 0) { eventRepository.saveSessionScope(any()) }
    }
}
