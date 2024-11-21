package com.simprints.feature.dashboard.settings.troubleshooting.overview

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.jraska.livedata.test
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.feature.dashboard.settings.troubleshooting.overview.usecase.CollectIdsUseCase
import com.simprints.feature.dashboard.settings.troubleshooting.overview.usecase.CollectLicenceStatesUseCase
import com.simprints.feature.dashboard.settings.troubleshooting.overview.usecase.CollectNetworkInformationUseCase
import com.simprints.infra.authstore.AuthStore
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
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
    private lateinit var collectLicencesUseCase: CollectLicenceStatesUseCase

    @MockK
    private lateinit var collectNetworkInformationUseCase: CollectNetworkInformationUseCase


    private lateinit var viewModel: OverviewViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        viewModel = OverviewViewModel(
            collectIds = collectIdsUseCase,
            collectLicenseStates = collectLicencesUseCase,
            collectNetworkInformation = collectNetworkInformationUseCase
        )
    }

    @Test
    fun `sets when data collected`() = runTest {
        every { collectIdsUseCase() } returns "ids"
        coEvery { collectLicencesUseCase() } returns "licences"
        every { collectNetworkInformationUseCase() } returns "network"

        val idsText = viewModel.projectIds.test()
        val licenceText = viewModel.licenseStates.test()
        val networkText = viewModel.networkStates.test()

        viewModel.collectData()

        assertThat(idsText.value()).isNotEmpty()
        assertThat(licenceText.value()).isNotEmpty()
        assertThat(networkText.value()).isNotEmpty()
    }
}
