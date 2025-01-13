package com.simprints.feature.validatepool.usecase

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.eventsync.EventSyncManager
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ShouldSuggestSyncUseCaseTest {
    @MockK
    lateinit var timeHelper: TimeHelper

    @MockK
    lateinit var syncManager: EventSyncManager

    @MockK
    lateinit var configRepository: ConfigRepository

    private lateinit var usecase: ShouldSuggestSyncUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        usecase = ShouldSuggestSyncUseCase(timeHelper, syncManager, configRepository)
    }

    @Test
    fun `returns true if not synced ever`() = runTest {
        coEvery { syncManager.getLastSyncTime() } returns null

        assertThat(usecase()).isTrue()
    }

    @Test
    fun `returns true if not synced recently`() = runTest {
        coEvery { syncManager.getLastSyncTime() } returns Timestamp(0)
        coEvery { timeHelper.msBetweenNowAndTime(any()) } returns WEEK_MS
        coEvery {
            configRepository
                .getProjectConfiguration()
                .synchronization.down.maxAge
        } returns "PT24H"

        assertThat(usecase()).isTrue()
    }

    @Test
    fun `returns true if not synced recently with non ISO max age`() = runTest {
        coEvery { syncManager.getLastSyncTime() } returns Timestamp(0)
        coEvery { timeHelper.msBetweenNowAndTime(any()) } returns WEEK_MS
        coEvery {
            configRepository
                .getProjectConfiguration()
                .synchronization.down.maxAge
        } returns "24h0m0s"

        assertThat(usecase()).isTrue()
    }

    @Test
    fun `returns false if synced recently`() = runTest {
        coEvery { syncManager.getLastSyncTime() } returns Timestamp(0)
        coEvery { timeHelper.msBetweenNowAndTime(any()) } returns HOUR_MS
        coEvery {
            configRepository
                .getProjectConfiguration()
                .synchronization.down.maxAge
        } returns "PT24H"

        assertThat(usecase()).isFalse()
    }

    companion object {
        private const val HOUR_MS = 60 * 60 * 1000L
        private const val WEEK_MS = 7 * 24 * HOUR_MS
    }
}
