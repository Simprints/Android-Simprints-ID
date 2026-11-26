package com.simprints.feature.dashboard.logout.syncdecline

import app.cash.turbine.test
import com.google.common.truth.Truth.*
import com.simprints.feature.dashboard.tools.ViewModelScopeRule
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.SettingsPasswordConfig
import com.simprints.infra.sync.OneTime
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LogoutSyncDeclineViewModelTest {
    @get:Rule(order = 0)
    val testCoroutineRule = TestCoroutineRule()

    // Inside TestCoroutineRule: scopes are cancelled while the test Main dispatcher is still set
    @get:Rule(order = 1)
    val viewModels = ViewModelScopeRule()

    private val configRepository = mockk<ConfigRepository>()
    private val syncOrchestrator = mockk<SyncOrchestrator>(relaxed = true)

    private lateinit var viewModel: LogoutSyncDeclineViewModel

    @Before
    fun setUp() {
        givenPasswordConfig(SettingsPasswordConfig.NotSet)
        viewModel = viewModels.track(
            LogoutSyncDeclineViewModel(
                configRepository = configRepository,
                syncOrchestrator = syncOrchestrator,
            ),
        )
    }

    @Test
    fun `opening the screen reads no configuration and issues no command`() = runTest {
        assertThat(viewModel.uiState.value).isEqualTo(LogoutSyncDeclineUiState(isLogoutConfirmationVisible = false))
        coVerify(exactly = 0) { configRepository.getProjectConfiguration() }
        verify(exactly = 0) { syncOrchestrator.execute(any<OneTime>()) }
        viewModel.uiEffect.test { expectNoEvents() }
    }

    @Test
    fun `confirm without a password shows the logout confirmation`() = runTest {
        viewModel.uiEffect.test {
            viewModel.onAction(LogoutSyncDeclineUiAction.ConfirmClicked)

            assertThat(viewModel.uiState.value.isLogoutConfirmationVisible).isTrue()
            expectNoEvents()
        }
    }

    @Test
    fun `confirm with an unlocked password config shows the logout confirmation`() = runTest {
        givenPasswordConfig(SettingsPasswordConfig.Unlocked)

        viewModel.uiEffect.test {
            viewModel.onAction(LogoutSyncDeclineUiAction.ConfirmClicked)

            assertThat(viewModel.uiState.value.isLogoutConfirmationVisible).isTrue()
            expectNoEvents()
        }
    }

    @Test
    fun `confirm with a password asks for that password`() = runTest {
        givenPasswordConfig(SettingsPasswordConfig.Locked(password = "123"))

        viewModel.uiEffect.test {
            viewModel.onAction(LogoutSyncDeclineUiAction.ConfirmClicked)

            assertThat(awaitItem()).isEqualTo(LogoutSyncDeclineUiEffect.ShowPasswordDialog(password = "123"))
            assertThat(viewModel.uiState.value.isLogoutConfirmationVisible).isFalse()
            expectNoEvents()
        }
    }

    @Test
    fun `confirm does not log out by itself`() = runTest {
        givenPasswordConfig(SettingsPasswordConfig.Locked(password = "123"))

        viewModel.onAction(LogoutSyncDeclineUiAction.ConfirmClicked)

        viewModel.uiEffect.test { assertThat(awaitItem()).isInstanceOf(LogoutSyncDeclineUiEffect.ShowPasswordDialog::class.java) }
        verify(exactly = 0) { syncOrchestrator.execute(any<OneTime>()) }
    }

    @Test
    fun `confirm request sent before anyone collects is still delivered`() = runTest {
        givenPasswordConfig(SettingsPasswordConfig.Locked(password = "123"))

        viewModel.onAction(LogoutSyncDeclineUiAction.ConfirmClicked)

        viewModel.uiEffect.test {
            assertThat(awaitItem()).isEqualTo(LogoutSyncDeclineUiEffect.ShowPasswordDialog(password = "123"))
            expectNoEvents()
        }
    }

    @Test
    fun `password config is read on every confirm click so a changed config applies to the next click`() = runTest {
        givenPasswordConfig(SettingsPasswordConfig.Locked(password = "123"))

        viewModel.uiEffect.test {
            viewModel.onAction(LogoutSyncDeclineUiAction.ConfirmClicked)
            assertThat(awaitItem()).isEqualTo(LogoutSyncDeclineUiEffect.ShowPasswordDialog(password = "123"))

            givenPasswordConfig(SettingsPasswordConfig.NotSet)
            viewModel.onAction(LogoutSyncDeclineUiAction.ConfirmClicked)
            assertThat(viewModel.uiState.value.isLogoutConfirmationVisible).isTrue()
            viewModel.onAction(LogoutSyncDeclineUiAction.LogoutConfirmationDismissed)

            givenPasswordConfig(SettingsPasswordConfig.Locked(password = "456"))
            viewModel.onAction(LogoutSyncDeclineUiAction.ConfirmClicked)
            assertThat(awaitItem()).isEqualTo(LogoutSyncDeclineUiEffect.ShowPasswordDialog(password = "456"))
        }
        coVerify(exactly = 3) { configRepository.getProjectConfiguration() }
    }

    @Test
    fun `two quick confirm clicks keep a single logout confirmation visible`() = runTest {
        viewModel.uiEffect.test {
            viewModel.onAction(LogoutSyncDeclineUiAction.ConfirmClicked)
            viewModel.onAction(LogoutSyncDeclineUiAction.ConfirmClicked)

            assertThat(viewModel.uiState.value).isEqualTo(LogoutSyncDeclineUiState(isLogoutConfirmationVisible = true))
            expectNoEvents()
        }
        coVerify(exactly = 2) { configRepository.getProjectConfiguration() }
    }

    @Test
    fun `dismissing the logout confirmation hides it without logging out`() = runTest {
        viewModel.onAction(LogoutSyncDeclineUiAction.ConfirmClicked)
        assertThat(viewModel.uiState.value.isLogoutConfirmationVisible).isTrue()

        viewModel.onAction(LogoutSyncDeclineUiAction.LogoutConfirmationDismissed)

        assertThat(viewModel.uiState.value.isLogoutConfirmationVisible).isFalse()
        verify(exactly = 0) { syncOrchestrator.execute(any<OneTime>()) }
    }

    @Test
    fun `confirming the logout hides the confirmation and logs out`() = runTest {
        viewModel.onAction(LogoutSyncDeclineUiAction.ConfirmClicked)
        assertThat(viewModel.uiState.value.isLogoutConfirmationVisible).isTrue()

        viewModel.onAction(LogoutSyncDeclineUiAction.LogoutConfirmed)

        assertThat(viewModel.uiState.value.isLogoutConfirmationVisible).isFalse()
        verify(exactly = 1) { syncOrchestrator.execute(OneTime.Logout.start(isProjectEnded = false)) }
    }

    @Test
    fun `logout confirmed executes exactly the logout start command and nothing else`() = runTest {
        viewModel.uiEffect.test {
            viewModel.onAction(LogoutSyncDeclineUiAction.LogoutConfirmed)

            expectNoEvents()
        }

        verify(exactly = 1) { syncOrchestrator.execute(OneTime.Logout.start(isProjectEnded = false)) }
        verify(exactly = 1) { syncOrchestrator.execute(any<OneTime>()) }
        coVerify(exactly = 0) { configRepository.getProjectConfiguration() }
    }

    private fun givenPasswordConfig(config: SettingsPasswordConfig) {
        coEvery { configRepository.getProjectConfiguration() } returns mockk<ProjectConfiguration> {
            every { general } returns mockk {
                every { settingsPassword } returns config
            }
        }
    }
}
