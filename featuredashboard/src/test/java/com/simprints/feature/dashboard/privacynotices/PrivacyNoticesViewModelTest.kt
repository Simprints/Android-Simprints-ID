package com.simprints.feature.dashboard.privacynotices

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.google.common.truth.Truth.assertThat
import com.simprints.feature.dashboard.main.sync.DeviceManager
import com.simprints.feature.dashboard.privacynotices.PrivacyNoticeState.NotConnectedToInternet
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.DeviceConfiguration
import com.simprints.infra.config.domain.models.PrivacyNoticeResult.*
import com.simprints.feature.dashboard.privacynotices.PrivacyNoticeState.*
import com.simprints.infra.login.LoginManager
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.getOrAwaitValue
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test

class PrivacyNoticesViewModelTest {

    companion object {
        private const val PROJECT_ID = "projectId"
        private const val LANGUAGE = "fr"
        private const val PRIVACY_NOTICE = "privacy notice"
    }

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val configManager = mockk<ConfigManager> {
        coEvery { getDeviceConfiguration() } returns DeviceConfiguration(
            LANGUAGE,
            listOf(),
            ""
        )
    }
    private val loginManager = mockk<LoginManager> {
        every { getSignedInProjectIdOrEmpty() } returns PROJECT_ID
    }
    private val deviceManager = mockk<DeviceManager>()

    @Test
    fun `should return a NotConnectedToInternet if the device is not connected to the internet and the privacy notice is not cached`() {
        coEvery { configManager.getPrivacyNotice(PROJECT_ID, LANGUAGE) } returns flowOf(
            Failed(
                LANGUAGE,
                Exception()
            )
        )
        every { deviceManager.isConnectedLiveData } returns MutableLiveData(false)

        val viewModel = initViewModel()
        viewModel.fetchPrivacyNotice()

        assertThat(viewModel.privacyNoticeState.getOrAwaitValue()).isEqualTo(
            NotConnectedToInternet(
                LANGUAGE
            )
        )
    }

    @Test
    fun `should return the privacy notice if the device is not connected to the internet and the privacy notice is cached`() {
        coEvery { configManager.getPrivacyNotice(PROJECT_ID, LANGUAGE) } returns flowOf(
            Succeed(LANGUAGE, PRIVACY_NOTICE)
        )
        every { deviceManager.isConnectedLiveData } returns MutableLiveData(false)

        val viewModel = initViewModel()
        viewModel.fetchPrivacyNotice()

        assertThat(viewModel.privacyNoticeState.getOrAwaitValue()).isEqualTo(
            Available(LANGUAGE, PRIVACY_NOTICE)
        )
    }

    @Test
    fun `should return a NotAvailable if the device fails to retrieve the privacy notice and is connected to the internet`() {
        coEvery { configManager.getPrivacyNotice(PROJECT_ID, LANGUAGE) } returns flowOf(
            Failed(LANGUAGE, Exception())
        )
        every { deviceManager.isConnectedLiveData } returns MutableLiveData(true)

        val viewModel = initViewModel()
        viewModel.fetchPrivacyNotice()

        assertThat(viewModel.privacyNoticeState.getOrAwaitValue()).isEqualTo(
            NotAvailable(LANGUAGE)
        )
    }

    @Test
    fun `should return a NotAvailableBecauseBackendMaintenance if the device fails to retrieve the privacy notice because of a maintenance`() {
        coEvery { configManager.getPrivacyNotice(PROJECT_ID, LANGUAGE) } returns flowOf(
            FailedBecauseBackendMaintenance(LANGUAGE, Exception(), 10)
        )
        every { deviceManager.isConnectedLiveData } returns MutableLiveData(true)

        val viewModel = initViewModel()
        viewModel.fetchPrivacyNotice()

        assertThat(viewModel.privacyNoticeState.getOrAwaitValue()).isEqualTo(
            NotAvailableBecauseBackendMaintenance(LANGUAGE, 10)
        )
    }

    @Test
    fun `should return a DownloadInProgress if the device is downloading the privacy notice`() {
        coEvery { configManager.getPrivacyNotice(PROJECT_ID, LANGUAGE) } returns flowOf(
            InProgress(LANGUAGE)
        )
        every { deviceManager.isConnectedLiveData } returns MutableLiveData(true)

        val viewModel = initViewModel()
        viewModel.fetchPrivacyNotice()

        assertThat(viewModel.privacyNoticeState.getOrAwaitValue()).isEqualTo(
            DownloadInProgress(LANGUAGE)
        )
    }

    private fun initViewModel(): PrivacyNoticesViewModel =
        PrivacyNoticesViewModel(
            configManager,
            loginManager,
            deviceManager,
        )
}
