package com.simprints.feature.consent.screens.privacy

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.models.DeviceConfiguration
import com.simprints.infra.config.store.models.PrivacyNoticeResult
import com.simprints.infra.config.sync.ConfigManager
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
        private const val DEVICE_LANGUAGE = "en"
        private const val DEFAULT_LANGUAGE = "fr"
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
    lateinit var authStore: AuthStore

    private lateinit var privacyNoticeViewModel: PrivacyNoticeViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        coEvery { configManager.getDeviceConfiguration() } returns DeviceConfiguration(DEVICE_LANGUAGE, listOf(), "")
        coEvery { configManager.getProjectConfiguration().general.defaultLanguage } returns DEFAULT_LANGUAGE
        every { authStore.signedInProjectId } returns PROJECT_ID

        privacyNoticeViewModel = PrivacyNoticeViewModel(
            connectivityTracker,
            configManager,
            authStore,
        )
    }

    @Test
    fun `retrievePrivacyNotice should return DownloadInProgress when trying download`() = runTest {
        coEvery { configManager.getPrivacyNotice(PROJECT_ID, DEVICE_LANGUAGE) } returns flowOf(
            PrivacyNoticeResult.InProgress(DEVICE_LANGUAGE),
        )

        val privacyNoticeLiveData = privacyNoticeViewModel.viewState
        privacyNoticeViewModel.retrievePrivacyNotice()

        val value = privacyNoticeLiveData.getOrAwaitValue()
        Truth.assertThat(value).isInstanceOf(PrivacyNoticeState.DownloadInProgress::class.java)
    }

    @Test
    fun `retrievePrivacyNotice should return ContentAvailable when success received`() = runTest {
        coEvery { configManager.getPrivacyNotice(PROJECT_ID, DEVICE_LANGUAGE) } returns flowOf(
            PrivacyNoticeResult.InProgress(DEVICE_LANGUAGE),
            PrivacyNoticeResult.Succeed(DEVICE_LANGUAGE, "some long consent"),
        )

        val privacyNoticeLiveData = privacyNoticeViewModel.viewState
        privacyNoticeViewModel.retrievePrivacyNotice()

        val value = privacyNoticeLiveData.getOrAwaitValue()
        Truth.assertThat(value).isInstanceOf(PrivacyNoticeState.ConsentAvailable::class.java)
    }

    @Test
    fun `retrievePrivacyNotice should attempt default language when Failed received with initial`() = runTest {
        coEvery { configManager.getPrivacyNotice(PROJECT_ID, DEVICE_LANGUAGE) } returns flowOf(
            PrivacyNoticeResult.InProgress(DEVICE_LANGUAGE),
            PrivacyNoticeResult.Failed(DEVICE_LANGUAGE, Throwable()),
        )
        coEvery { configManager.getPrivacyNotice(PROJECT_ID, DEFAULT_LANGUAGE) } returns flowOf(
            PrivacyNoticeResult.InProgress(DEFAULT_LANGUAGE),
            PrivacyNoticeResult.Succeed(DEFAULT_LANGUAGE, "some long consent"),
        )

        val privacyNoticeLiveData = privacyNoticeViewModel.viewState
        privacyNoticeViewModel.retrievePrivacyNotice()

        val value = privacyNoticeLiveData.getOrAwaitValue()
        Truth.assertThat(value).isInstanceOf(PrivacyNoticeState.ConsentAvailable::class.java)
    }

    @Test
    fun `retrievePrivacyNotice should return BackendMaintenance when FailedBecauseBackendMaintenance received`() = runTest {
        coEvery { configManager.getPrivacyNotice(PROJECT_ID, DEVICE_LANGUAGE) } returns flowOf(
            PrivacyNoticeResult.InProgress(DEVICE_LANGUAGE),
            PrivacyNoticeResult.FailedBecauseBackendMaintenance(DEVICE_LANGUAGE, Throwable()),
        )

        val privacyNoticeLiveData = privacyNoticeViewModel.viewState
        privacyNoticeViewModel.retrievePrivacyNotice()

        val value = privacyNoticeLiveData.getOrAwaitValue()
        Truth.assertThat(value).isInstanceOf(PrivacyNoticeState.BackendMaintenance::class.java)
    }

    @Test
    fun `retrievePrivacyNotice should return BackendMaintenance when FailedBecauseBackendMaintenance receivedon default language`() =
        runTest {
            coEvery { configManager.getPrivacyNotice(PROJECT_ID, DEVICE_LANGUAGE) } returns flowOf(
                PrivacyNoticeResult.InProgress(DEVICE_LANGUAGE),
                PrivacyNoticeResult.Failed(DEVICE_LANGUAGE, Throwable()),
            )
            coEvery { configManager.getPrivacyNotice(PROJECT_ID, DEFAULT_LANGUAGE) } returns flowOf(
                PrivacyNoticeResult.InProgress(DEFAULT_LANGUAGE),
                PrivacyNoticeResult.FailedBecauseBackendMaintenance(DEVICE_LANGUAGE, Throwable()),
            )

            val privacyNoticeLiveData = privacyNoticeViewModel.viewState
            privacyNoticeViewModel.retrievePrivacyNotice()

            val value = privacyNoticeLiveData.getOrAwaitValue()
            Truth.assertThat(value).isInstanceOf(PrivacyNoticeState.BackendMaintenance::class.java)
        }

    @Test
    fun `retrievePrivacyNotice should return BackendMaintenance with estimation when FailedBecauseBackendMaintenance received`() = runTest {
        coEvery { configManager.getPrivacyNotice(PROJECT_ID, DEVICE_LANGUAGE) } returns flowOf(
            PrivacyNoticeResult.InProgress(DEVICE_LANGUAGE),
            PrivacyNoticeResult.FailedBecauseBackendMaintenance(DEVICE_LANGUAGE, Throwable(), 1000L),
        )

        val privacyNoticeLiveData = privacyNoticeViewModel.viewState
        privacyNoticeViewModel.retrievePrivacyNotice()

        val value = privacyNoticeLiveData.getOrAwaitValue()
        Truth.assertThat(value).isInstanceOf(PrivacyNoticeState.BackendMaintenance::class.java)
        Truth.assertThat((value as PrivacyNoticeState.BackendMaintenance).estimatedOutage).isNotEmpty()
    }

    @Test
    fun `downloadPressed should retrieve notice when online`() = runTest {
        every { connectivityTracker.isConnected() } returns true
        coEvery { configManager.getPrivacyNotice(PROJECT_ID, DEVICE_LANGUAGE) } returns flowOf(
            PrivacyNoticeResult.InProgress(
                DEVICE_LANGUAGE,
            ),
        )

        val privacyNoticeLiveData = privacyNoticeViewModel.viewState
        val showOfflineLiveData = privacyNoticeViewModel.showOffline

        privacyNoticeViewModel.downloadPressed()

        Truth.assertThat(privacyNoticeLiveData.value).isNotNull()
        Truth.assertThat(showOfflineLiveData.value).isNull()
    }

    @Test
    fun `downloadPressed should return offline event when offline`() = runTest {
        every { connectivityTracker.isConnected() } returns false

        val privacyNoticeLiveData = privacyNoticeViewModel.viewState
        val showOfflineLiveData = privacyNoticeViewModel.showOffline

        privacyNoticeViewModel.downloadPressed()

        Truth.assertThat(privacyNoticeLiveData.value).isNull()
        Truth.assertThat(showOfflineLiveData.value).isNotNull()
    }
}
