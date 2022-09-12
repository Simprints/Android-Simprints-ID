package com.simprints.face.configuration

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.infralicense.repository.LicenseRepository
import com.simprints.infralicense.repository.LicenseState
import com.simprints.infralicense.repository.LicenseVendor
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.coroutines.TestDispatcherProvider
import com.simprints.testtools.common.livedata.getOrAwaitValue
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class ConfigurationViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val licenseRepository: LicenseRepository = mockk(relaxed = true)
    private val dispatcherProvider = TestDispatcherProvider(testCoroutineRule)
    private lateinit var viewModel: ConfigurationViewModel

    @Before
    fun setUp() {
        viewModel = ConfigurationViewModel(licenseRepository, dispatcherProvider)
        mockLicenseResult(
            "backendError", flowOf(
                LicenseState.Downloading,
                LicenseState.FinishedWithBackendMaintenanceError(null)
            )
        )
        mockLicenseResult(
            "timedBackendError", flowOf(
                LicenseState.Downloading,
                LicenseState.FinishedWithBackendMaintenanceError(600L)
            )
        )
        mockLicenseResult(
            "success", flowOf(
                LicenseState.Downloading,
                LicenseState.FinishedWithSuccess("some license here")
            )
        )
    }

    @Test
    fun gettingSuccess_shouldMapCorrectlyToConfigState() {
        viewModel.retrieveLicense("success", "")

        val state = viewModel.configurationState.getOrAwaitValue()

        assertThat(state.peekContent()).isInstanceOf(ConfigurationState.FinishedWithSuccess::class.java)
        assertThat((state.peekContent() as ConfigurationState.FinishedWithSuccess).license).isEqualTo("some license here")
    }

    @Test
    fun gettingBackendMaintenanceError_shouldMapCorrectlyToConfigState() {
        viewModel.retrieveLicense("backendError", "")

        val state = viewModel.configurationState.getOrAwaitValue()

        assertThat(state.peekContent()).isInstanceOf(ConfigurationState.FinishedWithBackendMaintenanceError::class.java)
    }

    @Test
    fun gettingTimedBackendMaintenanceError_shouldMapCorrectlyToConfigState() {
        viewModel.retrieveLicense("timedBackendError", "")

        val state = viewModel.configurationState.getOrAwaitValue()

        assertThat(state.peekContent()).isInstanceOf(ConfigurationState.FinishedWithBackendMaintenanceError::class.java)
        assertThat((state.peekContent() as ConfigurationState.FinishedWithBackendMaintenanceError).estimatedOutage).isEqualTo(600L)
    }

    private fun mockLicenseResult(projectId: String, data: Flow<LicenseState>) {
        coEvery {
            licenseRepository.getLicenseStates(
                projectId,
                "",
                LicenseVendor.RANK_ONE_FACE
            )
        } returns data
    }
}
