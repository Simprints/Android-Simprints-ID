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
    fun `should request location permission if collectLocation is enabled`() = runTest{
        // Given
        coEvery { configManager.getProjectConfiguration().general.collectLocation } returns true

        // When
        viewModel.start()

        // Then
        viewModel.requestLocationPermission.test().assertHasValue()

    }

    @Test
    fun `should finish if collectLocation is disabled`() =runTest {
        // Given
        coEvery { configManager.getProjectConfiguration().general.collectLocation } returns false

        // when
        viewModel.start()

        // Then
        viewModel.finish.test().assertHasValue()

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
}
