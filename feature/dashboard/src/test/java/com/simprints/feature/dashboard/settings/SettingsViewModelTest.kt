package com.simprints.feature.dashboard.settings

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.jraska.livedata.test
import com.simprints.infra.config.store.models.DeviceConfiguration
import com.simprints.infra.config.store.models.ExperimentalProjectConfiguration
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.SettingsPasswordConfig
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.config.sync.ConfigSyncCache
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
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

    @MockK
    private lateinit var configSyncCache: ConfigSyncCache

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        coEvery { configManager.getProjectConfiguration().general } returns generalConfiguration
        coEvery { configManager.getDeviceConfiguration().language } returns LANGUAGE

        coEvery { configSyncCache.sinceLastUpdateTime() } returnsMany listOf(
            LAST_UPDATED,
            OTHER_LAST_UPDATED,
        )

        viewModel = SettingsViewModel(configManager, syncOrchestrator, configSyncCache)
    }

    @Test
    fun `experimentalConfiguration live data should follow the project experimental configuration`() = runTest {
        val experimentalConfig1 = mapOf("key1" to "value1")
        val experimentalConfig2 = mapOf("key2" to "value2")

        coEvery { configManager.watchProjectConfiguration() } returns flowOf(
            mockk<ProjectConfiguration>(relaxed = true) {
                every { custom } returns experimentalConfig1
            },
            mockk<ProjectConfiguration>(relaxed = true) {
                every { custom } returns experimentalConfig2
            },
        )
        viewModel = SettingsViewModel(configManager, syncOrchestrator, configSyncCache)

        assertThat(viewModel.experimentalConfiguration.test().valueHistory())
            .isEqualTo(
                listOf(
                    ExperimentalProjectConfiguration(experimentalConfig1),
                    ExperimentalProjectConfiguration(experimentalConfig2),
                )
            )
    }

    @Test
    fun `should initialize the live data correctly`() {
        assertThat(viewModel.generalConfiguration.value).isEqualTo(generalConfiguration)
        assertThat(viewModel.languagePreference.value).isEqualTo(LANGUAGE)
        assertThat(viewModel.settingsLocked.value).isEqualTo(SettingsPasswordConfig.Locked("1234"))
        assertThat(viewModel.sinceConfigLastUpdated.value?.peekContent()).isEqualTo(LAST_UPDATED)
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
    fun `trigger config refresh when called`() {
        coEvery { syncOrchestrator.refreshConfiguration() } returns flowOf(Unit)
        val updateTest = viewModel.sinceConfigLastUpdated.test() // to capture full update history

        viewModel.scheduleConfigUpdate()

        verify { syncOrchestrator.refreshConfiguration() }
        viewModel.configUpdated.test().assertHasValue()
        updateTest
            .valueHistory()
            .map { it.peekContent() }
            .let { assertThat(it).containsExactly(LAST_UPDATED, OTHER_LAST_UPDATED) }
    }

    companion object {
        private const val LANGUAGE = "fr"
        private const val LAST_UPDATED = "5 minutes ago"
        private const val OTHER_LAST_UPDATED = "0 minutes ago"
    }
}
