package com.simprints.face.configuration.screen

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.face.configuration.data.FaceConfigurationState
import com.simprints.infra.license.LicenseRepository
import com.simprints.infra.license.LicenseState
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.getOrAwaitValue
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class FaceConfigurationViewModelTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @get:Rule
    val executorRule: TestRule = InstantTaskExecutorRule()

    @MockK
    private lateinit var licenseRepository: LicenseRepository

    private lateinit var viewModel: FaceConfigurationViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        viewModel = FaceConfigurationViewModel(licenseRepository)
    }

    @Test
    fun `Should correctly map successful licence retrieval`() = runTest {
        coEvery { licenseRepository.getLicenseStates(any(), any(), any()) } returns flowOf(
            LicenseState.Downloading,
            LicenseState.FinishedWithSuccess("some license here")
        )

        viewModel.retrieveLicense("", "")
        val state = viewModel.configurationState.getOrAwaitValue()

        assertThat(state.peekContent()).isInstanceOf(FaceConfigurationState.FinishedWithSuccess::class.java)
        assertThat((state.peekContent() as FaceConfigurationState.FinishedWithSuccess).license).isEqualTo(
            "some license here"
        )
    }

    @Test
    fun `Should correctly map backend maintenance error`() = runTest {
        coEvery { licenseRepository.getLicenseStates(any(), any(), any()) } returns flowOf(
            LicenseState.Downloading,
            LicenseState.FinishedWithBackendMaintenanceError(null)
        )
        viewModel.retrieveLicense("", "")
        val state = viewModel.configurationState.getOrAwaitValue()

        assertThat(state.peekContent()).isInstanceOf(FaceConfigurationState.FinishedWithBackendMaintenanceError::class.java)
    }

    @Test
    fun `Should correctly map timed maintenance error`() = runTest {
        coEvery { licenseRepository.getLicenseStates(any(), any(), any()) } returns flowOf(
            LicenseState.Downloading,
            LicenseState.FinishedWithBackendMaintenanceError(600L)
        )

        viewModel.retrieveLicense("", "")

        val state = viewModel.configurationState.getOrAwaitValue()

        assertThat(state.peekContent()).isInstanceOf(FaceConfigurationState.FinishedWithBackendMaintenanceError::class.java)
        assertThat((state.peekContent() as FaceConfigurationState.FinishedWithBackendMaintenanceError).estimatedOutage).isEqualTo(
            600L
        )
    }

    @Test
    fun `Should delete invalid licence`() = runTest {
        viewModel.deleteInvalidLicense()

        coVerify { licenseRepository.deleteCachedLicense() }
    }

}
