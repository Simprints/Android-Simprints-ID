package com.simprints.feature.logincheck

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.jraska.livedata.test
import com.simprints.feature.login.LoginError
import com.simprints.feature.login.LoginResult
import com.simprints.feature.logincheck.usecases.*
import com.simprints.feature.logincheck.usecases.ActionFactory
import com.simprints.feature.logincheck.usecases.AddAuthorizationEventUseCase
import com.simprints.feature.logincheck.usecases.CancelBackgroundSyncUseCase
import com.simprints.feature.logincheck.usecases.ExtractCrashKeysUseCase
import com.simprints.feature.logincheck.usecases.ExtractParametersForAnalyticsUseCase
import com.simprints.feature.logincheck.usecases.GetProjectStateUseCase
import com.simprints.feature.logincheck.usecases.IsUserSignedInUseCase
import com.simprints.feature.logincheck.usecases.ReportActionRequestEventsUseCase
import com.simprints.feature.logincheck.usecases.StartBackgroundSyncUseCase
import com.simprints.feature.logincheck.usecases.UpdateDatabaseCountsInCurrentSessionUseCase
import com.simprints.feature.logincheck.usecases.UpdateProjectInCurrentSessionUseCase
import com.simprints.infra.security.SecurityManager
import com.simprints.infra.security.exceptions.RootedDeviceException
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test


