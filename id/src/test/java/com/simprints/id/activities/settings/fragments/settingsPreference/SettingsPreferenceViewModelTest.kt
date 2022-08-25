package com.simprints.id.activities.settings.fragments.settingsPreference

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.DeviceConfiguration
import com.simprints.infra.config.domain.models.GeneralConfiguration
import com.simprints.testtools.common.livedata.getOrAwaitValue
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsPreferenceViewModelTest {

    companion object {
        private const val LANGUAGE = "fr"
    }

    @get:Rule
    val rule = InstantTaskExecutorRule()


    private val generalConfiguration = mockk<GeneralConfiguration>()
    private val configManager = mockk<ConfigManager> {
        coEvery { getProjectConfiguration() } returns mockk {
            every { general } returns generalConfiguration
        }
        coEvery { getDeviceConfiguration() } returns mockk {
            every { language } returns LANGUAGE
        }
    }
    private lateinit var viewModel: SettingsPreferenceViewModel

    @Before
    fun setup() {
        viewModel = SettingsPreferenceViewModel(configManager, UnconfinedTestDispatcher())
    }

    @Test
    fun `should init the live data with the configuration`() {
        assertThat(viewModel.generalConfiguration.getOrAwaitValue()).isEqualTo(generalConfiguration)
        assertThat(viewModel.languagePreference.getOrAwaitValue()).isEqualTo(LANGUAGE)
    }

    @Test
    fun `update language should update the device configuration`() = runTest {
        val updateConfigFn = slot<suspend (DeviceConfiguration) -> DeviceConfiguration>()
        coEvery { configManager.updateDeviceConfiguration(capture(updateConfigFn)) } returns Unit

        val updatedLanguage = "en"
        viewModel.updateLanguagePreference(updatedLanguage)

        val updatedConfig = updateConfigFn.captured(DeviceConfiguration("", listOf(), listOf()))
        // Comparing string representation as when executing the lambda captured in the mock it will
        // not return an ArrayList but a LinkedHashMap.
        assertThat(updatedConfig.language).isEqualTo(updatedLanguage)
    }
}
