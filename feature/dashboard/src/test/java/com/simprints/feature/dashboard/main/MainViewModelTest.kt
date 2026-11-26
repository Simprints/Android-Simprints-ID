package com.simprints.feature.dashboard.main

import com.google.common.truth.Truth.*
import com.simprints.feature.dashboard.tools.ViewModelScopeRule
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.logging.Simber
import com.simprints.infra.security.SecurityManager
import com.simprints.infra.security.exceptions.RootedDeviceException
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MainViewModelTest {
    @get:Rule(order = 0)
    val testCoroutineRule = TestCoroutineRule()

    // Inside TestCoroutineRule: scopes are cancelled while the test Main dispatcher is still set
    @get:Rule(order = 1)
    val viewModels = ViewModelScopeRule()

    private val configRepository = mockk<ConfigRepository> {
        coEvery { getProjectConfiguration() } returns projectConfiguration(consentRequired = true)
    }
    private val securityManager = mockk<SecurityManager>(relaxed = true)

    private lateinit var viewModel: MainViewModel

    @Before
    fun setUp() {
        mockkObject(Simber)
        justRun { Simber.e(message = any<String>(), t = any<Throwable>(), tag = any<String>()) }

        // Created after TestCoroutineRule has set the test Main dispatcher: the base class starts its action loop in init,
        // and the init block sends the Load.
        viewModel = createViewModel()
    }

    @After
    fun tearDown() {
        unmockkObject(Simber)
    }

    @Test
    fun `init loads the consent requirement from the project configuration`() = runTest {
        assertThat(viewModel.uiState.value).isEqualTo(MainUiState(isConsentRequired = true, isRootedDeviceDetected = false))
        coVerify(exactly = 1) { configRepository.getProjectConfiguration() }
    }

    @Test
    fun `rooted device sets the rooted state and logs the error`() = runTest {
        every { securityManager.checkIfDeviceIsRooted() } throws RootedDeviceException()

        val viewModel = createViewModel()

        assertThat(viewModel.uiState.value).isEqualTo(MainUiState(isConsentRequired = true, isRootedDeviceDetected = true))
        // Matched by type: coroutine stack-trace recovery in tests may rethrow a copy of the exception out of withContext
        verify(exactly = 1) { Simber.e("Rooted device detected", ofType<RootedDeviceException>(), any<String>()) }
    }

    @Test
    fun `consent not collected and a safe device produce no consent requirement and no rooted state`() = runTest {
        coEvery { configRepository.getProjectConfiguration() } returns projectConfiguration(consentRequired = false)
        clearMocks(securityManager, answers = false)

        val viewModel = createViewModel()

        assertThat(viewModel.uiState.value).isEqualTo(MainUiState(isConsentRequired = false, isRootedDeviceDetected = false))
        verify(exactly = 1) { securityManager.checkIfDeviceIsRooted() }
        verify(exactly = 0) { Simber.e(message = any<String>(), t = any<Throwable>(), tag = any<String>()) }
    }

    @Test
    fun `load runs the root check before reading the configuration`() = runTest {
        val configuration = CompletableDeferred<ProjectConfiguration>()
        coEvery { configRepository.getProjectConfiguration() } coAnswers { configuration.await() }
        every { securityManager.checkIfDeviceIsRooted() } throws RootedDeviceException()
        clearMocks(securityManager, answers = false) // forget the root check of the ViewModel built in setUp

        val viewModel = createViewModel()

        // The configuration read is still waiting, yet the rooted state is already set
        assertThat(viewModel.uiState.value).isEqualTo(MainUiState(isConsentRequired = null, isRootedDeviceDetected = true))
        verify(exactly = 1) { securityManager.checkIfDeviceIsRooted() }

        configuration.complete(projectConfiguration(consentRequired = true))

        assertThat(viewModel.uiState.value).isEqualTo(MainUiState(isConsentRequired = true, isRootedDeviceDetected = true))
        verify(exactly = 1) { securityManager.checkIfDeviceIsRooted() }
    }

    @Test
    fun `failing configuration read still runs the root check, logs the failure and leaves consent unknown`() = runTest {
        val failure = IllegalStateException(CONFIG_FAILURE)
        coEvery { configRepository.getProjectConfiguration() } throws failure
        every { securityManager.checkIfDeviceIsRooted() } throws RootedDeviceException()
        clearMocks(securityManager, answers = false) // forget the root check of the ViewModel built in setUp

        // runTest fails if an exception escapes viewModelScope, so finishing normally shows nothing escaped
        val viewModel = createViewModel()

        assertThat(viewModel.uiState.value).isEqualTo(MainUiState(isConsentRequired = null, isRootedDeviceDetected = true))
        verify(exactly = 1) { securityManager.checkIfDeviceIsRooted() }
        verify(exactly = 1) { Simber.e("Failed to load project configuration on dashboard", failure, any<String>()) }
    }

    @Test
    fun `failing configuration read on a safe device leaves consent unknown and no rooted state`() = runTest {
        coEvery { configRepository.getProjectConfiguration() } throws IllegalStateException(CONFIG_FAILURE)
        clearMocks(securityManager, answers = false)

        val viewModel = createViewModel()

        verify(exactly = 1) { securityManager.checkIfDeviceIsRooted() }
        assertThat(viewModel.uiState.value).isEqualTo(MainUiState(isConsentRequired = null, isRootedDeviceDetected = false))
    }

    @Test
    fun `cancelled configuration read is not logged as a failure`() = runTest {
        coEvery { configRepository.getProjectConfiguration() } throws CancellationException("cancelled")
        clearMocks(securityManager, answers = false)

        val viewModel = createViewModel()

        verify(exactly = 1) { securityManager.checkIfDeviceIsRooted() }
        assertThat(viewModel.uiState.value).isEqualTo(MainUiState(isConsentRequired = null, isRootedDeviceDetected = false))
        verify(exactly = 0) { Simber.e(message = any<String>(), t = any<Throwable>(), tag = any<String>()) }
    }

    @Test
    fun `root check runs on the io dispatcher and the configuration is read after it`() = runTest {
        val ioDispatcher = StandardTestDispatcher(testCoroutineRule.testCoroutineDispatcher.scheduler)
        every { securityManager.checkIfDeviceIsRooted() } throws RootedDeviceException()
        clearMocks(securityManager, configRepository, answers = false) // forget the calls of the ViewModel built in setUp

        val viewModel = createViewModel(ioDispatcher)

        // Nothing ran yet: the root check waits for the io dispatcher, and the configuration read waits for the root check
        verify(exactly = 0) { securityManager.checkIfDeviceIsRooted() }
        coVerify(exactly = 0) { configRepository.getProjectConfiguration() }
        assertThat(viewModel.uiState.value).isEqualTo(MainUiState())

        advanceUntilIdle()

        verify(exactly = 1) { securityManager.checkIfDeviceIsRooted() }
        coVerify(exactly = 1) { configRepository.getProjectConfiguration() }
        assertThat(viewModel.uiState.value).isEqualTo(MainUiState(isConsentRequired = true, isRootedDeviceDetected = true))
    }

    // The test Main dispatcher is unconfined, so by default the io work runs straight away, as on a free io thread.
    private fun createViewModel(ioDispatcher: CoroutineDispatcher = testCoroutineRule.testCoroutineDispatcher) = viewModels.track(
        MainViewModel(
            configRepository = configRepository,
            securityManager = securityManager,
            ioDispatcher = ioDispatcher,
        ),
    )

    private fun projectConfiguration(consentRequired: Boolean) = mockk<ProjectConfiguration> {
        every { consent } returns mockk {
            every { collectConsent } returns consentRequired
        }
    }

    companion object {
        private const val CONFIG_FAILURE = "config store unreadable"
    }
}
