package com.simprints.feature.dashboard.logout

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.*
import com.simprints.feature.dashboard.logout.usecase.LogoutUseCase
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.SettingsPasswordConfig
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.eventsync.status.models.EventSyncState
import com.simprints.infra.sync.ImageSyncStatus
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.getOrAwaitValue
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class LogoutSyncViewModelTest {
    @MockK
    lateinit var logoutUseCase: LogoutUseCase

    @MockK
    lateinit var configRepository: ConfigRepository

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
        coEvery { logoutUseCase() } returns Unit
    }

    @Test
    fun `should logout correctly`() = runTest {
        val viewModel = createViewModel()

        viewModel.logout()

        coVerify(exactly = 1) { logoutUseCase() }
    }

    @Test
    fun `password config should be fetched after initialization`() {
        val config = SettingsPasswordConfig.Locked(password = "123")
        coEvery { configRepository.getProjectConfiguration() } returns mockk {
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
        val eventSyncFlow = flowOf(eventSyncState)
        every { eventSyncManager.getLastSyncState(useDefaultValue = true) } returns eventSyncFlow
        every { syncOrchestrator.observeImageSyncStatus() } returns flowOf(imageSyncStatus)
        every { configRepository.observeProjectConfiguration() } returns flowOf(projectConfig)
    }

    private fun createViewModel() = LogoutSyncViewModel(
        configRepository = configRepository,
        eventSyncManager = eventSyncManager,
        syncOrchestrator = syncOrchestrator,
        authStore = authStore,
        logoutUseCase = logoutUseCase,
    )
}
