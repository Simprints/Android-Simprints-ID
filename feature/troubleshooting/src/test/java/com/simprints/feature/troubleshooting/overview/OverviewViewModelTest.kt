package com.simprints.feature.troubleshooting.overview

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.jraska.livedata.test
import com.simprints.feature.troubleshooting.overview.usecase.CollectConfigurationDetailsUseCase
import com.simprints.feature.troubleshooting.overview.usecase.CollectIdsUseCase
import com.simprints.feature.troubleshooting.overview.usecase.CollectLicenceStatesUseCase
import com.simprints.feature.troubleshooting.overview.usecase.CollectNetworkInformationUseCase
import com.simprints.feature.troubleshooting.overview.usecase.CollectScannerStateUseCase
import com.simprints.feature.troubleshooting.overview.usecase.PingServerUseCase
import com.simprints.feature.troubleshooting.overview.usecase.PingServerUseCase.PingResult
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class OverviewViewModelTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var collectIdsUseCase: CollectIdsUseCase

    @MockK
    private lateinit var collectConfigurationDetails: CollectConfigurationDetailsUseCase

    @MockK
    private lateinit var collectLicencesUseCase: CollectLicenceStatesUseCase

    @MockK
    private lateinit var collectNetworkInformationUseCase: CollectNetworkInformationUseCase

    @MockK
    private lateinit var collectScannerState: CollectScannerStateUseCase

    @MockK
    private lateinit var pingServerUseCase: PingServerUseCase

    private lateinit var viewModel: OverviewViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        viewModel = OverviewViewModel(
            collectIds = collectIdsUseCase,
            collectConfigurationDetails = collectConfigurationDetails,
            collectLicenseStates = collectLicencesUseCase,
            collectNetworkInformation = collectNetworkInformationUseCase,
            doServerPing = pingServerUseCase,
            collectScannerState = collectScannerState,
        )
    }

    @Test
    fun `sets when data collected`() = runTest {
        every { collectIdsUseCase() } returns "ids"
        coEvery { collectConfigurationDetails() } returns "details"
        coEvery { collectLicencesUseCase() } returns "licences"
        every { collectNetworkInformationUseCase() } returns "network"

        val idsText = viewModel.projectIds.test()
        val configText = viewModel.configurationDetails.test()
        val licenceText = viewModel.licenseStates.test()
        val networkText = viewModel.networkStates.test()
        val pingResult = viewModel.pingResult.test()

        viewModel.collectData()

        assertThat(idsText.value()).isNotEmpty()
        assertThat(configText.value()).isNotEmpty()
        assertThat(licenceText.value()).isNotEmpty()
        assertThat(networkText.value()).isNotEmpty()
        assertThat(pingResult.value()).isInstanceOf(PingResult.NotDone::class.java)
    }

    @Test
    fun `propagates server ping result`() = runTest {
        val pingResult = viewModel.pingResult.test()

        every { pingServerUseCase.invoke() } returns flowOf(
            PingResult.InProgress,
            PingResult.Success("message"),
        )

        viewModel.pingServer()

        assertThat(pingResult.valueHistory()).containsExactly(
            PingResult.NotDone,
            PingResult.InProgress,
            PingResult.Success("message"),
        )
    }
}
