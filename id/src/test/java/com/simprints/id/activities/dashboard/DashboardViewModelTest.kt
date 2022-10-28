package com.simprints.id.activities.dashboard

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.id.activities.dashboard.cards.daily_activity.model.DashboardDailyActivityState
import com.simprints.id.activities.dashboard.cards.daily_activity.repository.DashboardDailyActivityRepository
import com.simprints.id.activities.dashboard.cards.project.model.DashboardProjectState
import com.simprints.id.activities.dashboard.cards.project.repository.DashboardProjectDetailsRepository
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardStateRepository
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.SynchronizationConfiguration
import com.simprints.infra.config.domain.models.UpSynchronizationConfiguration
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.getOrAwaitValue
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test

class DashboardViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val dailyActivity = mockk<DashboardDailyActivityState>()
    private val projectState = mockk<DashboardProjectState>()
    private val synchronizationConfiguration = mockk<SynchronizationConfiguration>()
    private val projectDetailsRepository = mockk<DashboardProjectDetailsRepository> {
        coEvery { getProjectDetails() } returns projectState
    }
    private val syncCardStateRepository = mockk<DashboardSyncCardStateRepository>(relaxed = true)
    private val dailyActivityRepository = mockk<DashboardDailyActivityRepository> {
        coEvery { getDailyActivity() } returns dailyActivity
    }
    private val configManager = mockk<ConfigManager> {
        coEvery { getProjectConfiguration() } returns mockk {
            every { consent } returns mockk {
                every { collectConsent } returns true
            }
            every { synchronization } returns synchronizationConfiguration
        }
    }
    private lateinit var viewModel: DashboardViewModel

    @Test
    fun `should init the consentRequired live data correctly`() {
        init()
        assertThat(viewModel.consentRequired).isEqualTo(true)
    }

    @Test
    fun `should init the dailyActivity live data correctly`() {
        init()
        assertThat(viewModel.dailyActivity.getOrAwaitValue()).isEqualTo(dailyActivity)
    }

    @Test
    fun `should init the projectCardStateLiveData live data correctly`() {
        init()
        assertThat(viewModel.projectCardStateLiveData.getOrAwaitValue()).isEqualTo(projectState)
    }

    @Test
    fun `should init the syncToBFSIDAllowed live data to true if it can upsync`() {
        every { synchronizationConfiguration.up.simprints.kind } returns UpSynchronizationConfiguration.UpSynchronizationKind.ALL
        every { synchronizationConfiguration.frequency } returns SynchronizationConfiguration.Frequency.ONLY_PERIODICALLY_UP_SYNC
        init()
        assertThat(viewModel.syncToBFSIDAllowed.getOrAwaitValue()).isEqualTo(true)
    }

    @Test
    fun `should init the syncToBFSIDAllowed live data to true if it can downsync`() {
        every { synchronizationConfiguration.frequency } returns SynchronizationConfiguration.Frequency.PERIODICALLY_AND_ON_SESSION_START
        every { synchronizationConfiguration.up.simprints.kind } returns UpSynchronizationConfiguration.UpSynchronizationKind.NONE
        init()
        assertThat(viewModel.syncToBFSIDAllowed.getOrAwaitValue()).isEqualTo(true)
    }

    @Test
    fun `should init the syncToBFSIDAllowed live data to false if it can't downsync or upsync`() {
        every { synchronizationConfiguration.frequency } returns SynchronizationConfiguration.Frequency.ONLY_PERIODICALLY_UP_SYNC
        every { synchronizationConfiguration.up.simprints.kind } returns UpSynchronizationConfiguration.UpSynchronizationKind.NONE
        init()
        assertThat(viewModel.syncToBFSIDAllowed.getOrAwaitValue()).isEqualTo(false)
    }

    @Test
    fun `syncIfRequired should call the correct method`() {
        init()
        viewModel.syncIfRequired()

        coVerify(exactly = 1) { syncCardStateRepository.syncIfRequired() }
    }

    private fun init() {
        viewModel = DashboardViewModel(
            projectDetailsRepository,
            syncCardStateRepository,
            dailyActivityRepository,
            configManager,
            testCoroutineRule.testCoroutineDispatcher
        )
    }
}
