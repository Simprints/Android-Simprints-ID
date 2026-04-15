package com.simprints.feature.dashboard.logout

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.*
import com.simprints.feature.dashboard.logout.usecase.LogoutUseCase
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.ProjectState
import com.simprints.infra.config.store.models.SettingsPasswordConfig
import com.simprints.infra.config.store.models.isCommCareEventDownSyncAllowed
import com.simprints.infra.config.store.models.isSimprintsEventDownSyncAllowed
import com.simprints.infra.eventsync.status.models.DownSyncState
import com.simprints.infra.eventsync.status.models.UpSyncState
import com.simprints.infra.sync.ImageSyncStatus
import com.simprints.infra.sync.SyncStatus
import com.simprints.infra.sync.SyncOrchestrator
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
        mockkStatic("com.simprints.infra.config.store.models.ProjectConfigurationKt")
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
        val imageSyncStatus = ImageSyncStatus(isSyncing = false, progress = null, lastUpdateTimeMillis = null)

        setupSyncMocks(upSyncCompleted = false, imageSyncStatus = imageSyncStatus)

        val viewModel = createViewModel()

        val result = viewModel.isLogoutWithoutSyncVisibleLiveData.getOrAwaitValue(afterObserve = ::advanceUntilIdle)
        assertThat(result).isTrue()
    }

    @Test
    fun `isLogoutWithoutSyncVisibleLiveData should return true when image sync is running`() = runTest {
        val imageSyncStatus = ImageSyncStatus(isSyncing = true, progress = null, lastUpdateTimeMillis = null)

        setupSyncMocks(upSyncCompleted = true, imageSyncStatus = imageSyncStatus)

        val viewModel = createViewModel()

        val result = viewModel.isLogoutWithoutSyncVisibleLiveData.getOrAwaitValue(afterObserve = ::advanceUntilIdle)
        assertThat(result).isTrue()
    }

    @Test
    fun `isLogoutWithoutSyncVisibleLiveData should return false when conditions for logout are met`() = runTest {
        val imageSyncStatus = ImageSyncStatus(isSyncing = false, progress = null, lastUpdateTimeMillis = null)

        setupSyncMocks(upSyncCompleted = true, imageSyncStatus = imageSyncStatus)

        val viewModel = createViewModel()

        val result = viewModel.isLogoutWithoutSyncVisibleLiveData.getOrAwaitValue(afterObserve = ::advanceUntilIdle)
        assertThat(result).isFalse()
    }

    @Test
    fun `isLogoutWithoutSyncVisibleLiveData should return false when project is ending and up sync is completed`() = runTest {
        val imageSyncStatus = ImageSyncStatus(isSyncing = false, progress = null, lastUpdateTimeMillis = null)

        setupSyncMocks(
            upSyncCompleted = true,
            downSyncCompleted = false,
            imageSyncStatus = imageSyncStatus,
            projectState = ProjectState.PROJECT_ENDING,
        )

        val viewModel = createViewModel()

        val result = viewModel.isLogoutWithoutSyncVisibleLiveData.getOrAwaitValue(afterObserve = ::advanceUntilIdle)
        assertThat(result).isFalse()
    }

    @Test
    fun `isLogoutWithoutSyncVisibleLiveData should return false when project is paused and up sync is completed`() = runTest {
        val imageSyncStatus = ImageSyncStatus(isSyncing = false, progress = null, lastUpdateTimeMillis = null)

        setupSyncMocks(
            upSyncCompleted = true,
            downSyncCompleted = false,
            imageSyncStatus = imageSyncStatus,
            projectState = ProjectState.PROJECT_PAUSED,
        )

        val viewModel = createViewModel()

        val result = viewModel.isLogoutWithoutSyncVisibleLiveData.getOrAwaitValue(afterObserve = ::advanceUntilIdle)
        assertThat(result).isFalse()
    }

    private fun setupSyncMocks(
        upSyncCompleted: Boolean,
        downSyncCompleted: Boolean = upSyncCompleted,
        imageSyncStatus: ImageSyncStatus,
        projectState: ProjectState = ProjectState.RUNNING,
        downSyncAllowed: Boolean = true,
    ) {
        val mockProject = mockk<Project> { every { state } returns projectState }
        coEvery { configRepository.getProject() } returns mockProject
        val mockConfig = mockk<com.simprints.infra.config.store.models.ProjectConfiguration>(relaxed = true)
        coEvery { configRepository.getProjectConfiguration() } returns mockConfig
        every { mockConfig.isSimprintsEventDownSyncAllowed() } returns downSyncAllowed
        every { mockConfig.isCommCareEventDownSyncAllowed() } returns false

        val upSyncState = mockk<UpSyncState>(relaxed = true) { every { isSyncCompleted() } returns upSyncCompleted }
        val downSyncState = mockk<DownSyncState>(relaxed = true) { every { isSyncCompleted() } returns downSyncCompleted }
        val statusFlow = MutableStateFlow(SyncStatus(upSyncState = upSyncState, downSyncState = downSyncState, imageSyncStatus = imageSyncStatus))
        every { syncOrchestrator.observeSyncState() } returns statusFlow
    }

    private fun createViewModel() = LogoutSyncViewModel(
        configRepository = configRepository,
        syncOrchestrator = syncOrchestrator,
        authStore = authStore,
        logoutUseCase = logoutUseCase,
    )
}
