package com.simprints.feature.dashboard.settings.about

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.DownSynchronizationConfiguration
import com.simprints.infra.config.domain.models.GeneralConfiguration
import com.simprints.infra.config.domain.models.IdentificationConfiguration
import com.simprints.infra.config.domain.models.SettingsPasswordConfig
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.infra.recent.user.activity.domain.RecentUserActivity
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
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
        "version",
        "scanner",
        "user",
        10,
        20,
        30,
        10000,
    )
    private val configManager = mockk<ConfigManager> {
        coEvery { getProjectConfiguration() } returns mockk {
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
            }
        }
    }

    private val signerManager = mockk<SignerManager>(relaxed = true)
    private val recentUserActivityManager = mockk<RecentUserActivityManager> {
        coEvery { getRecentUserActivity() } returns recentUserActivity
    }

    @Test
    fun `should initialize the live data correctly`() {
        val viewModel = AboutViewModel(
            configManager,
            signerManager,
            recentUserActivityManager,
            CoroutineScope(testCoroutineRule.testCoroutineDispatcher),
        )

        assertThat(viewModel.modalities.value).isEqualTo(MODALITIES)
        assertThat(viewModel.syncAndSearchConfig.value).isEqualTo(
            SyncAndSearchConfig(
                PARTITION_TYPE.name,
                POOL_TYPE.name
            )
        )
        assertThat(viewModel.recentUserActivity.value).isEqualTo(recentUserActivity)
        assertThat(viewModel.settingsLocked.value).isEqualTo(SettingsPasswordConfig.Locked("1234"))
    }

    @Test
    fun `should logout correctly`() {
        val viewModel = AboutViewModel(
            configManager,
            signerManager,
            recentUserActivityManager,
            CoroutineScope(testCoroutineRule.testCoroutineDispatcher),
        )

        viewModel.logout()

        coVerify(exactly = 1) { signerManager.signOut() }
    }
}
