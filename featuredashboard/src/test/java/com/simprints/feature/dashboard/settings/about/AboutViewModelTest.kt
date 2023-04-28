package com.simprints.feature.dashboard.settings.about

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.DownSynchronizationConfiguration
import com.simprints.infra.config.domain.models.GeneralConfiguration
import com.simprints.infra.config.domain.models.IdentificationConfiguration
import com.simprints.infra.config.domain.models.ProjectConfiguration
import com.simprints.infra.config.domain.models.SettingsPasswordConfig
import com.simprints.infra.config.domain.models.UpSynchronizationConfiguration
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.login.LoginManager
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.infra.recent.user.activity.domain.RecentUserActivity
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.getOrAwaitValue
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class AboutViewModelTest {

    companion object {
        private val MODALITIES = listOf(GeneralConfiguration.Modality.FINGERPRINT)
        private val POOL_TYPE = IdentificationConfiguration.PoolType.MODULE
        private val PARTITION_TYPE = DownSynchronizationConfiguration.PartitionType.PROJECT
        private const val PROJECT_ID = "projectId"
    }

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val recentUserActivity = RecentUserActivity(
        "version",
        "scanner",
        "user",
        10,
        20,
        30,
        10000,
    )
    private val eventSyncManager = mockk<EventSyncManager>()

    private val loginManager = mockk<LoginManager> {
        every { getSignedInProjectIdOrEmpty() } returns PROJECT_ID
    }
    private val configManager = mockk<ConfigManager> {
        coEvery { getProjectConfiguration() } returns buildProjectConfigurationMock()
    }

    private val signerManager = mockk<SignerManager>(relaxed = true)
    private val recentUserActivityManager = mockk<RecentUserActivityManager> {
        coEvery { getRecentUserActivity() } returns recentUserActivity
    }

    @Test
    fun `should initialize the live data correctly`() {
        val viewModel = AboutViewModel(
            configManager = configManager,
            loginManager = loginManager,
            eventSyncManager = eventSyncManager,
            recentUserActivityManager = recentUserActivityManager,
            signerManager = signerManager,
            externalScope = CoroutineScope(testCoroutineRule.testCoroutineDispatcher),
        )

        assertThat(viewModel.modalities.value).isEqualTo(MODALITIES)
        assertThat(viewModel.syncAndSearchConfig.value).isEqualTo(
            SyncAndSearchConfig(
                PARTITION_TYPE.name, POOL_TYPE.name
            )
        )
        assertThat(viewModel.recentUserActivity.value).isEqualTo(recentUserActivity)
        assertThat(viewModel.settingsLocked.value).isEqualTo(SettingsPasswordConfig.Locked("1234"))
    }

    @Test
    fun `should sign out from signer manager when cannot sync data to simprints`() {
        val viewModel =
            buildLogoutViewModel(canSyncDataToSimprints = false, hasEventsToUpload = true)
        runTest {
            viewModel.processLogoutRequest()
            coVerify(exactly = 1) { signerManager.signOut() }
        }
    }

    @Test
    fun `should sign out from signer manager when can sync data to simprints but there are no events to upload`() {
        val viewModel =
            buildLogoutViewModel(canSyncDataToSimprints = true, hasEventsToUpload = false)
        runTest {
            viewModel.processLogoutRequest()
            coVerify(exactly = 1) { signerManager.signOut() }
        }
    }

    @Test
    fun `should not sign out from signer manager when can sync data to simprints and there are events to upload`() {
        val viewModel =
            buildLogoutViewModel(canSyncDataToSimprints = true, hasEventsToUpload = true)
        runTest {
            viewModel.processLogoutRequest()
            coVerify(exactly = 0) { signerManager.signOut() }
        }
    }

    @Test
    fun `should emit LogoutDestination_LogoutDataSyncScreen when can sync data to simprints and there are events to upload`() {
        val viewModel =
            buildLogoutViewModel(canSyncDataToSimprints = true, hasEventsToUpload = true)
        runTest {
            viewModel.processLogoutRequest()
            assertThat(viewModel.logoutDestinationEvent.getOrAwaitValue().peekContent()).isEqualTo(
                LogoutDestination.LogoutDataSyncScreen
            )
        }
    }

    @Test
    fun `should emit LogoutDestination_LoginScreen when can sync data to simprints but there are no events to upload`() {
        val viewModel =
            buildLogoutViewModel(canSyncDataToSimprints = true, hasEventsToUpload = false)
        runTest {
            viewModel.processLogoutRequest()
            assertThat(viewModel.logoutDestinationEvent.getOrAwaitValue().peekContent()).isEqualTo(
                LogoutDestination.LoginScreen
            )
        }
    }

    @Test
    fun `should emit LogoutDestination_LoginScreen when cannot sync data to simprints`() {
        val viewModel =
            buildLogoutViewModel(canSyncDataToSimprints = false, hasEventsToUpload = true)
        runTest {
            viewModel.processLogoutRequest()
            assertThat(viewModel.logoutDestinationEvent.getOrAwaitValue().peekContent()).isEqualTo(
                LogoutDestination.LoginScreen
            )
        }
    }

    private fun buildProjectConfigurationMock(upSyncKind: UpSynchronizationConfiguration.UpSynchronizationKind = UpSynchronizationConfiguration.UpSynchronizationKind.ALL): ProjectConfiguration =
        mockk {
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

    private fun buildLogoutViewModel(
        canSyncDataToSimprints: Boolean, hasEventsToUpload: Boolean
    ): AboutViewModel {
        val upSyncKind = when (canSyncDataToSimprints) {
            true -> UpSynchronizationConfiguration.UpSynchronizationKind.ALL
            false -> UpSynchronizationConfiguration.UpSynchronizationKind.NONE
        }
        val countEventsToUpload = when (hasEventsToUpload) {
            true -> 1
            false -> 0
        }
        coEvery { eventSyncManager.countEventsToUpload(any(), any()) } returns flowOf(
            countEventsToUpload
        )
        coEvery { configManager.getProjectConfiguration() } returns buildProjectConfigurationMock(
            upSyncKind
        )
        return AboutViewModel(
            configManager = configManager,
            loginManager = loginManager,
            eventSyncManager = eventSyncManager,
            recentUserActivityManager = recentUserActivityManager,
            signerManager = signerManager,
            externalScope = CoroutineScope(testCoroutineRule.testCoroutineDispatcher),
        )

    }
}
