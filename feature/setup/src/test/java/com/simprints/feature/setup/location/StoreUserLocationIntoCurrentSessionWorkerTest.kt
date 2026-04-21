package com.simprints.feature.setup.location

import android.os.PowerManager
import com.simprints.infra.events.event.domain.models.scope.Location
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
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
    private lateinit var updateSessionScopeLocationUseCase: UpdateSessionScopeLocationUseCase

    private lateinit var worker: StoreUserLocationIntoCurrentSessionWorker

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        worker = StoreUserLocationIntoCurrentSessionWorker(
            mockk(relaxed = true) {
                every { getSystemService<PowerManager>(any()) } returns mockk {
                    every { isIgnoringBatteryOptimizations(any()) } returns true
                }
            },
            mockk(relaxed = true),
            updateSessionScopeLocationUseCase,
            locationManager,
            testCoroutineRule.testCoroutineDispatcher,
        )
    }

    @Test
    fun storeUserLocationIntoCurrentSession() = runTest {
        every { locationManager.requestLocation() } returns flowOf(Location(latitude = 23.0, longitude = 54.0))
        worker.doWork()
        coVerify(exactly = 1) { updateSessionScopeLocationUseCase.invoke(any()) }
    }

    @Test
    fun `storeUserLocationIntoCurrentSession requestLocation throw exception`() = runTest {
        every { locationManager.requestLocation() } throws Exception("Location collect exception")
        worker.doWork()
        coVerify(exactly = 0) { updateSessionScopeLocationUseCase.invoke(any()) }
    }

    @Test(expected = Test.None::class)
    fun `storeUserLocationIntoCurrentSession can't save event should not crash the app`() = runTest {
        every { locationManager.requestLocation() } returns flowOf(Location(latitude = 23.0, longitude = 54.0))
        coEvery {
            updateSessionScopeLocationUseCase.invoke(any())
        } throws Exception("No session capture event found")
        worker.doWork()
    }

    @Test
    fun `storeUserLocationIntoCurrentSession can't save events if the worker is canceled`() = runTest {
        every { locationManager.requestLocation() } returns flowOf(Location(latitude = 23.0, longitude = 54.0))
        worker.stop(0)
        worker.doWork()
        coVerify(exactly = 0) { updateSessionScopeLocationUseCase.invoke(any()) }
    }
}
