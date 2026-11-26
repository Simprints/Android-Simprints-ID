package com.simprints.feature.dashboard.settings

import app.cash.turbine.test
import com.google.common.truth.Truth.*
import com.simprints.core.domain.common.Modality
import com.simprints.feature.dashboard.tools.ViewModelScopeRule
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.ConfigSyncCache
import com.simprints.infra.config.store.models.DeviceConfiguration
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.SettingsPasswordConfig
import com.simprints.infra.events.device.DeviceEventTracker
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {
    @get:Rule(order = 0)
    val testCoroutineRule = TestCoroutineRule()

    // Inside TestCoroutineRule: scopes are cancelled while the test Main dispatcher is still set
    @get:Rule(order = 1)
    val viewModels = ViewModelScopeRule()

    private val generalConfiguration = GeneralConfiguration(
        modalities = listOf(Modality.FINGERPRINT),
        matchingModalities = listOf(Modality.FINGERPRINT),
        languageOptions = listOf("en", "fr"),
        defaultLanguage = "fr",
        collectLocation = true,
        duplicateBiometricEnrolmentCheck = true,
        settingsPassword = SettingsPasswordConfig.Locked(PASSWORD),
    )

    private val faceConfiguration = FaceConfiguration(
        allowedSDKs = listOf(),
        isAutoCapture = true,
        rankOne = null,
        simFace = null,
    )

    private val configRepository = mockk<ConfigRepository>(relaxed = true)
    private val syncOrchestrator = mockk<SyncOrchestrator>(relaxed = true)
    private val configSyncCache = mockk<ConfigSyncCache>()
    private val deviceEventTracker = mockk<DeviceEventTracker>(relaxed = true)

    private val refreshEmissions = MutableSharedFlow<Unit>()
    private var cachedLastUpdated = LAST_UPDATED

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        givenProjectConfiguration(generalConfiguration, faceConfiguration)
        coEvery { configRepository.getDeviceConfiguration() } returns DeviceConfiguration(LANGUAGE, listOf(), "")
        coEvery { configSyncCache.sinceLastUpdateTime() } answers { cachedLastUpdated }
        every { syncOrchestrator.refreshConfiguration() } returns refreshEmissions

        // Created after TestCoroutineRule has set the test Main dispatcher: the init block sends Load.
        viewModel = createViewModel()
    }

    @Test
    fun `init loads the settings state from the configuration`() = runTest {
        assertThat(viewModel.uiState.value).isEqualTo(
            SettingsUiState(
                isFingerprintSettingsVisible = true,
                isFaceAutoCaptureVisible = true,
                languageCode = LANGUAGE,
                languageOptions = listOf("en", "fr"),
                passwordConfig = SettingsPasswordConfig.Locked(PASSWORD),
                configLastUpdated = LAST_UPDATED,
                isConfigUpdateScheduled = false,
            ),
        )
        viewModel.uiEffect.test { expectNoEvents() }
        verify(exactly = 0) { syncOrchestrator.refreshConfiguration() }
    }

    @Test
    fun `init without a face configuration or fingerprint hides face auto capture and fingerprint settings`() = runTest {
        givenProjectConfiguration(generalConfiguration.copy(modalities = listOf(Modality.FACE)), face = null)

        val viewModel = createViewModel()

        assertThat(viewModel.uiState.value.isFaceAutoCaptureVisible).isFalse()
        assertThat(viewModel.uiState.value.isFingerprintSettingsVisible).isFalse()
    }

    @Test
    fun `init with a face configuration without auto capture hides face auto capture`() = runTest {
        givenProjectConfiguration(generalConfiguration, faceConfiguration.copy(isAutoCapture = false))

        val viewModel = createViewModel()

        assertThat(viewModel.uiState.value.isFaceAutoCaptureVisible).isFalse()
        assertThat(viewModel.uiState.value.isFingerprintSettingsVisible).isTrue()
    }

    @Test
    fun `language click sent before the load finishes waits for it and the password check then applies`() = runTest {
        val configuration = CompletableDeferred<ProjectConfiguration>()
        coEvery { configRepository.getProjectConfiguration() } coAnswers { configuration.await() }
        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.onAction(SettingsUiAction.LanguageClicked)
            expectNoEvents()
            assertThat(viewModel.uiState.value).isEqualTo(SettingsUiState())

            configuration.complete(projectConfiguration(generalConfiguration, faceConfiguration))

            assertThat(awaitItem()).isEqualTo(SettingsUiEffect.ShowPasswordDialog(PASSWORD, SettingsGatedAction.LANGUAGE))
            expectNoEvents()
        }
    }

    @Test
    fun `language selected persists the language with a local change event, updates the state and asks for a relaunch`() = runTest {
        val updateConfigFn = slot<suspend (DeviceConfiguration) -> DeviceConfiguration>()
        coEvery { configRepository.updateDeviceConfiguration(capture(updateConfigFn)) } returns Unit

        viewModel.onAction(SettingsUiAction.LanguageSelected("en"))

        val updatedConfig = updateConfigFn.captured(DeviceConfiguration("", listOf(), ""))
        assertThat(updatedConfig.language).isEqualTo("en")
        assertThat(viewModel.uiState.value.languageCode).isEqualTo("en")
        verify(exactly = 1) {
            deviceEventTracker.trackDeviceConfigurationUpdatedEvent(withArg { assertThat(it.language).isEqualTo("en") }, true)
        }
        viewModel.uiEffect.test {
            assertThat(awaitItem()).isEqualTo(SettingsUiEffect.RelaunchApp)
            expectNoEvents()
        }
    }

    // KI-3 fix
    @Test
    fun `relaunch is requested only after the language is saved`() = runTest {
        val release = CompletableDeferred<Unit>()
        coEvery { configRepository.updateDeviceConfiguration(any()) } coAnswers { release.await() }

        viewModel.uiEffect.test {
            viewModel.onAction(SettingsUiAction.LanguageSelected("en"))

            coVerify(exactly = 1) { configRepository.updateDeviceConfiguration(any()) }
            assertThat(viewModel.uiState.value.languageCode).isEqualTo(LANGUAGE)
            expectNoEvents()

            release.complete(Unit)

            assertThat(awaitItem()).isEqualTo(SettingsUiEffect.RelaunchApp)
            assertThat(viewModel.uiState.value.languageCode).isEqualTo("en")
            expectNoEvents()
        }
    }

    // KI-3 fix: a failed save does not relaunch; the exception still escapes, as it did before.
    @Test
    fun `a failed language save does not ask for a relaunch`() {
        coEvery { configRepository.updateDeviceConfiguration(any()) } throws IllegalStateException(SAVE_FAILURE)

        val escaped = assertThrows(IllegalStateException::class.java) {
            // runTest reports exceptions that escaped other coroutines (here viewModelScope) once its body is done.
            runTest { viewModel.onAction(SettingsUiAction.LanguageSelected("en")) }
        }

        assertThat(escaped).hasMessageThat().isEqualTo(SAVE_FAILURE)
        assertThat(viewModel.uiState.value.languageCode).isEqualTo(LANGUAGE)
        // Effects are buffered until collected, so a RelaunchApp sent above would still be waiting here.
        runTest { viewModel.uiEffect.test { expectNoEvents() } }
    }

    // ST-6
    @Test
    fun `language change persists and tracks the event before the language output changes`() = runTest {
        val release = CompletableDeferred<Unit>()
        val languageSeenByTracker = mutableListOf<String?>()
        coEvery { configRepository.updateDeviceConfiguration(any()) } coAnswers {
            release.await()
            firstArg<suspend (DeviceConfiguration) -> DeviceConfiguration>().invoke(DeviceConfiguration("", listOf(), ""))
        }
        every { deviceEventTracker.trackDeviceConfigurationUpdatedEvent(any(), any()) } answers {
            languageSeenByTracker += viewModel.uiState.value.languageCode
        }

        viewModel.onAction(SettingsUiAction.LanguageSelected("en"))

        assertThat(viewModel.uiState.value.languageCode).isEqualTo(LANGUAGE)
        verify(exactly = 0) { deviceEventTracker.trackDeviceConfigurationUpdatedEvent(any(), any()) }

        release.complete(Unit)

        assertThat(viewModel.uiState.value.languageCode).isEqualTo("en")
        assertThat(languageSeenByTracker).containsExactly(LANGUAGE)
        verify(exactly = 1) {
            deviceEventTracker.trackDeviceConfigurationUpdatedEvent(withArg { assertThat(it.language).isEqualTo("en") }, true)
        }
        verify(exactly = 1) { deviceEventTracker.trackDeviceConfigurationUpdatedEvent(any(), any()) }
    }

    // N-4
    @Test
    fun `language selection is not delayed by a configuration refresh in progress`() = runTest {
        every { syncOrchestrator.refreshConfiguration() } returns flow { awaitCancellation() }
        coEvery { configRepository.updateDeviceConfiguration(any()) } returns Unit

        viewModel.onAction(SettingsUiAction.UpdateConfigClicked)
        viewModel.onAction(SettingsUiAction.LanguageSelected("en"))

        coVerify(exactly = 1) { configRepository.updateDeviceConfiguration(any()) }
        assertThat(viewModel.uiState.value.languageCode).isEqualTo("en")
        assertThat(viewModel.uiState.value.isConfigUpdateScheduled).isTrue()
    }

    // N-8
    @Test
    fun `language click without a password opens the language dialog with the options and the current language`() = runTest {
        givenProjectConfiguration(generalConfiguration.copy(settingsPassword = SettingsPasswordConfig.NotSet), faceConfiguration)
        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.onAction(SettingsUiAction.LanguageClicked)

            viewModel.uiState.value.let { state ->
                assertThat(state.isLanguageDialogVisible).isTrue()
                assertThat(state.languageOptions).isEqualTo(listOf("en", "fr"))
                assertThat(state.languageCode).isEqualTo(LANGUAGE)
            }
            expectNoEvents()
        }
    }

    // N-8
    @Test
    fun `language click with an unlocked password config opens the language dialog`() = runTest {
        givenProjectConfiguration(generalConfiguration.copy(settingsPassword = SettingsPasswordConfig.Unlocked), faceConfiguration)
        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.onAction(SettingsUiAction.LanguageClicked)

            assertThat(viewModel.uiState.value.isLanguageDialogVisible).isTrue()
            expectNoEvents()
        }
    }

    // N-8
    @Test
    fun `language click with a password asks for it and does not unlock`() = runTest {
        viewModel.uiEffect.test {
            viewModel.onAction(SettingsUiAction.LanguageClicked)

            assertThat(awaitItem()).isEqualTo(SettingsUiEffect.ShowPasswordDialog(PASSWORD, SettingsGatedAction.LANGUAGE))
            expectNoEvents()
        }
        assertThat(viewModel.uiState.value.passwordConfig).isEqualTo(SettingsPasswordConfig.Locked(PASSWORD))
        assertThat(viewModel.uiState.value.isLanguageDialogVisible).isFalse()
    }

    @Test
    fun `password unlocked marks the settings unlocked and opens the language dialog`() = runTest {
        assertThat(viewModel.uiState.value.passwordConfig).isEqualTo(SettingsPasswordConfig.Locked(PASSWORD))

        viewModel.uiEffect.test {
            viewModel.onAction(SettingsUiAction.PasswordUnlocked(SettingsGatedAction.LANGUAGE))

            assertThat(viewModel.uiState.value.isLanguageDialogVisible).isTrue()
            expectNoEvents()
        }
        assertThat(viewModel.uiState.value.passwordConfig).isEqualTo(SettingsPasswordConfig.Unlocked)
    }

    // ST-7
    @Test
    fun `unlock persists so a later language click opens the language dialog without a password`() = runTest {
        viewModel.uiEffect.test {
            viewModel.onAction(SettingsUiAction.LanguageClicked)
            assertThat(awaitItem()).isInstanceOf(SettingsUiEffect.ShowPasswordDialog::class.java)
            viewModel.onAction(SettingsUiAction.PasswordUnlocked(SettingsGatedAction.LANGUAGE))
            assertThat(viewModel.uiState.value.isLanguageDialogVisible).isTrue()
            viewModel.onAction(SettingsUiAction.LanguageDialogDismissed)
            assertThat(viewModel.uiState.value.isLanguageDialogVisible).isFalse()

            viewModel.onAction(SettingsUiAction.LanguageClicked)

            assertThat(viewModel.uiState.value.isLanguageDialogVisible).isTrue()
            expectNoEvents()
        }
    }

    @Test
    fun `dismissing the language dialog hides it without saving a language`() = runTest {
        viewModel.onAction(SettingsUiAction.PasswordUnlocked(SettingsGatedAction.LANGUAGE))

        viewModel.uiEffect.test {
            viewModel.onAction(SettingsUiAction.LanguageDialogDismissed)

            assertThat(viewModel.uiState.value.isLanguageDialogVisible).isFalse()
            assertThat(viewModel.uiState.value.languageCode).isEqualTo(LANGUAGE)
            expectNoEvents()
        }
        coVerify(exactly = 0) { configRepository.updateDeviceConfiguration(any()) }
    }

    // KI-3: the dialog closes before the save, so a second pick while saving cannot reach the ViewModel through the dialog.
    @Test
    fun `language selection hides the language dialog before the save completes`() = runTest {
        val release = CompletableDeferred<Unit>()
        coEvery { configRepository.updateDeviceConfiguration(any()) } coAnswers { release.await() }
        viewModel.onAction(SettingsUiAction.PasswordUnlocked(SettingsGatedAction.LANGUAGE))
        assertThat(viewModel.uiState.value.isLanguageDialogVisible).isTrue()

        viewModel.onAction(SettingsUiAction.LanguageSelected("en"))

        assertThat(viewModel.uiState.value.isLanguageDialogVisible).isFalse()
        assertThat(viewModel.uiState.value.languageCode).isEqualTo(LANGUAGE)
        release.complete(Unit)
        assertThat(viewModel.uiState.value.languageCode).isEqualTo("en")
    }

    // ST-9 (KI-2 fix)
    @Test
    fun `screen started re-reads the last updated text`() = runTest {
        assertThat(viewModel.uiState.value.configLastUpdated).isEqualTo(LAST_UPDATED)
        cachedLastUpdated = LATER_LAST_UPDATED

        viewModel.onAction(SettingsUiAction.ScreenStarted)

        assertThat(viewModel.uiState.value.configLastUpdated).isEqualTo(LATER_LAST_UPDATED)
        coVerify(exactly = 2) { configSyncCache.sinceLastUpdateTime() }
        viewModel.uiEffect.test { expectNoEvents() }
    }

    @Test
    fun `update config click marks the update scheduled and starts the refresh`() = runTest {
        viewModel.onAction(SettingsUiAction.UpdateConfigClicked)

        assertThat(viewModel.uiState.value.isConfigUpdateScheduled).isTrue()
        assertThat(viewModel.uiState.value.updateConfigSummary).isEqualTo(SettingsUiState.UpdateConfigSummary.Scheduled)
        verify(exactly = 1) { syncOrchestrator.refreshConfiguration() }
        viewModel.uiEffect.test { expectNoEvents() }
    }

    // KI-2 fix: "scheduled" stays until a refresh emission, as the old Fragment-owned summary did.
    @Test
    fun `screen started while an update is scheduled keeps it scheduled`() = runTest {
        viewModel.onAction(SettingsUiAction.UpdateConfigClicked)
        cachedLastUpdated = LATER_LAST_UPDATED

        viewModel.onAction(SettingsUiAction.ScreenStarted)

        assertThat(viewModel.uiState.value.configLastUpdated).isEqualTo(LATER_LAST_UPDATED)
        assertThat(viewModel.uiState.value.isConfigUpdateScheduled).isTrue()
        assertThat(viewModel.uiState.value.updateConfigSummary).isEqualTo(SettingsUiState.UpdateConfigSummary.Scheduled)
        viewModel.uiEffect.test { expectNoEvents() }
    }

    @Test
    fun `each refresh emission sends the toast and updates the last updated text`() = runTest {
        viewModel.onAction(SettingsUiAction.UpdateConfigClicked)

        viewModel.uiEffect.test {
            // An early emission (WorkManager before the new work is stored) does not end the refresh
            cachedLastUpdated = OTHER_LAST_UPDATED
            refreshEmissions.emit(Unit)
            assertThat(awaitItem()).isEqualTo(SettingsUiEffect.ConfigUpdated)
            assertThat(viewModel.uiState.value.configLastUpdated).isEqualTo(OTHER_LAST_UPDATED)
            assertThat(viewModel.uiState.value.isConfigUpdateScheduled).isFalse()

            // The real completion still updates the last updated text
            cachedLastUpdated = LATER_LAST_UPDATED
            refreshEmissions.emit(Unit)
            assertThat(awaitItem()).isEqualTo(SettingsUiEffect.ConfigUpdated)
            assertThat(viewModel.uiState.value.configLastUpdated).isEqualTo(LATER_LAST_UPDATED)
            expectNoEvents()
        }
    }

    @Test
    fun `repeated update config clicks keep a single refresh collector so one emission sends one toast`() = runTest {
        viewModel.onAction(SettingsUiAction.UpdateConfigClicked)
        viewModel.onAction(SettingsUiAction.UpdateConfigClicked)
        viewModel.onAction(SettingsUiAction.UpdateConfigClicked)

        // Every click still starts the configuration sync
        verify(exactly = 3) { syncOrchestrator.refreshConfiguration() }
        assertThat(refreshEmissions.subscriptionCount.value).isEqualTo(1)
        assertThat(viewModel.uiState.value.isConfigUpdateScheduled).isTrue()

        viewModel.uiEffect.test {
            refreshEmissions.emit(Unit)
            assertThat(awaitItem()).isEqualTo(SettingsUiEffect.ConfigUpdated)
            expectNoEvents()
        }
    }

    @Test
    fun `update config click after a refresh emission starts a new refresh with a single collector`() = runTest {
        viewModel.uiEffect.test {
            viewModel.onAction(SettingsUiAction.UpdateConfigClicked)
            refreshEmissions.emit(Unit)
            assertThat(awaitItem()).isEqualTo(SettingsUiEffect.ConfigUpdated)

            viewModel.onAction(SettingsUiAction.UpdateConfigClicked)

            verify(exactly = 2) { syncOrchestrator.refreshConfiguration() }
            assertThat(refreshEmissions.subscriptionCount.value).isEqualTo(1)
            assertThat(viewModel.uiState.value.isConfigUpdateScheduled).isTrue()
            refreshEmissions.emit(Unit)
            assertThat(awaitItem()).isEqualTo(SettingsUiEffect.ConfigUpdated)
            assertThat(viewModel.uiState.value.isConfigUpdateScheduled).isFalse()
            expectNoEvents()
        }
    }

    // ST-9 (KI-2 fix): the toast comes first, then the state update.
    @Test
    fun `refresh emission sends the toast before the last updated value changes`() = runTest {
        val readAfterRefresh = CompletableDeferred<String>()
        viewModel.onAction(SettingsUiAction.UpdateConfigClicked)
        coEvery { configSyncCache.sinceLastUpdateTime() } coAnswers { readAfterRefresh.await() }

        viewModel.uiEffect.test {
            refreshEmissions.emit(Unit)

            assertThat(awaitItem()).isEqualTo(SettingsUiEffect.ConfigUpdated)
            assertThat(viewModel.uiState.value.configLastUpdated).isEqualTo(LAST_UPDATED)
            assertThat(viewModel.uiState.value.isConfigUpdateScheduled).isTrue()

            readAfterRefresh.complete(OTHER_LAST_UPDATED)

            assertThat(viewModel.uiState.value.configLastUpdated).isEqualTo(OTHER_LAST_UPDATED)
            assertThat(viewModel.uiState.value.isConfigUpdateScheduled).isFalse()
            expectNoEvents()
        }
    }

    // ST-10
    @Test
    fun `a refresh that emits nothing sends no toast and keeps the last updated value`() = runTest {
        every { syncOrchestrator.refreshConfiguration() } returns emptyFlow()
        cachedLastUpdated = OTHER_LAST_UPDATED

        viewModel.uiEffect.test {
            viewModel.onAction(SettingsUiAction.UpdateConfigClicked)

            verify(exactly = 1) { syncOrchestrator.refreshConfiguration() }
            assertThat(viewModel.uiState.value.isConfigUpdateScheduled).isTrue()
            assertThat(viewModel.uiState.value.configLastUpdated).isEqualTo(LAST_UPDATED)
            coVerify(exactly = 1) { configSyncCache.sinceLastUpdateTime() }
            expectNoEvents()
        }
    }

    private fun createViewModel() = viewModels.track(
        SettingsViewModel(
            configRepository = configRepository,
            syncOrchestrator = syncOrchestrator,
            configSyncCache = configSyncCache,
            deviceEventTracker = deviceEventTracker,
        ),
    )

    private fun givenProjectConfiguration(
        general: GeneralConfiguration,
        face: FaceConfiguration?,
    ) {
        coEvery { configRepository.getProjectConfiguration() } returns projectConfiguration(general, face)
    }

    private fun projectConfiguration(
        general: GeneralConfiguration,
        face: FaceConfiguration?,
    ) = mockk<ProjectConfiguration> {
        every { this@mockk.general } returns general
        every { this@mockk.face } returns face
    }

    companion object {
        private const val LANGUAGE = "fr"
        private const val PASSWORD = "1234"
        private const val LAST_UPDATED = "5 minutes ago"
        private const val OTHER_LAST_UPDATED = "0 minutes ago"
        private const val LATER_LAST_UPDATED = "7 minutes ago"
        private const val SAVE_FAILURE = "save failed"
    }
}
