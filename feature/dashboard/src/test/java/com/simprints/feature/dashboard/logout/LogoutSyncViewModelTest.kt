package com.simprints.feature.dashboard.logout

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.asFlow
import com.google.common.truth.Truth.assertThat
import com.simprints.feature.dashboard.logout.usecase.LogoutUseCase
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.SettingsPasswordConfig
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.eventsync.status.models.EventSyncState
import com.simprints.infra.sync.ImageSyncStatus
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.getOrAwaitValue
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class LogoutSyncViewModelTest {
    @MockK
    lateinit var logoutUseCase: LogoutUseCase

    @MockK
    lateinit var configManager: ConfigManager

    @MockK
    lateinit var eventSyncManager: EventSyncManager

    @MockK
    lateinit var syncOrchestrator: SyncOrchestrator

    @MockK
    lateinit var authStore: AuthStore

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        // Setup default behavior for logoutUseCase
        every { logoutUseCase() } returns Unit
    }

    @Test
    fun `should logout correctly`() {
        val viewModel = createViewModel()

        viewModel.logout()

        verify(exactly = 1) { logoutUseCase() }
    }

    @Test
    fun `password config should be fetched after initialization`() {
        val config = SettingsPasswordConfig.Locked(password = "123")
        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { general } returns mockk {
                every { settingsPassword } returns config
            }
        }
        val viewModel = createViewModel()
        val resultConfig = viewModel.settingsLocked.getOrAwaitValue()
        assertThat(resultConfig.peekContent()).isEqualTo(config)
    }

    @Test
    fun `logoutEventLiveData should emit momentarily when user is signed out`() {
        every { authStore.observeSignedInProjectId() } returns MutableStateFlow("")

        val viewModel = createViewModel()

        val result = viewModel.logoutEventLiveData.getOrAwaitValue()
        assertThat(result).isEqualTo(Unit)
    }

    @Test
    fun `logoutEventLiveData should not emit when user is signed in`() {
        every { authStore.observeSignedInProjectId() } returns MutableStateFlow("userId123")

        val viewModel = createViewModel()

        assertThat(viewModel.logoutEventLiveData.value).isNull()
    }

    @Test
    fun `isLogoutWithoutSyncVisibleLiveData should return true when sync is not completed`() {
        val eventSyncState = mockk<EventSyncState> {
            every { isSyncCompleted() } returns false
        }
        val imageSyncStatus = ImageSyncStatus(isSyncing = false, progress = null, lastUpdateTimeMillis = null)
        val projectConfig = mockk<ProjectConfiguration>(relaxed = true)

        setupSyncMocks(eventSyncState, imageSyncStatus, projectConfig)

        val viewModel = createViewModel()

        val result = viewModel.isLogoutWithoutSyncVisibleLiveData.getOrAwaitValue()
        assertThat(result).isTrue()
    }

    @Test
    fun `isLogoutWithoutSyncVisibleLiveData should return true when image sync is running`() {
        val eventSyncState = mockk<EventSyncState> {
            every { isSyncCompleted() } returns true
        }
        val imageSyncStatus = ImageSyncStatus(isSyncing = true, progress = null, lastUpdateTimeMillis = null)
        val projectConfig = mockk<ProjectConfiguration>(relaxed = true)

        setupSyncMocks(eventSyncState, imageSyncStatus, projectConfig)

        val viewModel = createViewModel()

        val result = viewModel.isLogoutWithoutSyncVisibleLiveData.getOrAwaitValue()
        assertThat(result).isTrue()
    }

    @Test
    fun `isLogoutWithoutSyncVisibleLiveData should return false when conditions for logout are met`() {
        val eventSyncState = mockk<EventSyncState> {
            every { isSyncCompleted() } returns true
        }
        val imageSyncStatus = ImageSyncStatus(isSyncing = false, progress = null, lastUpdateTimeMillis = null)
        val projectConfig = mockk<ProjectConfiguration>(relaxed = true)

        setupSyncMocks(eventSyncState, imageSyncStatus, projectConfig)

        val viewModel = createViewModel()

        val result = viewModel.isLogoutWithoutSyncVisibleLiveData.getOrAwaitValue()
        assertThat(result).isFalse()
    }

    private fun setupSyncMocks(
        eventSyncState: EventSyncState,
        imageSyncStatus: ImageSyncStatus,
        projectConfig: ProjectConfiguration,
    ) {
        mockkStatic("androidx.lifecycle.FlowLiveDataConversions")
        val eventSyncLiveData = mockk<LiveData<EventSyncState>>(relaxed = true)
        every { eventSyncLiveData.asFlow() } returns flowOf(eventSyncState)
        every { eventSyncManager.getLastSyncState(useDefaultValue = true) } returns eventSyncLiveData
        every { syncOrchestrator.observeImageSyncStatus() } returns flowOf(imageSyncStatus)
        every { configManager.observeProjectConfiguration() } returns flowOf(projectConfig)
    }

    private fun createViewModel() = LogoutSyncViewModel(
        configManager = configManager,
        eventSyncManager = eventSyncManager,
        syncOrchestrator = syncOrchestrator,
        authStore = authStore,
        logoutUseCase = logoutUseCase,
    )
}
