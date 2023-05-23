package com.simprints.feature.consent.screens.privacy

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.DeviceConfiguration
import com.simprints.infra.config.domain.models.PrivacyNoticeResult
import com.simprints.infra.login.LoginManager
import com.simprints.infra.network.ConnectivityTracker
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.getOrAwaitValue
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test


internal class PrivacyNoticeViewModelTest {
    companion object {
        private const val PROJECT_ID = "projectId"
        private const val LANGUAGE = "en"
    }

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    lateinit var connectivityTracker: ConnectivityTracker

    @MockK
    lateinit var configManager: ConfigManager

    @MockK
    lateinit var loginManager: LoginManager


    private lateinit var privacyNoticeViewModel: PrivacyNoticeViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        coEvery { configManager.getDeviceConfiguration() } returns DeviceConfiguration(LANGUAGE, listOf(), "")
        every { loginManager.signedInProjectId } returns PROJECT_ID

        privacyNoticeViewModel = PrivacyNoticeViewModel(
            connectivityTracker,
            configManager,
            loginManager,
        )
    }

    @Test
    fun `retrievePrivacyNotice should return DownloadInProgress when trying download`() = runTest {
        coEvery { configManager.getPrivacyNotice(PROJECT_ID, LANGUAGE) } returns flowOf(
            PrivacyNoticeResult.InProgress(LANGUAGE),
        )

        val privacyNoticeLiveData = privacyNoticeViewModel.viewState()
        privacyNoticeViewModel.retrievePrivacyNotice()

        val value = privacyNoticeLiveData.getOrAwaitValue()
        Truth.assertThat(value).isInstanceOf(PrivacyNoticeState.DownloadInProgress::class.java)
    }

    @Test
    fun `retrievePrivacyNotice should return ContentAvailable when success received`() = runTest {
        coEvery { configManager.getPrivacyNotice(PROJECT_ID, LANGUAGE) } returns flowOf(
            PrivacyNoticeResult.InProgress(LANGUAGE),
            PrivacyNoticeResult.Succeed(LANGUAGE, "some long consent")
        )

        val privacyNoticeLiveData = privacyNoticeViewModel.viewState()
        privacyNoticeViewModel.retrievePrivacyNotice()

        val value = privacyNoticeLiveData.getOrAwaitValue()
        Truth.assertThat(value).isInstanceOf(PrivacyNoticeState.ConsentAvailable::class.java)
    }

    @Test
    fun `retrievePrivacyNotice should return ConsentNotAvailable when Failed received`() = runTest {
        coEvery { configManager.getPrivacyNotice(PROJECT_ID, LANGUAGE) } returns flowOf(
            PrivacyNoticeResult.InProgress(LANGUAGE),
            PrivacyNoticeResult.Failed(LANGUAGE, Throwable())
        )

        val privacyNoticeLiveData = privacyNoticeViewModel.viewState()
        privacyNoticeViewModel.retrievePrivacyNotice()

        val value = privacyNoticeLiveData.getOrAwaitValue()
        Truth.assertThat(value).isInstanceOf(PrivacyNoticeState.ConsentNotAvailable::class.java)
    }

    @Test
    fun `retrievePrivacyNotice should return BackendMaintenance when FailedBecauseBackendMaintenance received`() = runTest {
        coEvery { configManager.getPrivacyNotice(PROJECT_ID, LANGUAGE) } returns flowOf(
            PrivacyNoticeResult.InProgress(LANGUAGE),
            PrivacyNoticeResult.FailedBecauseBackendMaintenance(LANGUAGE, Throwable())
        )

        val privacyNoticeLiveData = privacyNoticeViewModel.viewState()
        privacyNoticeViewModel.retrievePrivacyNotice()

        val value = privacyNoticeLiveData.getOrAwaitValue()
        Truth.assertThat(value).isInstanceOf(PrivacyNoticeState.BackendMaintenance::class.java)
    }

    @Test
    fun `retrievePrivacyNotice should return BackendMaintenance with estimation when FailedBecauseBackendMaintenance received`() = runTest {
        coEvery { configManager.getPrivacyNotice(PROJECT_ID, LANGUAGE) } returns flowOf(
            PrivacyNoticeResult.InProgress(LANGUAGE),
            PrivacyNoticeResult.FailedBecauseBackendMaintenance(LANGUAGE, Throwable(), 1000L)
        )

        val privacyNoticeLiveData = privacyNoticeViewModel.viewState()
        privacyNoticeViewModel.retrievePrivacyNotice()

        val value = privacyNoticeLiveData.getOrAwaitValue()
        Truth.assertThat(value).isInstanceOf(PrivacyNoticeState.BackendMaintenance::class.java)
        Truth.assertThat((value as PrivacyNoticeState.BackendMaintenance).estimatedOutage).isNotEmpty()
    }

    @Test
    fun `downloadPressed should retrieve notice when online`() = runTest {
        every { connectivityTracker.isConnected() } returns true
        coEvery { configManager.getPrivacyNotice(PROJECT_ID, LANGUAGE) } returns flowOf(PrivacyNoticeResult.InProgress(LANGUAGE))

        val privacyNoticeLiveData = privacyNoticeViewModel.viewState()
        val showOfflineLiveData = privacyNoticeViewModel.showOffline()

        privacyNoticeViewModel.downloadPressed()

        Truth.assertThat(privacyNoticeLiveData.value).isNotNull()
        Truth.assertThat(showOfflineLiveData.value).isNull()
    }

    @Test
    fun `downloadPressed should return offline event when offline`() = runTest {
        every { connectivityTracker.isConnected() } returns false

        val privacyNoticeLiveData = privacyNoticeViewModel.viewState()
        val showOfflineLiveData = privacyNoticeViewModel.showOffline()

        privacyNoticeViewModel.downloadPressed()

        Truth.assertThat(privacyNoticeLiveData.value).isNull()
        Truth.assertThat(showOfflineLiveData.value).isNotNull()
    }
}