internal class LoginCheckViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    lateinit var rootMatchers: SecurityManager

    @MockK
    lateinit var reportActionRequestEventsUseCase: ReportActionRequestEventsUseCase

    @MockK
    lateinit var extractParametersForAnalyticsUseCase: ExtractParametersForAnalyticsUseCase

    @MockK
    lateinit var extractCrashKeysUseCase: ExtractCrashKeysUseCase

    @MockK
    lateinit var addAuthorizationEventUseCase: AddAuthorizationEventUseCase

    @MockK
    lateinit var isUserSignedInUseCase: IsUserSignedInUseCase

    @MockK
    lateinit var getProjectStateUseCase: GetProjectStateUseCase

    @MockK
    lateinit var startBackgroundSync: StartBackgroundSyncUseCase

    @MockK
    lateinit var cleanupDeprecatedWorkersUseCase: CleanupDeprecatedWorkersUseCase

    @MockK
    lateinit var cancelBackgroundSync: CancelBackgroundSyncUseCase

    @MockK
    lateinit var updateDatabaseCountsInCurrentSessionUseCase: UpdateDatabaseCountsInCurrentSessionUseCase

    @MockK
    lateinit var updateProjectStateUseCase: UpdateProjectInCurrentSessionUseCase

    private lateinit var viewModel: LoginCheckViewModel


    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        viewModel = LoginCheckViewModel(
            rootMatchers,
            reportActionRequestEventsUseCase,
            extractParametersForAnalyticsUseCase,
            extractCrashKeysUseCase,
            addAuthorizationEventUseCase,
            isUserSignedInUseCase,
            getProjectStateUseCase,
            startBackgroundSync,
            cleanupDeprecatedWorkersUseCase,
            cancelBackgroundSync,
            updateDatabaseCountsInCurrentSessionUseCase,
            updateProjectStateUseCase,
        )
    }

    @Test
    fun `Triggers alert if device is rooted`() = runTest {
        every { rootMatchers.checkIfDeviceIsRooted() } throws RootedDeviceException()

        assertThat(viewModel.isDeviceSafe()).isFalse()

        viewModel.showAlert.test().assertValue { it.peekContent() == com.simprints.feature.logincheck.LoginCheckError.ROOTED_DEVICE }
    }

    @Test
    fun `Correctly reports safe device if not rooted`() = runTest {
        assertThat(viewModel.isDeviceSafe()).isTrue()

        viewModel.showAlert.test().assertNoValue()
    }

    @Test
    fun `Reports action event and extracts analytics for valid action`() = runTest {
        viewModel.validateSignInAndProceed(ActionFactory.getFlowRequest())

        coVerify {
            reportActionRequestEventsUseCase.invoke(any())
            extractParametersForAnalyticsUseCase.invoke(any())
        }
    }

    @Test
    fun `Triggers alert if mismatched project IDs`() = runTest {
        coEvery { isUserSignedInUseCase.invoke(any()) } returns com.simprints.feature.logincheck.usecases.IsUserSignedInUseCase.SignedInState.MISMATCHED_PROJECT_ID

        viewModel.validateSignInAndProceed(ActionFactory.getFlowRequest())

        viewModel.showAlert.test().assertValue { it.peekContent() == com.simprints.feature.logincheck.LoginCheckError.DIFFERENT_PROJECT_ID }
    }

    @Test
    fun `Returns error response if not signed in and followup action`() = runTest {
        coEvery { isUserSignedInUseCase.invoke(any()) } returns com.simprints.feature.logincheck.usecases.IsUserSignedInUseCase.SignedInState.NOT_SIGNED_IN

        viewModel.validateSignInAndProceed(ActionFactory.getFolowUpRequest())

        viewModel.returnLoginNotComplete.test().assertHasValue()
    }

    @Test
    fun `Returns error response if there are several login attempts`() = runTest {
        coEvery { isUserSignedInUseCase.invoke(any()) } returns com.simprints.feature.logincheck.usecases.IsUserSignedInUseCase.SignedInState.NOT_SIGNED_IN

        viewModel.validateSignInAndProceed(ActionFactory.getFlowRequest())
        viewModel.validateSignInAndProceed(ActionFactory.getFlowRequest())

        viewModel.returnLoginNotComplete.test().assertHasValue()
    }

    @Test
    fun `Triggers login flow if not signed in and flow action`() = runTest {
        coEvery { isUserSignedInUseCase.invoke(any()) } returns com.simprints.feature.logincheck.usecases.IsUserSignedInUseCase.SignedInState.NOT_SIGNED_IN

        viewModel.validateSignInAndProceed(ActionFactory.getFlowRequest())

        coVerify {
            addAuthorizationEventUseCase.invoke(any(), eq(false))
            cancelBackgroundSync.invoke()
        }
        viewModel.showLoginFlow.test().assertValue { it.peekContent() == ActionFactory.getFlowRequest() }
    }

    @Test
    fun `Returns error response if login attempt not complete`() = runTest {
        coEvery { isUserSignedInUseCase.invoke(any()) } returns com.simprints.feature.logincheck.usecases.IsUserSignedInUseCase.SignedInState.NOT_SIGNED_IN

        viewModel.validateSignInAndProceed(ActionFactory.getFlowRequest())
        viewModel.handleLoginResult(LoginResult(false, LoginError.LoginNotCompleted))

        viewModel.returnLoginNotComplete.test().assertHasValue()
    }

    @Test
    fun `Triggers alert if login if failed to cache request`() = runTest {
        coEvery { isUserSignedInUseCase.invoke(any()) } returns com.simprints.feature.logincheck.usecases.IsUserSignedInUseCase.SignedInState.NOT_SIGNED_IN

        viewModel.handleLoginResult(LoginResult(true, null))

        viewModel.showAlert.test().assertValue { it.peekContent() == com.simprints.feature.logincheck.LoginCheckError.UNEXPECTED_LOGIN_ERROR }
    }

    @Test
    fun `Triggers alert if login attempt failed with integrity error`() = runTest {
        coEvery { isUserSignedInUseCase.invoke(any()) } returns com.simprints.feature.logincheck.usecases.IsUserSignedInUseCase.SignedInState.NOT_SIGNED_IN

        viewModel.validateSignInAndProceed(ActionFactory.getFlowRequest())
        viewModel.handleLoginResult(LoginResult(false, LoginError.IntegrityServiceError))

        viewModel.showAlert.test().assertValue { it.peekContent() == com.simprints.feature.logincheck.LoginCheckError.INTEGRITY_SERVICE_ERROR }
    }

    @Test
    fun `Triggers alert if login attempt failed with missing play services`() = runTest {
        coEvery { isUserSignedInUseCase.invoke(any()) } returns com.simprints.feature.logincheck.usecases.IsUserSignedInUseCase.SignedInState.NOT_SIGNED_IN

        viewModel.validateSignInAndProceed(ActionFactory.getFlowRequest())
        viewModel.handleLoginResult(LoginResult(false, LoginError.MissingPlayServices))

        viewModel.showAlert.test().assertValue { it.peekContent() == com.simprints.feature.logincheck.LoginCheckError.MISSING_GOOGLE_PLAY_SERVICES }
    }

    @Test
    fun `Triggers alert if login attempt failed with outdated play services`() = runTest {
        coEvery { isUserSignedInUseCase.invoke(any()) } returns com.simprints.feature.logincheck.usecases.IsUserSignedInUseCase.SignedInState.NOT_SIGNED_IN

        viewModel.validateSignInAndProceed(ActionFactory.getFlowRequest())
        viewModel.handleLoginResult(LoginResult(false, LoginError.OutdatedPlayServices))

        viewModel.showAlert.test().assertValue { it.peekContent() == com.simprints.feature.logincheck.LoginCheckError.GOOGLE_PLAY_SERVICES_OUTDATED }
    }

    @Test
    fun `Triggers alert if login attempt failed with missing or outdated play services`() = runTest {
        coEvery { isUserSignedInUseCase.invoke(any()) } returns com.simprints.feature.logincheck.usecases.IsUserSignedInUseCase.SignedInState.NOT_SIGNED_IN

        viewModel.validateSignInAndProceed(ActionFactory.getFlowRequest())
        viewModel.handleLoginResult(LoginResult(false, LoginError.MissingOrOutdatedPlayServices))

        viewModel.showAlert.test().assertValue { it.peekContent() == com.simprints.feature.logincheck.LoginCheckError.MISSING_OR_OUTDATED_GOOGLE_PLAY_STORE_APP }
    }

    @Test
    fun `Triggers alert if login attempt failed with unknown error`() = runTest {
        coEvery { isUserSignedInUseCase.invoke(any()) } returns com.simprints.feature.logincheck.usecases.IsUserSignedInUseCase.SignedInState.NOT_SIGNED_IN

        viewModel.validateSignInAndProceed(ActionFactory.getFlowRequest())
        viewModel.handleLoginResult(LoginResult(false, LoginError.Unknown))

        viewModel.showAlert.test().assertValue { it.peekContent() == com.simprints.feature.logincheck.LoginCheckError.UNEXPECTED_LOGIN_ERROR }
    }

    @Test
    fun `Correctly handles successful login attempt`() = runTest {
        coEvery { isUserSignedInUseCase.invoke(any()) } returns com.simprints.feature.logincheck.usecases.IsUserSignedInUseCase.SignedInState.NOT_SIGNED_IN

        viewModel.validateSignInAndProceed(ActionFactory.getFlowRequest())
        viewModel.handleLoginResult(LoginResult(true, null))

        verify { getProjectStateUseCase.invoke() }
    }

    @Test
    fun `Correctly handles signed in users`() = runTest {
        coEvery { isUserSignedInUseCase.invoke(any()) } returns com.simprints.feature.logincheck.usecases.IsUserSignedInUseCase.SignedInState.SIGNED_IN

        viewModel.validateSignInAndProceed(ActionFactory.getFlowRequest())

        verify { getProjectStateUseCase.invoke() }
    }

    @Test
    fun `Triggers alert if project is paused`() = runTest {
        coEvery { isUserSignedInUseCase.invoke(any()) } returns com.simprints.feature.logincheck.usecases.IsUserSignedInUseCase.SignedInState.SIGNED_IN
        coEvery { getProjectStateUseCase.invoke() } returns com.simprints.feature.logincheck.usecases.GetProjectStateUseCase.ProjectState.PAUSED

        viewModel.validateSignInAndProceed(ActionFactory.getFlowRequest())

        viewModel.showAlert.test().assertValue { it.peekContent() == com.simprints.feature.logincheck.LoginCheckError.PROJECT_PAUSED }
    }

    @Test
    fun `Triggers alert if project is ending`() = runTest {
        coEvery { isUserSignedInUseCase.invoke(any()) } returns com.simprints.feature.logincheck.usecases.IsUserSignedInUseCase.SignedInState.SIGNED_IN
        coEvery { getProjectStateUseCase.invoke() } returns com.simprints.feature.logincheck.usecases.GetProjectStateUseCase.ProjectState.ENDING

        viewModel.validateSignInAndProceed(ActionFactory.getFlowRequest())

        viewModel.showAlert.test().assertValue { it.peekContent() == com.simprints.feature.logincheck.LoginCheckError.PROJECT_ENDING }
    }

    @Test
    fun `Triggers login attempt if project has ended`() = runTest {
        coEvery { isUserSignedInUseCase.invoke(any()) } returns com.simprints.feature.logincheck.usecases.IsUserSignedInUseCase.SignedInState.SIGNED_IN
        coEvery { getProjectStateUseCase.invoke() } returns com.simprints.feature.logincheck.usecases.GetProjectStateUseCase.ProjectState.ENDED

        viewModel.validateSignInAndProceed(ActionFactory.getFlowRequest())

        viewModel.showLoginFlow.test().assertValue { it.peekContent() == ActionFactory.getFlowRequest() }
    }

    @Test
    fun `Correctly handles if user signed in active project`() = runTest {
        coEvery { isUserSignedInUseCase.invoke(any()) } returns com.simprints.feature.logincheck.usecases.IsUserSignedInUseCase.SignedInState.SIGNED_IN
        coEvery { getProjectStateUseCase.invoke() } returns com.simprints.feature.logincheck.usecases.GetProjectStateUseCase.ProjectState.ACTIVE

        viewModel.validateSignInAndProceed(ActionFactory.getFlowRequest())

        coVerify {
            updateProjectStateUseCase.invoke()
            updateDatabaseCountsInCurrentSessionUseCase.invoke()
            addAuthorizationEventUseCase.invoke(any(), eq(true))
            extractCrashKeysUseCase.invoke(any())
            startBackgroundSync.invoke()
        }

        viewModel.proceedWithAction.test().assertValue { it.peekContent() == ActionFactory.getFlowRequest() }
    }

}
