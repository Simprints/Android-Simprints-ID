package com.simprints.id.activities.settings.fragments.settingsAbout

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.id.secure.SignerManager
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.ProjectConfiguration
import com.simprints.testtools.common.livedata.getOrAwaitValue
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsAboutViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val projectConfiguration = mockk<ProjectConfiguration>()
    private val configManager = mockk<ConfigManager> {
        coEvery { getProjectConfiguration() } returns projectConfiguration
    }
    private val signerManager = mockk<SignerManager>(relaxed = true)
    private lateinit var viewModel: SettingsAboutViewModel

    @Before
    fun setup() {
        viewModel = SettingsAboutViewModel(configManager, signerManager, UnconfinedTestDispatcher())
    }

    @Test
    fun `should init the live data with the configuration`() {
        assertThat(viewModel.configuration.getOrAwaitValue()).isEqualTo(projectConfiguration)
    }

    @Test
    fun `should call the correct method to logout`() {
        viewModel.logout()

        coVerify(exactly = 1) { signerManager.signOut() }
    }
}
