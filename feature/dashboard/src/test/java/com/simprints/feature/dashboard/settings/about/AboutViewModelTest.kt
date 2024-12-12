package com.simprints.feature.dashboard.settings.about

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.jraska.livedata.test
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.feature.dashboard.logout.usecase.LogoutUseCase
import com.simprints.infra.config.store.models.DownSynchronizationConfiguration
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.config.store.models.IdentificationConfiguration
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.SettingsPasswordConfig
import com.simprints.infra.config.store.models.UpSynchronizationConfiguration
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.infra.recent.user.activity.domain.RecentUserActivity
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.getOrAwaitValue
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class AboutViewModelTest {
    companion object {
        private val MODALITIES = listOf(GeneralConfiguration.Modality.FINGERPRINT)
        private val POOL_TYPE = IdentificationConfiguration.PoolType.MODULE
        private val PARTITION_TYPE = DownSynchronizationConfiguration.PartitionType.PROJECT
    }

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val recentUserActivity = RecentUserActivity(
        lastScannerVersion = "version",
        lastScannerUsed = "scanner",
        lastUserUsed = "user".asTokenizableEncrypted(),
        enrolmentsToday = 10,
        identificationsToday = 20,
        verificationsToday = 30,
        lastActivityTime = 10000,
    )
    private val eventSyncManager = mockk<EventSyncManager>()

    private val configManager = mockk<ConfigManager> {
        coEvery { getProjectConfiguration() } returns buildProjectConfigurationMock()
    }

    private val logoutUseCase = mockk<LogoutUseCase>(relaxed = true)
    private val recentUserActivityManager = mockk<RecentUserActivityManager> {
        coEvery { getRecentUserActivity() } returns recentUserActivity
    }

    @Test
    fun `should initialize the live data correctly`() {
        val viewModel = AboutViewModel(
            configManager = configManager,
            eventSyncManager = eventSyncManager,
            recentUserActivityManager = recentUserActivityManager,
            logoutUseCase = logoutUseCase,
        )

        assertThat(viewModel.modalities.value).isEqualTo(MODALITIES)
        assertThat(viewModel.syncAndSearchConfig.value).isEqualTo(
            SyncAndSearchConfig(PARTITION_TYPE.name, POOL_TYPE.name),
        )
        assertThat(viewModel.recentUserActivity.value).isEqualTo(recentUserActivity)
        assertThat(viewModel.settingsLocked.value).isEqualTo(SettingsPasswordConfig.Locked("1234"))
    }

    @Test
    fun `should sign out from signer manager when cannot sync data to simprints`() {
        val viewModel =
            buildAboutViewModel(canSyncDataToSimprints = false, hasEventsToUpload = true)
        runTest {
            viewModel.processLogoutRequest()
            coVerify(exactly = 1) { logoutUseCase.invoke() }
        }
    }

    @Test
    fun `should sign out from signer manager when can sync data to simprints but there are no events to upload`() {
        val viewModel =
            buildAboutViewModel(canSyncDataToSimprints = true, hasEventsToUpload = false)
        runTest {
            viewModel.processLogoutRequest()
            coVerify(exactly = 1) { logoutUseCase.invoke() }
        }
    }

    @Test
    fun `should not sign out from signer manager when can sync data to simprints and there are events to upload`() {
        val viewModel =
            buildAboutViewModel(canSyncDataToSimprints = true, hasEventsToUpload = true)
        runTest {
            viewModel.processLogoutRequest()
            coVerify(exactly = 0) { logoutUseCase.invoke() }
        }
    }

    @Test
    fun `should emit LogoutDestination_LogoutDataSyncScreen when can sync data to simprints and there are events to upload`() {
        val viewModel =
            buildAboutViewModel(canSyncDataToSimprints = true, hasEventsToUpload = true)
        runTest {
            viewModel.processLogoutRequest()
            assertThat(viewModel.logoutDestinationEvent.getOrAwaitValue().peekContent()).isEqualTo(
                LogoutDestination.LogoutDataSyncScreen,
            )
        }
    }

    @Test
    fun `should emit LogoutDestination_LoginScreen when can sync data to simprints but there are no events to upload`() {
        val viewModel =
            buildAboutViewModel(canSyncDataToSimprints = true, hasEventsToUpload = false)
        runTest {
            viewModel.processLogoutRequest()
            assertThat(viewModel.logoutDestinationEvent.getOrAwaitValue().peekContent()).isEqualTo(
                LogoutDestination.LoginScreen,
            )
        }
    }

    @Test
    fun `should emit LogoutDestination_LoginScreen when cannot sync data to simprints`() {
        val viewModel =
            buildAboutViewModel(canSyncDataToSimprints = false, hasEventsToUpload = true)
        runTest {
            viewModel.processLogoutRequest()
            assertThat(viewModel.logoutDestinationEvent.getOrAwaitValue().peekContent()).isEqualTo(
                LogoutDestination.LoginScreen,
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should emit reset troubleshooting counter`() {
        val viewModel = buildAboutViewModel(canSyncDataToSimprints = false, hasEventsToUpload = true)
        runTest {
            val navigationEvent = viewModel.openTroubleshooting.test()
            repeat(3) { viewModel.troubleshootingClick() }
            advanceTimeBy(5000L)
            repeat(2) { viewModel.troubleshootingClick() }
            navigationEvent.assertNoValue()
            repeat(3) { viewModel.troubleshootingClick() }
            navigationEvent.assertHasValue()
        }
    }

    @Test
    fun `mark settings as unlocked when called`() {
        val viewModel = buildAboutViewModel(canSyncDataToSimprints = false, hasEventsToUpload = true)
        runTest {
            assertThat(viewModel.settingsLocked.value).isEqualTo(SettingsPasswordConfig.Locked("1234"))
            viewModel.unlockSettings()
            assertThat(viewModel.settingsLocked.value).isEqualTo(SettingsPasswordConfig.Unlocked)
        }
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
            every { down } returns mockk {
                every { partitionType } returns PARTITION_TYPE
            }
            every { up } returns mockk {
                every { simprints } returns mockk {
                    every { kind } returns upSyncKind
                }
            }
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
        coEvery { eventSyncManager.countEventsToUpload(any()) } returns flowOf(
            countEventsToUpload,
        )
        coEvery { configManager.getProjectConfiguration() } returns buildProjectConfigurationMock(
            upSyncKind,
        )
        return AboutViewModel(
            configManager = configManager,
            eventSyncManager = eventSyncManager,
            recentUserActivityManager = recentUserActivityManager,
            logoutUseCase = logoutUseCase,
        )
    }
}
