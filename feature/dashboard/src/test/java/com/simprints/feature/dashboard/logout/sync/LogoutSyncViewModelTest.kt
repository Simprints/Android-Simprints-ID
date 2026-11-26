package com.simprints.feature.dashboard.logout.sync

import app.cash.turbine.test
import com.google.common.truth.Truth.*
import com.simprints.feature.dashboard.tools.ViewModelScopeRule
import com.simprints.infra.eventsync.status.models.EventSyncState
import com.simprints.infra.sync.ImageSyncStatus
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.infra.sync.SyncStatus
import com.simprints.infra.sync.devicestate.DeviceDataState
import com.simprints.infra.sync.devicestate.DeviceStateDataTracker
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class LogoutSyncViewModelTest {
    @get:Rule(order = 0)
    val testCoroutineRule = TestCoroutineRule()

    // Inside TestCoroutineRule: scopes are cancelled while the test Main dispatcher is still set
    @get:Rule(order = 1)
    val viewModels = ViewModelScopeRule()

    private val syncOrchestrator = mockk<SyncOrchestrator>(relaxed = true)
    private val deviceStateDataTracker = mockk<DeviceStateDataTracker>(relaxed = true)

    // Defaults: everything synced, nothing pending, so "logout without sync" is hidden.
    private val syncStatus = MutableStateFlow(syncStatus(isEventSyncCompleted = true, isImageSyncing = false))
    private val deviceDataState = MutableStateFlow(deviceDataState(hasDataToUpload = false))

    private lateinit var viewModel: LogoutSyncViewModel

    @Before
    fun setUp() {
        every { syncOrchestrator.observeSyncState() } returns syncStatus
        every { deviceStateDataTracker.observeDeviceDataState() } returns deviceDataState
        viewModel = viewModels.track(
            LogoutSyncViewModel(
                syncOrchestrator = syncOrchestrator,
                deviceStateDataTracker = deviceStateDataTracker,
            ),
        )
    }

    @Test
    fun `state has no value until the first result has been stable for 1 s`() = runTest {
        assertThat(viewModel.uiState.value).isEqualTo(LogoutSyncUiState(isLogoutWithoutSyncVisible = null))

        viewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(LogoutSyncUiState(isLogoutWithoutSyncVisible = null))

            advanceTimeBy(999)
            expectNoEvents()

            advanceTimeBy(1)
            runCurrent()
            assertThat(awaitItem()).isEqualTo(LogoutSyncUiState(isLogoutWithoutSyncVisible = false))
        }
    }

    @Test
    fun `logout without sync is visible when event sync is not completed`() = runTest {
        syncStatus.value = syncStatus(isEventSyncCompleted = false, isImageSyncing = false)

        assertThat(firstValue()).isEqualTo(LogoutSyncUiState(isLogoutWithoutSyncVisible = true))
    }

    @Test
    fun `logout without sync is visible when image sync is running`() = runTest {
        syncStatus.value = syncStatus(isEventSyncCompleted = true, isImageSyncing = true)

        assertThat(firstValue()).isEqualTo(LogoutSyncUiState(isLogoutWithoutSyncVisible = true))
    }

    @Test
    fun `logout without sync is visible when sync completed but there is still data to upload`() = runTest {
        deviceDataState.value = deviceDataState(hasDataToUpload = true)

        assertThat(firstValue()).isEqualTo(LogoutSyncUiState(isLogoutWithoutSyncVisible = true))
    }

    @Test
    fun `logout without sync is hidden when events are synced, images are idle and no data is pending`() = runTest {
        assertThat(firstValue()).isEqualTo(LogoutSyncUiState(isLogoutWithoutSyncVisible = false))
    }

    @Test
    fun `visibility follows later changes of either source`() = runTest {
        deviceDataState.value = deviceDataState(hasDataToUpload = true)

        viewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(LogoutSyncUiState(isLogoutWithoutSyncVisible = null))
            advanceTimeBy(1_000)
            runCurrent()
            assertThat(awaitItem()).isEqualTo(LogoutSyncUiState(isLogoutWithoutSyncVisible = true))

            deviceDataState.value = deviceDataState(hasDataToUpload = false)
            advanceTimeBy(1_000)
            runCurrent()
            assertThat(awaitItem()).isEqualTo(LogoutSyncUiState(isLogoutWithoutSyncVisible = false))

            syncStatus.value = syncStatus(isEventSyncCompleted = true, isImageSyncing = true)
            advanceTimeBy(1_000)
            runCurrent()
            assertThat(awaitItem()).isEqualTo(LogoutSyncUiState(isLogoutWithoutSyncVisible = true))
        }
    }

    @Test
    fun `values that change within the anti-jitter delay deliver only the last one 1 s after the last change`() = runTest {
        syncStatus.value = syncStatus(isEventSyncCompleted = false, isImageSyncing = false)

        viewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(LogoutSyncUiState(isLogoutWithoutSyncVisible = null))

            // t = 0: true, t = 400: false, t = 800: true
            advanceTimeBy(400)
            syncStatus.value = syncStatus(isEventSyncCompleted = true, isImageSyncing = false)
            advanceTimeBy(400)
            syncStatus.value = syncStatus(isEventSyncCompleted = true, isImageSyncing = true)

            // t = 1 799: neither the first value (due at 1 000) nor the second (due at 1 400) was delivered
            advanceTimeBy(999)
            expectNoEvents()

            // t = 1 800: 1 s after the last change
            advanceTimeBy(1)
            runCurrent()
            assertThat(awaitItem()).isEqualTo(LogoutSyncUiState(isLogoutWithoutSyncVisible = true))
            expectNoEvents()
        }
    }

    @Test
    fun `sources stay observed for 5 s after the last collector leaves and the last value is kept`() = runTest {
        viewModel.uiState.test {
            skipItems(1)
            advanceTimeBy(1_000)
            runCurrent()
            assertThat(awaitItem()).isEqualTo(LogoutSyncUiState(isLogoutWithoutSyncVisible = false))
        }
        assertThat(syncStatus.subscriptionCount.value).isEqualTo(1)

        advanceTimeBy(4_999)
        assertThat(syncStatus.subscriptionCount.value).isEqualTo(1)

        advanceTimeBy(1)
        runCurrent()
        assertThat(syncStatus.subscriptionCount.value).isEqualTo(0)
        assertThat(deviceDataState.subscriptionCount.value).isEqualTo(0)
        assertThat(viewModel.uiState.value).isEqualTo(LogoutSyncUiState(isLogoutWithoutSyncVisible = false))
    }

    @Test
    fun `the screen only observes sync state and issues no sync command`() = runTest {
        assertThat(firstValue()).isEqualTo(LogoutSyncUiState(isLogoutWithoutSyncVisible = false))

        verify(exactly = 1) { syncOrchestrator.observeSyncState() }
        verify(exactly = 1) { deviceStateDataTracker.observeDeviceDataState() }
        confirmVerified(syncOrchestrator, deviceStateDataTracker)
    }

    // Subscribes (uiState is WhileSubscribed), skips the initial "no value yet" state and returns the first debounced state.
    private suspend fun TestScope.firstValue(): LogoutSyncUiState {
        var value: LogoutSyncUiState? = null
        viewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(LogoutSyncUiState(isLogoutWithoutSyncVisible = null))
            advanceTimeBy(1_000)
            runCurrent()
            value = awaitItem()
            expectNoEvents()
        }
        return checkNotNull(value)
    }

    private fun syncStatus(
        isEventSyncCompleted: Boolean,
        isImageSyncing: Boolean,
    ) = SyncStatus(
        eventSyncState = mockk<EventSyncState> {
            every { isSyncCompleted() } returns isEventSyncCompleted
        },
        imageSyncStatus = ImageSyncStatus(isSyncing = isImageSyncing, progress = null, lastUpdateTimeMillis = null),
    )

    private fun deviceDataState(hasDataToUpload: Boolean) = mockk<DeviceDataState> {
        every { this@mockk.hasDataToUpload } returns hasDataToUpload
    }
}
