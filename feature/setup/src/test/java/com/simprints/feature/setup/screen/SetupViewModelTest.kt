package com.simprints.feature.setup.screen

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.jraska.livedata.test
import com.simprints.feature.setup.LocationStore
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.coEvery
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test


class SetupViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val locationStore = mockk<LocationStore>()
    private val configManager = mockk<ConfigManager>()
    private val viewModel = SetupViewModel(locationStore, configManager)

    @Test
    fun `should request location permission if collectLocation is enabled`() = runTest {
        // Given
        coEvery { configManager.getProjectConfiguration().general.collectLocation } returns true

        // When
        viewModel.start()

        // Then
        viewModel.requestLocationPermission.test().assertHasValue()

    }

    @Test
    fun `should not request location permission if collectLocation is disabled`() = runTest {
        // Given
        coEvery { configManager.getProjectConfiguration().general.collectLocation } returns false

        // when
        viewModel.start()

        // Then
        viewModel.requestLocationPermission.test().assertNoValue()

    }


    @Test
    fun `should call locationStore collectLocationInBackground if collectLocation is called`()=runTest {
        // Given
        justRun { locationStore.collectLocationInBackground()  }

        // when
        viewModel.collectLocation()

        // Then
        verify { locationStore.collectLocationInBackground() }

    }

    @Test
    fun `should request notification permission if collectLocation is disabled`() = runTest {
        // Given
        coEvery { configManager.getProjectConfiguration().general.collectLocation } returns false

        // When
        viewModel.start()

        // Then
        viewModel.requestNotificationPermission.test().assertHasValue()

    }

    @Test
    fun `should not request notification permission yet if collectLocation is enabled`() = runTest {
        // Given
        coEvery { configManager.getProjectConfiguration().general.collectLocation } returns true

        // When
        viewModel.start()

        // Then
        viewModel.requestNotificationPermission.test().assertNoValue()

    }

    @Test
    fun `should request notification permission on command`() = runTest {
        // When
        viewModel.requestNotificationsPermission()

        // Then
        viewModel.requestNotificationPermission.test().assertHasValue()
    }
}
