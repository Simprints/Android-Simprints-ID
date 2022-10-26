package com.simprints.id.activities.settings.fragments.settingsAbout

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.id.secure.SignerManager
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.ProjectConfiguration
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.infra.recent.user.activity.domain.RecentUserActivity
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.getOrAwaitValue
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsAboutViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val projectConfiguration = mockk<ProjectConfiguration>()
    private val recentUserActivity = mockk<RecentUserActivity>()
    private val configManager = mockk<ConfigManager> {
        coEvery { getProjectConfiguration() } returns projectConfiguration
    }
    private val recentUserActivityManager = mockk<RecentUserActivityManager> {
        coEvery { getRecentUserActivity() } returns recentUserActivity
    }
    private val signerManager = mockk<SignerManager>(relaxed = true)
    private lateinit var viewModel: SettingsAboutViewModel

    @Before
    fun setup() {
        viewModel = SettingsAboutViewModel(
            configManager,
            signerManager,
            recentUserActivityManager,
            testCoroutineRule.testCoroutineDispatcher
        )
    }

    @Test
    fun `should init the live data with the configuration`() {
        assertThat(viewModel.configuration.getOrAwaitValue()).isEqualTo(projectConfiguration)
    }

    @Test
    fun `should init the live data with the recent user activity`() {
        assertThat(viewModel.recentUserActivity.getOrAwaitValue()).isEqualTo(recentUserActivity)
    }

    @Test
    fun `should call the correct method to logout`() {
        viewModel.logout()

        coVerify(exactly = 1) { signerManager.signOut() }
    }
}
