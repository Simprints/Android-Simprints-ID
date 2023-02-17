package com.simprints.feature.dashboard.settings

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.DeviceConfiguration
import com.simprints.infra.config.domain.models.GeneralConfiguration
import com.simprints.infra.config.domain.models.SettingsPasswordConfig
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    companion object {
        private const val LANGUAGE = "fr"
    }

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val generalConfiguration = GeneralConfiguration(
        modalities = listOf(GeneralConfiguration.Modality.FINGERPRINT),
        languageOptions = listOf("en", "fr"),
        defaultLanguage = "fr",
        collectLocation = true,
        duplicateBiometricEnrolmentCheck = true,
        settingsPassword = SettingsPasswordConfig.Locked("1234"),
    )
    private val configManager = mockk<ConfigManager> {
        coEvery { getProjectConfiguration() } returns mockk {
            every { general } returns generalConfiguration
        }
        coEvery { getDeviceConfiguration() } returns mockk {
            every { language } returns LANGUAGE
        }
    }

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        viewModel = SettingsViewModel(configManager)
    }

    @Test
    fun `should initialize the live data correctly`() {
        assertThat(viewModel.generalConfiguration.value).isEqualTo(generalConfiguration)
        assertThat(viewModel.languagePreference.value).isEqualTo(LANGUAGE)
        assertThat(viewModel.settingsLocked.value).isEqualTo(SettingsPasswordConfig.Locked("1234"))
    }

    @Test
    fun `updateLanguagePreference should update the language`() = runTest {
        val updatedLanguage = "en"
        val updateConfigFn = slot<suspend (DeviceConfiguration) -> DeviceConfiguration>()
        coEvery { configManager.updateDeviceConfiguration(capture(updateConfigFn)) } returns Unit

        viewModel.updateLanguagePreference(updatedLanguage)

        val updatedConfig = updateConfigFn.captured(DeviceConfiguration("", listOf(), listOf(), ""))

        assertThat(updatedConfig.language).isEqualTo(updatedLanguage)
        assertThat(viewModel.languagePreference.value).isEqualTo(updatedLanguage)
    }

    @Test
    fun `mark settings as unlocked when called`() {
        assertThat(viewModel.settingsLocked.value).isEqualTo(SettingsPasswordConfig.Locked("1234"))

        viewModel.unlockSettings()

        assertThat(viewModel.settingsLocked.value).isEqualTo(SettingsPasswordConfig.Unlocked)
    }
}
