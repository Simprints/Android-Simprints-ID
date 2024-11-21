package com.simprints.feature.dashboard.settings

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.models.DeviceConfiguration
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.config.store.models.SettingsPasswordConfig
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val generalConfiguration = GeneralConfiguration(
        modalities = listOf(GeneralConfiguration.Modality.FINGERPRINT),
        matchingModalities = listOf(GeneralConfiguration.Modality.FINGERPRINT),
        languageOptions = listOf("en", "fr"),
        defaultLanguage = "fr",
        collectLocation = true,
        duplicateBiometricEnrolmentCheck = true,
        settingsPassword = SettingsPasswordConfig.Locked("1234"),
    )

    @MockK
    private lateinit var configManager: ConfigManager

    @MockK
    private lateinit var syncOrchestrator: SyncOrchestrator

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        coEvery { configManager.getProjectConfiguration().general } returns generalConfiguration
        coEvery { configManager.getDeviceConfiguration().language } returns LANGUAGE

        viewModel = SettingsViewModel(configManager, syncOrchestrator)
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

        val updatedConfig = updateConfigFn.captured(DeviceConfiguration("", listOf(), ""))

        assertThat(updatedConfig.language).isEqualTo(updatedLanguage)
        assertThat(viewModel.languagePreference.value).isEqualTo(updatedLanguage)
    }

    @Test
    fun `mark settings as unlocked when called`() {
        assertThat(viewModel.settingsLocked.value).isEqualTo(SettingsPasswordConfig.Locked("1234"))

        viewModel.unlockSettings()

        assertThat(viewModel.settingsLocked.value).isEqualTo(SettingsPasswordConfig.Unlocked)
    }

    @Test
    fun `trigger device sync when called`() {
        viewModel.scheduleConfigUpdate()

        verify { syncOrchestrator.startProjectSync() }
        verify { syncOrchestrator.startDeviceSync() }
    }

    companion object {

        private const val LANGUAGE = "fr"
    }

}
