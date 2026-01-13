package com.simprints.feature.dashboard.logout

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.*
import com.simprints.feature.dashboard.logout.usecase.LogoutUseCase
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.SettingsPasswordConfig
import com.simprints.infra.eventsync.status.models.EventSyncState
import com.simprints.infra.sync.ImageSyncStatus
import com.simprints.infra.sync.LegacySyncStates
import com.simprints.infra.sync.SyncStatus
import com.simprints.infra.sync.usecase.SyncUseCase
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.getOrAwaitValue
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class LogoutSyncViewModelTest {
    @MockK
    lateinit var logoutUseCase: LogoutUseCase

    @MockK
    lateinit var configRepository: ConfigRepository

    @MockK
    lateinit var sync: SyncUseCase

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
    fun `isLogoutWithoutSyncVisibleLiveData should return true when sync is not completed`() = runTest {
        val eventSyncState = mockk<EventSyncState> {
            every { isSyncCompleted() } returns false
        }
        val imageSyncStatus = ImageSyncStatus(isSyncing = false, progress = null, lastUpdateTimeMillis = null)

        setupSyncMocks(eventSyncState, imageSyncStatus)

        val viewModel = createViewModel()

        val result = viewModel.isLogoutWithoutSyncVisibleLiveData.getOrAwaitValue(afterObserve = ::advanceUntilIdle)
        assertThat(result).isTrue()
    }

    @Test
    fun `isLogoutWithoutSyncVisibleLiveData should return true when image sync is running`() = runTest {
        val eventSyncState = mockk<EventSyncState> {
            every { isSyncCompleted() } returns true
        }
        val imageSyncStatus = ImageSyncStatus(isSyncing = true, progress = null, lastUpdateTimeMillis = null)

        setupSyncMocks(eventSyncState, imageSyncStatus)

        val viewModel = createViewModel()

        val result = viewModel.isLogoutWithoutSyncVisibleLiveData.getOrAwaitValue(afterObserve = ::advanceUntilIdle)
        assertThat(result).isTrue()
    }

    @Test
    fun `isLogoutWithoutSyncVisibleLiveData should return false when conditions for logout are met`() = runTest {
        val eventSyncState = mockk<EventSyncState> {
            every { isSyncCompleted() } returns true
        }
        val imageSyncStatus = ImageSyncStatus(isSyncing = false, progress = null, lastUpdateTimeMillis = null)

        setupSyncMocks(eventSyncState, imageSyncStatus)

        val viewModel = createViewModel()

        val result = viewModel.isLogoutWithoutSyncVisibleLiveData.getOrAwaitValue(afterObserve = ::advanceUntilIdle)
        assertThat(result).isFalse()
    }

    private fun setupSyncMocks(
        eventSyncState: EventSyncState,
        imageSyncStatus: ImageSyncStatus,
    ) {
        val statusFlow = MutableStateFlow(
            SyncStatus(LegacySyncStates(eventSyncState = eventSyncState, imageSyncStatus = imageSyncStatus)),
        )
        every { sync.invoke(any(), any()) } returns statusFlow
    }

    private fun createViewModel() = LogoutSyncViewModel(
        configRepository = configRepository,
        sync = sync,
        authStore = authStore,
        logoutUseCase = logoutUseCase,
    )
}
