package com.simprints.feature.validatepool.usecase

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.eventsync.status.models.DownSyncState
import com.simprints.infra.sync.SyncOrchestrator
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ShouldSuggestSyncUseCaseTest {
    @MockK
    lateinit var timeHelper: TimeHelper

    @MockK
    lateinit var syncOrchestrator: SyncOrchestrator

    @MockK
    lateinit var configRepository: ConfigRepository

    private lateinit var usecase: ShouldSuggestSyncUseCase
    private lateinit var syncStatusFlow: MutableStateFlow<DownSyncState>

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        syncStatusFlow = MutableStateFlow(createDownSyncState(lastSyncTime = null))
        every { syncOrchestrator.observeDownSyncState() } returns syncStatusFlow

        usecase = ShouldSuggestSyncUseCase(timeHelper, syncOrchestrator, configRepository)
    }

    @Test
    fun `returns true if not synced ever`() = runTest {
        syncStatusFlow.value = createDownSyncState(lastSyncTime = null)

        assertThat(usecase()).isTrue()
    }

    @Test
    fun `returns true if not synced recently`() = runTest {
        syncStatusFlow.value = createDownSyncState(lastSyncTime = Timestamp(0))
        coEvery { timeHelper.msBetweenNowAndTime(any()) } returns WEEK_MS
        coEvery {
            configRepository
                .getProjectConfiguration()
                .synchronization.down.simprints
                ?.maxAge
        } returns "PT24H"

        assertThat(usecase()).isTrue()
    }

    @Test
    fun `returns true if not synced recently with non ISO max age`() = runTest {
        syncStatusFlow.value = createDownSyncState(lastSyncTime = Timestamp(0))
        coEvery { timeHelper.msBetweenNowAndTime(any()) } returns WEEK_MS
        coEvery {
            configRepository
                .getProjectConfiguration()
                .synchronization.down.simprints
                ?.maxAge
        } returns "24h0m0s"

        assertThat(usecase()).isTrue()
    }

    @Test
    fun `returns false if synced recently`() = runTest {
        syncStatusFlow.value = createDownSyncState(lastSyncTime = Timestamp(0))
        coEvery { timeHelper.msBetweenNowAndTime(any()) } returns HOUR_MS
        coEvery {
            configRepository
                .getProjectConfiguration()
                .synchronization.down.simprints
                ?.maxAge
        } returns "PT24H"

        assertThat(usecase()).isFalse()
    }

    @Test
    fun `returns false if not Simprints sync`() = runTest {
        syncStatusFlow.value = createDownSyncState(lastSyncTime = Timestamp(0))
        coEvery { timeHelper.msBetweenNowAndTime(any()) } returns HOUR_MS
        coEvery {
            configRepository
                .getProjectConfiguration()
                .synchronization.down.simprints
        } returns null

        assertThat(usecase()).isFalse()
    }

    private fun createDownSyncState(lastSyncTime: Timestamp?): DownSyncState = DownSyncState(
        syncId = "",
        workersInfo = emptyList(),
        progress = null,
        total = null,
        lastSyncTime = lastSyncTime,
    )

    companion object {
        private const val HOUR_MS = 60 * 60 * 1000L
        private const val WEEK_MS = 7 * 24 * HOUR_MS
    }
}
