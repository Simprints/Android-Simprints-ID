package com.simprints.feature.login.screens.form

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.feature.login.LoginParams
import com.simprints.infra.authlogic.AuthManager
import com.simprints.infra.authlogic.model.AuthenticateDataResult
import com.simprints.infra.network.SimNetwork
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.getOrAwaitValue
import io.mockk.MockKAnnotations
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

internal class LoginFormViewModelTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @get:Rule
    val executorRule: TestRule = InstantTaskExecutorRule()

    @MockK
    private lateinit var simNetwork: SimNetwork

    @MockK
    private lateinit var authManager: AuthManager

    private lateinit var viewModel: LoginFormViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        viewModel = LoginFormViewModel(
            DEVICE_ID,
            simNetwork,
            authManager,
        )
    }

    @Test
    fun `resets api base url on initialisation`() {
        viewModel.init()

        verify { simNetwork.resetApiBaseUrl() }
    }

    @Test
    fun `returns MissingCredentials when empty user id`() {
        viewModel.signInClicked(LoginParams(PROJECT_ID, ""), PROJECT_ID, PROJECT_SECRET)

        val result = viewModel.signInState.getOrAwaitValue()

        assertThat(result.getContentIfNotHandled()).isInstanceOf(SignInState.MissingCredential::class.java)
    }

    @Test
    fun `returns MissingCredentials when empty project id`() {
        viewModel.signInClicked(LoginParams(PROJECT_ID, USER_ID), "", PROJECT_SECRET)

        val result = viewModel.signInState.getOrAwaitValue()

        assertThat(result.getContentIfNotHandled()).isInstanceOf(SignInState.MissingCredential::class.java)
    }

    @Test
    fun `returns MissingCredentials when empty project secret`() {
        viewModel.signInClicked(LoginParams(PROJECT_ID, USER_ID), PROJECT_ID, "")

        val result = viewModel.signInState.getOrAwaitValue()

        assertThat(result.getContentIfNotHandled()).isInstanceOf(SignInState.MissingCredential::class.java)
    }

    @Test
    fun `returns ProjectIdMismatch when login and actual project ids not match`() {
        viewModel.signInClicked(LoginParams(PROJECT_ID, USER_ID), "otherProjectId", PROJECT_SECRET)

        val result = viewModel.signInState.getOrAwaitValue()

        assertThat(result.getContentIfNotHandled()).isInstanceOf(SignInState.ProjectIdMismatch::class.java)
    }

    @Test
    fun `returns correct SignInState for each auth result class`() {
        mapOf(
            AuthenticateDataResult.Authenticated to SignInState.Success::class.java,
            AuthenticateDataResult.BadCredentials to SignInState.BadCredentials::class.java,
            AuthenticateDataResult.IntegrityException to SignInState.IntegrityException::class.java,
            AuthenticateDataResult.IntegrityServiceTemporaryDown to SignInState.IntegrityServiceTemporaryDown::class.java,
            AuthenticateDataResult.MissingOrOutdatedGooglePlayStoreApp to SignInState.MissingOrOutdatedGooglePlayStoreApp::class.java,
            AuthenticateDataResult.Offline to SignInState.Offline::class.java,
            AuthenticateDataResult.TechnicalFailure to SignInState.TechnicalFailure::class.java,
            AuthenticateDataResult.Unknown to SignInState.Unknown::class.java,
            AuthenticateDataResult.BackendMaintenanceError() to SignInState.BackendMaintenanceError::class.java,
        ).forEach { (provided, expected) ->
            clearMocks(authManager)
            coEvery { authManager.authenticateSafely(any(), any(), any(), any()) } returns provided
            viewModel.signInClicked(LoginParams(PROJECT_ID, USER_ID), PROJECT_ID, PROJECT_SECRET)

            val result = viewModel.signInState.getOrAwaitValue()

            assertThat(result.getContentIfNotHandled()).isInstanceOf(expected)
        }
    }

    @Test
    fun `returns correct SignInState with estimated outage`() {
        coEvery { authManager.authenticateSafely(any(), any(), any(), any()) } returns
            AuthenticateDataResult.BackendMaintenanceError(100)

        viewModel.signInClicked(LoginParams(PROJECT_ID, USER_ID), PROJECT_ID, PROJECT_SECRET)

        val result = viewModel.signInState.getOrAwaitValue()

        assertThat((result.getContentIfNotHandled() as SignInState.BackendMaintenanceError).estimatedOutage)
            .isEqualTo("01 minutes, 40 seconds")
    }

    companion object {

        private const val DEVICE_ID = "deviceId"
        private const val PROJECT_ID = "projectId"
        private const val USER_ID = "userId"
        private const val PROJECT_SECRET = "projectSecret"
    }
}
