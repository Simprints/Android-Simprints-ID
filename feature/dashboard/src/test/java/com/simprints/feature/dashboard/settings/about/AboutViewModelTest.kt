package com.simprints.feature.dashboard.settings.about

import app.cash.turbine.test
import com.google.common.truth.Truth.*
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.feature.dashboard.tools.ViewModelScopeRule
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.DownSynchronizationConfiguration
import com.simprints.infra.config.store.models.DownSynchronizationConfiguration.SimprintsDownSynchronizationConfiguration
import com.simprints.infra.config.store.models.IdentificationConfiguration
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.SettingsPasswordConfig
import com.simprints.infra.config.store.models.UpSynchronizationConfiguration.UpSynchronizationKind
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.infra.recent.user.activity.domain.RecentUserActivity
import com.simprints.infra.sync.OneTime
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.infra.sync.devicestate.DeviceStateDataTracker
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AboutViewModelTest {
    @get:Rule(order = 0)
    val testCoroutineRule = TestCoroutineRule()

    // Inside TestCoroutineRule: scopes are cancelled while the test Main dispatcher is still set
    @get:Rule(order = 1)
    val viewModels = ViewModelScopeRule()

    private val recentUserActivity = RecentUserActivity(
        lastScannerVersion = SCANNER_VERSION,
        lastScannerUsed = "scanner",
        lastUserUsed = "user".asTokenizableEncrypted(),
        enrolmentsToday = 10,
        identificationsToday = 20,
        verificationsToday = 30,
        lastActivityTime = 10000,
    )

    private val configRepository = mockk<ConfigRepository>()
    private val deviceStateDataTracker = mockk<DeviceStateDataTracker>()
    private val syncOrchestrator = mockk<SyncOrchestrator>(relaxed = true)
    private val recentUserActivityManager = mockk<RecentUserActivityManager>()

    private lateinit var viewModel: AboutViewModel

    @Before
    fun setUp() {
        givenProjectConfiguration()
        givenPendingData(events = false, samples = 0)
        coEvery { recentUserActivityManager.getRecentUserActivity() } returns recentUserActivity

        // Created after TestCoroutineRule has set the test Main dispatcher: the init block sends Load.
        viewModel = createViewModel()
    }

    @Test
    fun `init loads the about state from the configuration`() = runTest {
        assertThat(viewModel.uiState.value).isEqualTo(LOADED_STATE)
        viewModel.uiEffect.test { expectNoEvents() }
        verify(exactly = 0) { syncOrchestrator.execute(any<OneTime>()) }
    }

    @Test
    fun `init without the fingerprint modality hides the scanner version`() = runTest {
        givenProjectConfiguration(modalities = listOf(Modality.FACE))

        val viewModel = createViewModel()

        assertThat(viewModel.uiState.value.isScannerVersionVisible).isFalse()
        assertThat(viewModel.uiState.value.lastScannerVersion).isEqualTo(SCANNER_VERSION)
    }

    // AB-7
    @Test
    fun `init without a simprints down sync configuration shows commcare as the sync type`() = runTest {
        givenProjectConfiguration(partitionType = null)

        val viewModel = createViewModel()

        assertThat(viewModel.uiState.value.syncAndSearchConfig).isEqualTo(SyncAndSearchConfig("CommCare", POOL_TYPE.name))
    }

    @Test
    fun `logout click sent before the load finishes waits for it and the password check then applies`() = runTest {
        val configuration = CompletableDeferred<ProjectConfiguration>()
        coEvery { configRepository.getProjectConfiguration() } coAnswers { configuration.await() }
        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.onAction(AboutUiAction.LogoutClicked)
            expectNoEvents()
            assertThat(viewModel.uiState.value).isEqualTo(AboutUiState())

            configuration.complete(projectConfiguration())

            assertThat(awaitItem()).isEqualTo(AboutUiEffect.ShowPasswordDialog(PASSWORD, AboutGatedAction.LOGOUT))
            expectNoEvents()
        }
    }

    // N-7
    @Test
    fun `logout click with a password asks for it and does not unlock or log out`() = runTest {
        viewModel.uiEffect.test {
            viewModel.onAction(AboutUiAction.LogoutClicked)

            assertThat(awaitItem()).isEqualTo(AboutUiEffect.ShowPasswordDialog(PASSWORD, AboutGatedAction.LOGOUT))
            expectNoEvents()
        }
        assertThat(viewModel.uiState.value).isEqualTo(LOADED_STATE)
        verify(exactly = 0) { syncOrchestrator.execute(any<OneTime>()) }
    }

    // N-7
    @Test
    fun `logout click without a password shows the confirmation and does not log out`() = runTest {
        givenProjectConfiguration(password = SettingsPasswordConfig.NotSet)
        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.onAction(AboutUiAction.LogoutClicked)

            assertThat(viewModel.uiState.value.isLogoutConfirmationVisible).isTrue()
            expectNoEvents()
        }
        verify(exactly = 0) { syncOrchestrator.execute(any<OneTime>()) }
    }

    // N-7
    @Test
    fun `logout click with an unlocked password config shows the confirmation`() = runTest {
        givenProjectConfiguration(password = SettingsPasswordConfig.Unlocked)
        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.onAction(AboutUiAction.LogoutClicked)

            assertThat(viewModel.uiState.value.isLogoutConfirmationVisible).isTrue()
            expectNoEvents()
        }
        verify(exactly = 0) { syncOrchestrator.execute(any<OneTime>()) }
    }

    // PC-105
    @Test
    fun `two quick logout clicks keep a single confirmation visible`() = runTest {
        givenProjectConfiguration(password = SettingsPasswordConfig.NotSet)
        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.onAction(AboutUiAction.LogoutClicked)
            viewModel.onAction(AboutUiAction.LogoutClicked)

            assertThat(viewModel.uiState.value.isLogoutConfirmationVisible).isTrue()
            expectNoEvents()
        }
    }

    @Test
    fun `dismissing the logout confirmation hides it without logging out`() = runTest {
        givenProjectConfiguration(password = SettingsPasswordConfig.NotSet)
        val viewModel = createViewModel()
        viewModel.onAction(AboutUiAction.LogoutClicked)

        viewModel.uiEffect.test {
            viewModel.onAction(AboutUiAction.LogoutConfirmationDismissed)

            assertThat(viewModel.uiState.value.isLogoutConfirmationVisible).isFalse()
            expectNoEvents()
        }
        verify(exactly = 0) { syncOrchestrator.execute(any<OneTime>()) }
    }

    @Test
    fun `confirming the logout hides the confirmation and logs out`() = runTest {
        givenProjectConfiguration(password = SettingsPasswordConfig.NotSet, upSyncKind = UpSynchronizationKind.NONE)
        val viewModel = createViewModel()
        viewModel.onAction(AboutUiAction.LogoutClicked)
        assertThat(viewModel.uiState.value.isLogoutConfirmationVisible).isTrue()

        viewModel.onAction(AboutUiAction.LogoutConfirmed)

        assertThat(viewModel.uiState.value.isLogoutConfirmationVisible).isFalse()
        verify(exactly = 1) { syncOrchestrator.execute(OneTime.Logout.start()) }
    }

    // AB-1
    @Test
    fun `logout confirmed when data cannot be synced executes exactly the logout start command`() = runTest {
        givenProjectConfiguration(upSyncKind = UpSynchronizationKind.NONE)
        givenPendingData(events = true, samples = 1)
        val viewModel = createViewModel()

        viewModel.onAction(AboutUiAction.LogoutConfirmed)

        verify(exactly = 1) { syncOrchestrator.execute(OneTime.Logout.start(isProjectEnded = false)) }
        verify(exactly = 1) { syncOrchestrator.execute(any<OneTime>()) }
    }

    @Test
    fun `logout confirmed when data cannot be synced sends no effect and reads no pending data`() = runTest {
        givenProjectConfiguration(upSyncKind = UpSynchronizationKind.NONE)
        givenPendingData(events = true, samples = 1)
        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.onAction(AboutUiAction.LogoutConfirmed)

            verify(exactly = 1) { syncOrchestrator.execute(any<OneTime>()) }
            expectNoEvents()
        }
        coVerify(exactly = 0) { deviceStateDataTracker.hasPendingEvents() }
        coVerify(exactly = 0) { deviceStateDataTracker.getCurrentDeviceDataState() }
    }

    // AB-1
    @Test
    fun `logout confirmed with nothing pending executes exactly the logout start command`() = runTest {
        viewModel.onAction(AboutUiAction.LogoutConfirmed)

        verify(exactly = 1) { syncOrchestrator.execute(OneTime.Logout.start(isProjectEnded = false)) }
        verify(exactly = 1) { syncOrchestrator.execute(any<OneTime>()) }
    }

    // AB-3 (ViewModel half): navigation to request login comes from the DashboardActivity logout broadcast.
    @Test
    fun `logout confirmed with nothing pending sends no effect after checking events and samples`() = runTest {
        viewModel.uiEffect.test {
            viewModel.onAction(AboutUiAction.LogoutConfirmed)

            verify(exactly = 1) { syncOrchestrator.execute(any<OneTime>()) }
            expectNoEvents()
        }
        coVerify(exactly = 1) { deviceStateDataTracker.hasPendingEvents() }
        coVerify(exactly = 1) { deviceStateDataTracker.getCurrentDeviceDataState() }
    }

    @Test
    fun `logout confirmed with pending events does not log out`() = runTest {
        givenPendingData(events = true, samples = 0)

        viewModel.onAction(AboutUiAction.LogoutConfirmed)

        coVerify(exactly = 1) { deviceStateDataTracker.hasPendingEvents() }
        verify(exactly = 0) { syncOrchestrator.execute(any<OneTime>()) }
    }

    @Test
    fun `logout confirmed with pending events navigates to logout sync without reading samples`() = runTest {
        givenPendingData(events = true, samples = 0)

        viewModel.uiEffect.test {
            viewModel.onAction(AboutUiAction.LogoutConfirmed)

            assertThat(awaitItem()).isEqualTo(AboutUiEffect.NavigateToLogoutSync)
            expectNoEvents()
        }
        coVerify(exactly = 0) { deviceStateDataTracker.getCurrentDeviceDataState() }
    }

    @Test
    fun `logout confirmed with only samples pending does not log out`() = runTest {
        givenPendingData(events = false, samples = 3)

        viewModel.onAction(AboutUiAction.LogoutConfirmed)

        coVerify(exactly = 1) { deviceStateDataTracker.getCurrentDeviceDataState() }
        verify(exactly = 0) { syncOrchestrator.execute(any<OneTime>()) }
    }

    @Test
    fun `logout confirmed with only samples pending navigates to logout sync`() = runTest {
        givenPendingData(events = false, samples = 3)

        viewModel.uiEffect.test {
            viewModel.onAction(AboutUiAction.LogoutConfirmed)

            assertThat(awaitItem()).isEqualTo(AboutUiEffect.NavigateToLogoutSync)
            expectNoEvents()
        }
    }

    @Test
    fun `each logout request reads the pending data and the configuration freshly`() = runTest {
        viewModel.uiEffect.test {
            viewModel.onAction(AboutUiAction.LogoutConfirmed)
            verify(exactly = 1) { syncOrchestrator.execute(OneTime.Logout.start(isProjectEnded = false)) }
            expectNoEvents()

            givenPendingData(events = true, samples = 0)
            viewModel.onAction(AboutUiAction.LogoutConfirmed)
            assertThat(awaitItem()).isEqualTo(AboutUiEffect.NavigateToLogoutSync)

            givenProjectConfiguration(upSyncKind = UpSynchronizationKind.NONE)
            viewModel.onAction(AboutUiAction.LogoutConfirmed)
            verify(exactly = 2) { syncOrchestrator.execute(OneTime.Logout.start(isProjectEnded = false)) }
            expectNoEvents()
        }
        coVerify(exactly = 2) { deviceStateDataTracker.hasPendingEvents() }
        // One read for the load, one per request.
        coVerify(exactly = 4) { configRepository.getProjectConfiguration() }
    }

    // AB-2
    @Test
    fun `a failing pending events check is treated as pending and does not log out`() = runTest {
        coEvery { deviceStateDataTracker.hasPendingEvents() } throws IllegalStateException("db closed")

        viewModel.uiEffect.test {
            viewModel.onAction(AboutUiAction.LogoutConfirmed)

            assertThat(awaitItem()).isEqualTo(AboutUiEffect.NavigateToLogoutSync)
            expectNoEvents()
        }
        verify(exactly = 0) { syncOrchestrator.execute(any<OneTime>()) }
    }

    // AB-2
    @Test
    fun `a cancelled pending events check sends no effect and does not log out`() = runTest {
        coEvery { deviceStateDataTracker.hasPendingEvents() } throws CancellationException("cancelled")

        viewModel.uiEffect.test {
            viewModel.onAction(AboutUiAction.LogoutConfirmed)

            coVerify(exactly = 1) { deviceStateDataTracker.hasPendingEvents() }
            expectNoEvents()
        }
        verify(exactly = 0) { syncOrchestrator.execute(any<OneTime>()) }
    }

    // N-6
    @Test
    fun `a cancelled pending events check does not stop later actions`() = runTest {
        coEvery { deviceStateDataTracker.hasPendingEvents() } throws CancellationException("cancelled") andThen false

        viewModel.uiEffect.test {
            viewModel.onAction(AboutUiAction.LogoutConfirmed)
            viewModel.onAction(AboutUiAction.LogoutClicked)
            viewModel.onAction(AboutUiAction.LogoutConfirmed)

            assertThat(awaitItem()).isEqualTo(AboutUiEffect.ShowPasswordDialog(PASSWORD, AboutGatedAction.LOGOUT))
            expectNoEvents()
        }
        coVerify(exactly = 2) { deviceStateDataTracker.hasPendingEvents() }
        verify(exactly = 1) { syncOrchestrator.execute(OneTime.Logout.start(isProjectEnded = false)) }
        verify(exactly = 1) { syncOrchestrator.execute(any<OneTime>()) }
    }

    // AB-5 (ViewModel half)
    @Test
    fun `password unlocked for logout marks the settings unlocked and runs the logout once`() = runTest {
        viewModel.uiEffect.test {
            viewModel.onAction(AboutUiAction.PasswordUnlocked(AboutGatedAction.LOGOUT))

            verify(exactly = 1) { syncOrchestrator.execute(OneTime.Logout.start(isProjectEnded = false)) }
            expectNoEvents()
        }
        assertThat(viewModel.uiState.value.passwordConfig).isEqualTo(SettingsPasswordConfig.Unlocked)
        verify(exactly = 1) { syncOrchestrator.execute(any<OneTime>()) }
    }

    // §4.8: unlock first, so the settings stay unlocked even if the pending data check never finishes.
    @Test
    fun `password unlocked for logout unlocks before the pending data check`() = runTest {
        val pending = CompletableDeferred<Boolean>()
        coEvery { deviceStateDataTracker.hasPendingEvents() } coAnswers { pending.await() }

        viewModel.uiEffect.test {
            viewModel.onAction(AboutUiAction.PasswordUnlocked(AboutGatedAction.LOGOUT))

            coVerify(exactly = 1) { deviceStateDataTracker.hasPendingEvents() }
            assertThat(viewModel.uiState.value.passwordConfig).isEqualTo(SettingsPasswordConfig.Unlocked)
            expectNoEvents()

            pending.complete(true)

            assertThat(awaitItem()).isEqualTo(AboutUiEffect.NavigateToLogoutSync)
            expectNoEvents()
        }
        verify(exactly = 0) { syncOrchestrator.execute(any<OneTime>()) }
    }

    @Test
    fun `password unlocked for logout with pending data navigates to logout sync`() = runTest {
        givenPendingData(events = true, samples = 0)

        viewModel.uiEffect.test {
            viewModel.onAction(AboutUiAction.PasswordUnlocked(AboutGatedAction.LOGOUT))

            assertThat(awaitItem()).isEqualTo(AboutUiEffect.NavigateToLogoutSync)
            expectNoEvents()
        }
        assertThat(viewModel.uiState.value.passwordConfig).isEqualTo(SettingsPasswordConfig.Unlocked)
        verify(exactly = 0) { syncOrchestrator.execute(any<OneTime>()) }
    }

    @Test
    fun `password unlocked for troubleshooting marks the settings unlocked and opens troubleshooting`() = runTest {
        assertThat(viewModel.uiState.value.passwordConfig).isEqualTo(SettingsPasswordConfig.Locked(PASSWORD))

        viewModel.uiEffect.test {
            viewModel.onAction(AboutUiAction.PasswordUnlocked(AboutGatedAction.TROUBLESHOOTING))

            assertThat(awaitItem()).isEqualTo(AboutUiEffect.NavigateToTroubleshooting)
            expectNoEvents()
        }
        assertThat(viewModel.uiState.value).isEqualTo(LOADED_STATE.copy(passwordConfig = SettingsPasswordConfig.Unlocked))
        verify(exactly = 0) { syncOrchestrator.execute(any<OneTime>()) }
    }

    // N-7
    @Test
    fun `unlock persists so later logout and troubleshooting skip the password`() = runTest {
        viewModel.uiEffect.test {
            viewModel.onAction(AboutUiAction.PasswordUnlocked(AboutGatedAction.TROUBLESHOOTING))
            assertThat(awaitItem()).isEqualTo(AboutUiEffect.NavigateToTroubleshooting)

            viewModel.onAction(AboutUiAction.LogoutClicked)
            assertThat(viewModel.uiState.value.isLogoutConfirmationVisible).isTrue()

            repeat(5) { viewModel.onAction(AboutUiAction.TroubleshootingClicked) }
            assertThat(awaitItem()).isEqualTo(AboutUiEffect.NavigateToTroubleshooting)
            expectNoEvents()
        }
    }

    // Every field that can change while the screen is open (only passwordConfig) survives the other actions.
    @Test
    fun `the unlocked state and the loaded fields survive every other action`() = runTest {
        givenPendingData(events = true, samples = 0)
        val unlocked = LOADED_STATE.copy(passwordConfig = SettingsPasswordConfig.Unlocked)

        viewModel.uiEffect.test {
            viewModel.onAction(AboutUiAction.PasswordUnlocked(AboutGatedAction.LOGOUT))
            assertThat(awaitItem()).isEqualTo(AboutUiEffect.NavigateToLogoutSync)
            assertThat(viewModel.uiState.value).isEqualTo(unlocked)

            viewModel.onAction(AboutUiAction.LogoutClicked)
            assertThat(viewModel.uiState.value).isEqualTo(unlocked.copy(isLogoutConfirmationVisible = true))
            viewModel.onAction(AboutUiAction.LogoutConfirmed)
            assertThat(awaitItem()).isEqualTo(AboutUiEffect.NavigateToLogoutSync)
            repeat(5) { viewModel.onAction(AboutUiAction.TroubleshootingClicked) }
            assertThat(awaitItem()).isEqualTo(AboutUiEffect.NavigateToTroubleshooting)
            viewModel.onAction(AboutUiAction.PasswordUnlocked(AboutGatedAction.TROUBLESHOOTING))
            assertThat(awaitItem()).isEqualTo(AboutUiEffect.NavigateToTroubleshooting)
            expectNoEvents()
        }
        assertThat(viewModel.uiState.value).isEqualTo(unlocked)
    }

    @Test
    fun `the locked state survives logout and troubleshooting actions`() = runTest {
        viewModel.uiEffect.test {
            viewModel.onAction(AboutUiAction.LogoutClicked)
            assertThat(awaitItem()).isEqualTo(AboutUiEffect.ShowPasswordDialog(PASSWORD, AboutGatedAction.LOGOUT))
            viewModel.onAction(AboutUiAction.LogoutConfirmed)
            repeat(5) { viewModel.onAction(AboutUiAction.TroubleshootingClicked) }
            assertThat(awaitItem()).isEqualTo(AboutUiEffect.ShowPasswordDialog(PASSWORD, AboutGatedAction.TROUBLESHOOTING))
            expectNoEvents()
        }
        verify(exactly = 1) { syncOrchestrator.execute(any<OneTime>()) }
        assertThat(viewModel.uiState.value).isEqualTo(LOADED_STATE)
    }

    // N-7
    @Test
    fun `fifth quick troubleshooting click with a password asks for it and does not unlock`() = runTest {
        viewModel.uiEffect.test {
            repeat(4) { viewModel.onAction(AboutUiAction.TroubleshootingClicked) }
            expectNoEvents()

            viewModel.onAction(AboutUiAction.TroubleshootingClicked)

            assertThat(awaitItem()).isEqualTo(AboutUiEffect.ShowPasswordDialog(PASSWORD, AboutGatedAction.TROUBLESHOOTING))
            expectNoEvents()
        }
        assertThat(viewModel.uiState.value.passwordConfig).isEqualTo(SettingsPasswordConfig.Locked(PASSWORD))
    }

    @Test
    fun `troubleshooting click counter resets after a pause and opens troubleshooting on the fifth quick click`() = runTest {
        givenProjectConfiguration(password = SettingsPasswordConfig.NotSet)
        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            repeat(3) { viewModel.onAction(AboutUiAction.TroubleshootingClicked) }
            advanceTimeBy(5000L)
            repeat(4) { viewModel.onAction(AboutUiAction.TroubleshootingClicked) }
            expectNoEvents()

            viewModel.onAction(AboutUiAction.TroubleshootingClicked)

            assertThat(awaitItem()).isEqualTo(AboutUiEffect.NavigateToTroubleshooting)
            expectNoEvents()
        }
    }

    @Test
    fun `troubleshooting clicks less than a second apart keep counting`() = runTest {
        givenProjectConfiguration(password = SettingsPasswordConfig.NotSet)
        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            repeat(4) {
                viewModel.onAction(AboutUiAction.TroubleshootingClicked)
                advanceTimeBy(999L)
            }
            expectNoEvents()

            viewModel.onAction(AboutUiAction.TroubleshootingClicked)

            assertThat(awaitItem()).isEqualTo(AboutUiEffect.NavigateToTroubleshooting)
            expectNoEvents()
        }
    }

    private fun createViewModel() = viewModels.track(
        AboutViewModel(
            configRepository = configRepository,
            deviceStateDataTracker = deviceStateDataTracker,
            syncOrchestrator = syncOrchestrator,
            recentUserActivityManager = recentUserActivityManager,
        ),
    )

    private fun givenPendingData(
        events: Boolean,
        samples: Int,
    ) {
        coEvery { deviceStateDataTracker.hasPendingEvents() } returns events
        coEvery { deviceStateDataTracker.getCurrentDeviceDataState() } returns mockk {
            every { pendingSamples } returns samples
        }
    }

    private fun givenProjectConfiguration(
        modalities: List<Modality> = MODALITIES,
        password: SettingsPasswordConfig = SettingsPasswordConfig.Locked(PASSWORD),
        partitionType: DownSynchronizationConfiguration.PartitionType? = PARTITION_TYPE,
        upSyncKind: UpSynchronizationKind = UpSynchronizationKind.ALL,
    ) {
        coEvery { configRepository.getProjectConfiguration() } returns projectConfiguration(
            modalities,
            password,
            partitionType,
            upSyncKind,
        )
    }

    private fun projectConfiguration(
        modalities: List<Modality> = MODALITIES,
        password: SettingsPasswordConfig = SettingsPasswordConfig.Locked(PASSWORD),
        partitionType: DownSynchronizationConfiguration.PartitionType? = PARTITION_TYPE,
        upSyncKind: UpSynchronizationKind = UpSynchronizationKind.ALL,
    ): ProjectConfiguration = mockk {
        every { general } returns mockk {
            every { this@mockk.modalities } returns modalities
            every { settingsPassword } returns password
        }
        every { identification } returns mockk {
            every { poolType } returns POOL_TYPE
        }
        every { synchronization } returns mockk {
            // null partition type: no Simprints down-sync configuration at all (CommCare projects).
            every { down.simprints } returns partitionType?.let { type ->
                mockk<SimprintsDownSynchronizationConfiguration> { every { this@mockk.partitionType } returns type }
            }
            every { up.simprints.kind } returns upSyncKind
        }
    }

    companion object {
        private const val PASSWORD = "1234"
        private const val SCANNER_VERSION = "SP424242"
        private val MODALITIES = listOf(Modality.FINGERPRINT)
        private val POOL_TYPE = IdentificationConfiguration.PoolType.MODULE
        private val PARTITION_TYPE = DownSynchronizationConfiguration.PartitionType.PROJECT

        private val LOADED_STATE = AboutUiState(
            syncAndSearchConfig = SyncAndSearchConfig(PARTITION_TYPE.name, POOL_TYPE.name),
            isScannerVersionVisible = true,
            lastScannerVersion = SCANNER_VERSION,
            passwordConfig = SettingsPasswordConfig.Locked(PASSWORD),
        )
    }
}
