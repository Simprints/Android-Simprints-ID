package com.simprints.feature.dashboard.settings.about

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.*
import com.jraska.livedata.test
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.feature.dashboard.logout.usecase.LogoutUseCase
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.DownSynchronizationConfiguration
import com.simprints.infra.config.store.models.IdentificationConfiguration
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.SettingsPasswordConfig
import com.simprints.infra.config.store.models.UpSynchronizationConfiguration
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.infra.recent.user.activity.domain.RecentUserActivity
import com.simprints.infra.sync.SyncableCounts
import com.simprints.infra.sync.usecase.CountSyncableUseCase
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.getOrAwaitValue
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AboutViewModelTest {
    companion object {
        private val MODALITIES = listOf(Modality.FINGERPRINT)
        private val POOL_TYPE = IdentificationConfiguration.PoolType.MODULE
        private val PARTITION_TYPE = DownSynchronizationConfiguration.PartitionType.PROJECT
    }

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()
    private val testDispatcher = testCoroutineRule.testCoroutineDispatcher

    private val recentUserActivity = RecentUserActivity(
        lastScannerVersion = "version",
        lastScannerUsed = "scanner",
        lastUserUsed = "user".asTokenizableEncrypted(),
        enrolmentsToday = 10,
        identificationsToday = 20,
        verificationsToday = 30,
        lastActivityTime = 10000,
    )

    @MockK
    lateinit var countSyncable: CountSyncableUseCase

    @MockK
    lateinit var configRepository: ConfigRepository

    @MockK
    internal lateinit var logoutUseCase: LogoutUseCase

    @MockK
    lateinit var recentUserActivityManager: RecentUserActivityManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        coEvery { configRepository.getProjectConfiguration() } returns buildProjectConfigurationMock()
        coEvery { recentUserActivityManager.getRecentUserActivity() } returns recentUserActivity
    }

    @Test
    fun `should initialize the live data correctly`() = runTest(testDispatcher) {
        val viewModel = AboutViewModel(
            configRepository = configRepository,
            countSyncable = countSyncable,
            recentUserActivityManager = recentUserActivityManager,
            logoutUseCase = logoutUseCase,
        )

        advanceUntilIdle()
        assertThat(viewModel.modalities.getOrAwaitValue()).isEqualTo(MODALITIES)
        assertThat(viewModel.syncAndSearchConfig.getOrAwaitValue()).isEqualTo(
            SyncAndSearchConfig(PARTITION_TYPE.name, POOL_TYPE.name),
        )
        assertThat(viewModel.recentUserActivity.getOrAwaitValue()).isEqualTo(recentUserActivity)
        assertThat(viewModel.settingsLocked.getOrAwaitValue()).isEqualTo(SettingsPasswordConfig.Locked("1234"))
    }

    @Test
    fun `should sign out from signer manager when cannot sync data to simprints`() = runTest(testDispatcher) {
        val viewModel =
            buildAboutViewModel(canSyncDataToSimprints = false, hasEventsToUpload = true)
        advanceUntilIdle()
        viewModel.processLogoutRequest()
        advanceUntilIdle()
        coVerify(exactly = 1) { logoutUseCase.invoke() }
    }

    @Test
    fun `should sign out from signer manager when can sync data to simprints but there are no events to upload`() =
        runTest(testDispatcher) {
            val viewModel =
                buildAboutViewModel(canSyncDataToSimprints = true, hasEventsToUpload = false)
            advanceUntilIdle()
            viewModel.processLogoutRequest()
            advanceUntilIdle()
            coVerify(exactly = 1) { logoutUseCase.invoke() }
        }

    @Test
    fun `should not sign out from signer manager when can sync data to simprints and there are events to upload`() =
        runTest(testDispatcher) {
            val viewModel =
                buildAboutViewModel(canSyncDataToSimprints = true, hasEventsToUpload = true)
            advanceUntilIdle()
            viewModel.processLogoutRequest()
            advanceUntilIdle()
            coVerify(exactly = 0) { logoutUseCase.invoke() }
        }

    @Test
    fun `should emit LogoutDestination_LogoutDataSyncScreen when can sync data to simprints and there are events to upload`() =
        runTest(testDispatcher) {
            val viewModel =
                buildAboutViewModel(canSyncDataToSimprints = true, hasEventsToUpload = true)
            advanceUntilIdle()
            viewModel.processLogoutRequest()
            assertThat(viewModel.logoutDestinationEvent.getOrAwaitValue().peekContent()).isEqualTo(
                LogoutDestination.LogoutDataSyncScreen,
            )
        }

    @Test
    fun `should emit LogoutDestination_LoginScreen when can sync data to simprints but there are no events to upload`() =
        runTest(testDispatcher) {
            val viewModel =
                buildAboutViewModel(canSyncDataToSimprints = true, hasEventsToUpload = false)
            advanceUntilIdle()
            viewModel.processLogoutRequest()
            assertThat(viewModel.logoutDestinationEvent.getOrAwaitValue().peekContent()).isEqualTo(
                LogoutDestination.LoginScreen,
            )
        }

    @Test
    fun `should emit LogoutDestination_LoginScreen when cannot sync data to simprints`() = runTest(testDispatcher) {
        val viewModel =
            buildAboutViewModel(canSyncDataToSimprints = false, hasEventsToUpload = true)
        advanceUntilIdle()
        viewModel.processLogoutRequest()
        assertThat(viewModel.logoutDestinationEvent.getOrAwaitValue().peekContent()).isEqualTo(
            LogoutDestination.LoginScreen,
        )
    }

    @Test
    fun `should emit reset troubleshooting counter`() = runTest(testDispatcher) {
        val viewModel = buildAboutViewModel(canSyncDataToSimprints = false, hasEventsToUpload = true)
        advanceUntilIdle()
        val navigationEvent = viewModel.openTroubleshooting.test()
        repeat(3) { viewModel.troubleshootingClick() }
        advanceTimeBy(5000L)
        repeat(2) { viewModel.troubleshootingClick() }
        navigationEvent.assertNoValue()
        repeat(3) { viewModel.troubleshootingClick() }
        navigationEvent.assertHasValue()
    }

    @Test
    fun `mark settings as unlocked when called`() = runTest(testDispatcher) {
        val viewModel = buildAboutViewModel(canSyncDataToSimprints = false, hasEventsToUpload = true)
        advanceUntilIdle()
        assertThat(viewModel.settingsLocked.getOrAwaitValue()).isEqualTo(SettingsPasswordConfig.Locked("1234"))
        viewModel.unlockSettings()
        assertThat(viewModel.settingsLocked.getOrAwaitValue()).isEqualTo(SettingsPasswordConfig.Unlocked)
    }

    private fun buildProjectConfigurationMock(
        upSyncKind: UpSynchronizationConfiguration.UpSynchronizationKind = UpSynchronizationConfiguration.UpSynchronizationKind.ALL,
    ): ProjectConfiguration = mockk {
        every { general } returns mockk {
            every { modalities } returns MODALITIES
            every { settingsPassword } returns SettingsPasswordConfig.Locked("1234")
        }
        every { identification } returns mockk {
            every { poolType } returns POOL_TYPE
        }
        every { synchronization } returns mockk {
            every { down.simprints?.partitionType } returns PARTITION_TYPE
            every { up.simprints.kind } returns upSyncKind
        }
    }

    private fun buildAboutViewModel(
        canSyncDataToSimprints: Boolean,
        hasEventsToUpload: Boolean,
    ): AboutViewModel {
        val upSyncKind = when (canSyncDataToSimprints) {
            true -> UpSynchronizationConfiguration.UpSynchronizationKind.ALL
            false -> UpSynchronizationConfiguration.UpSynchronizationKind.NONE
        }
        val countEventsToUpload = when (hasEventsToUpload) {
            true -> 1
            false -> 0
        }
        every { countSyncable.invoke() } returns flowOf(
            SyncableCounts(
                totalRecords = 0,
                recordEventsToDownload = 0,
                isRecordEventsToDownloadLowerBound = false,
                eventsToUpload = countEventsToUpload,
                enrolmentsToUpload = 0,
                samplesToUpload = 0,
            ),
        )
        coEvery { configRepository.getProjectConfiguration() } returns buildProjectConfigurationMock(
            upSyncKind,
        )
        return AboutViewModel(
            configRepository = configRepository,
            countSyncable = countSyncable,
            recentUserActivityManager = recentUserActivityManager,
            logoutUseCase = logoutUseCase,
        )
    }
}
