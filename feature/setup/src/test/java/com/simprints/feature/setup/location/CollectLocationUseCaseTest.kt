package com.simprints.feature.setup.location

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.events.event.domain.models.scope.Location
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class CollectLocationUseCaseTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var locationManager: LocationManager

    @MockK
    private lateinit var updateSessionScopeLocationUseCase: UpdateSessionScopeLocationUseCase

    private lateinit var collectLocation: CollectLocationUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        collectLocation = CollectLocationUseCase(
            locationManager = locationManager,
            updateSessionScopeLocationUseCase = updateSessionScopeLocationUseCase,
        )
    }

    @Test
    fun `invoke saves location into current session`() = runTest {
        every { locationManager.requestLocation() } returns flowOf(Location(latitude = 23.0, longitude = 54.0))

        collectLocation()

        coVerify(exactly = 1) { updateSessionScopeLocationUseCase.invoke(any()) }
    }

    @Test
    fun `invoke requestLocation throws exception does not crash`() = runTest {
        every { locationManager.requestLocation() } throws Exception("Location collect exception")

        collectLocation()

        coVerify(exactly = 0) { updateSessionScopeLocationUseCase.invoke(any()) }
    }

    @Test(expected = Test.None::class)
    fun `invoke can't save event should not crash the app`() = runTest {
        every { locationManager.requestLocation() } returns flowOf(Location(latitude = 23.0, longitude = 54.0))
        coEvery {
            updateSessionScopeLocationUseCase.invoke(any())
        } throws Exception("No session capture event found")

        collectLocation()
    }

    @Test
    fun `invoke rethrows cancellation exception thrown while saving location`() = runTest {
        every { locationManager.requestLocation() } returns flowOf(Location(latitude = 23.0, longitude = 54.0))
        coEvery {
            updateSessionScopeLocationUseCase.invoke(any())
        } throws CancellationException("Cancelled")

        var thrown: Throwable? = null
        try {
            collectLocation()
        } catch (c: CancellationException) {
            thrown = c
        }

        assertThat(thrown).isInstanceOf(CancellationException::class.java)
    }
}
