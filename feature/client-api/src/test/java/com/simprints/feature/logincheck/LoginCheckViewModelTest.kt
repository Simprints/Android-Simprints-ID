package com.simprints.feature.logincheck

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.jraska.livedata.test
import com.simprints.feature.clientapi.mappers.request.requestFactories.ConfirmIdentityActionFactory
import com.simprints.feature.clientapi.mappers.request.requestFactories.EnrolActionFactory
import com.simprints.feature.login.LoginError
import com.simprints.feature.login.LoginResult
import com.simprints.feature.logincheck.usecases.AddAuthorizationEventUseCase
import com.simprints.feature.logincheck.usecases.CancelBackgroundSyncUseCase
import com.simprints.feature.logincheck.usecases.ExtractCrashKeysUseCase
import com.simprints.feature.logincheck.usecases.ExtractParametersForAnalyticsUseCase
import com.simprints.feature.logincheck.usecases.GetProjectStateUseCase
import com.simprints.feature.logincheck.usecases.IsUserSignedInUseCase
import com.simprints.feature.logincheck.usecases.ReportActionRequestEventsUseCase
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
            cancelBackgroundSync,
            updateDatabaseCountsInCurrentSessionUseCase,
            updateProjectStateUseCase,
        )
    }

    @Test
    fun `Triggers alert if device is rooted`() = runTest {
        every { rootMatchers.checkIfDeviceIsRooted() } throws RootedDeviceException()

        assertThat(viewModel.isDeviceSafe()).isFalse()

        viewModel.showAlert.test().assertValue { it.peekContent() == LoginCheckError.ROOTED_DEVICE }
    }

    @Test
    fun `Correctly reports safe device if not rooted`() = runTest {
        assertThat(viewModel.isDeviceSafe()).isTrue()

        viewModel.showAlert.test().assertNoValue()
    }

    @Test
    fun `Reports action event and extracts analytics for valid action`() = runTest {
        viewModel.validateSignInAndProceed(FLOW_ACTION)

        coVerify {
            reportActionRequestEventsUseCase.invoke(any())
            extractParametersForAnalyticsUseCase.invoke(any())
        }
    }

    @Test
    fun `Triggers alert if mismatched project IDs`() = runTest {
        coEvery { isUserSignedInUseCase.invoke(any()) } returns IsUserSignedInUseCase.SignedInState.MISMATCHED_PROJECT_ID

        viewModel.validateSignInAndProceed(FLOW_ACTION)

        viewModel.showAlert.test().assertValue { it.peekContent() == LoginCheckError.DIFFERENT_PROJECT_ID }
    }

    @Test
    fun `Returns error response if not signed in and followup action`() = runTest {
        coEvery { isUserSignedInUseCase.invoke(any()) } returns IsUserSignedInUseCase.SignedInState.NOT_SIGNED_IN

        viewModel.validateSignInAndProceed(FOLLOWUP_ACTON)

        viewModel.returnLoginNotComplete.test().assertHasValue()
    }

    @Test
    fun `Returns error response if there are several login attempts`() = runTest {
        coEvery { isUserSignedInUseCase.invoke(any()) } returns IsUserSignedInUseCase.SignedInState.NOT_SIGNED_IN

        viewModel.validateSignInAndProceed(FLOW_ACTION)
        viewModel.validateSignInAndProceed(FLOW_ACTION)

        viewModel.returnLoginNotComplete.test().assertHasValue()
    }

    @Test
    fun `Triggers login flow if not signed in and flow action`() = runTest {
        coEvery { isUserSignedInUseCase.invoke(any()) } returns IsUserSignedInUseCase.SignedInState.NOT_SIGNED_IN

        viewModel.validateSignInAndProceed(FLOW_ACTION)

        coVerify {
            addAuthorizationEventUseCase.invoke(any(), eq(false))
            cancelBackgroundSync.invoke()
        }
        viewModel.showLoginFlow.test().assertValue { it.peekContent() == FLOW_ACTION }
    }

    @Test
    fun `Returns error response if login attempt not complete`() = runTest {
        coEvery { isUserSignedInUseCase.invoke(any()) } returns IsUserSignedInUseCase.SignedInState.NOT_SIGNED_IN

        viewModel.validateSignInAndProceed(FLOW_ACTION)
        viewModel.handleLoginResult(LoginResult(false, LoginError.LoginNotCompleted))

        viewModel.returnLoginNotComplete.test().assertHasValue()
    }

    @Test
    fun `Triggers alert if login if failed to cache request`() = runTest {
        coEvery { isUserSignedInUseCase.invoke(any()) } returns IsUserSignedInUseCase.SignedInState.NOT_SIGNED_IN

        viewModel.handleLoginResult(LoginResult(true, null))

        viewModel.showAlert.test().assertValue { it.peekContent() == LoginCheckError.UNEXPECTED_LOGIN_ERROR }
    }

    @Test
    fun `Triggers alert if login attempt failed with integrity error`() = runTest {
        coEvery { isUserSignedInUseCase.invoke(any()) } returns IsUserSignedInUseCase.SignedInState.NOT_SIGNED_IN

        viewModel.validateSignInAndProceed(FLOW_ACTION)
        viewModel.handleLoginResult(LoginResult(false, LoginError.IntegrityServiceError))

        viewModel.showAlert.test().assertValue { it.peekContent() == LoginCheckError.INTEGRITY_SERVICE_ERROR }
    }

    @Test
    fun `Triggers alert if login attempt failed with missing play services`() = runTest {
        coEvery { isUserSignedInUseCase.invoke(any()) } returns IsUserSignedInUseCase.SignedInState.NOT_SIGNED_IN

        viewModel.validateSignInAndProceed(FLOW_ACTION)
        viewModel.handleLoginResult(LoginResult(false, LoginError.MissingPlayServices))

        viewModel.showAlert.test().assertValue { it.peekContent() == LoginCheckError.MISSING_GOOGLE_PLAY_SERVICES }
    }

    @Test
    fun `Triggers alert if login attempt failed with outdated play services`() = runTest {
        coEvery { isUserSignedInUseCase.invoke(any()) } returns IsUserSignedInUseCase.SignedInState.NOT_SIGNED_IN

        viewModel.validateSignInAndProceed(FLOW_ACTION)
        viewModel.handleLoginResult(LoginResult(false, LoginError.OutdatedPlayServices))

        viewModel.showAlert.test().assertValue { it.peekContent() == LoginCheckError.GOOGLE_PLAY_SERVICES_OUTDATED }
    }

    @Test
    fun `Triggers alert if login attempt failed with missing or outdated play services`() = runTest {
        coEvery { isUserSignedInUseCase.invoke(any()) } returns IsUserSignedInUseCase.SignedInState.NOT_SIGNED_IN

        viewModel.validateSignInAndProceed(FLOW_ACTION)
        viewModel.handleLoginResult(LoginResult(false, LoginError.MissingOrOutdatedPlayServices))

        viewModel.showAlert.test().assertValue { it.peekContent() == LoginCheckError.MISSING_OR_OUTDATED_GOOGLE_PLAY_STORE_APP }
    }

    @Test
    fun `Triggers alert if login attempt failed with unknown error`() = runTest {
        coEvery { isUserSignedInUseCase.invoke(any()) } returns IsUserSignedInUseCase.SignedInState.NOT_SIGNED_IN

        viewModel.validateSignInAndProceed(FLOW_ACTION)
        viewModel.handleLoginResult(LoginResult(false, LoginError.Unknown))

        viewModel.showAlert.test().assertValue { it.peekContent() == LoginCheckError.UNEXPECTED_LOGIN_ERROR }
    }

    @Test
    fun `Correctly handles successful login attempt`() = runTest {
        coEvery { isUserSignedInUseCase.invoke(any()) } returns IsUserSignedInUseCase.SignedInState.NOT_SIGNED_IN

        viewModel.validateSignInAndProceed(FLOW_ACTION)
        viewModel.handleLoginResult(LoginResult(true, null))

        verify { getProjectStateUseCase.invoke() }
    }

    @Test
    fun `Correctly handles signed in users`() = runTest {
        coEvery { isUserSignedInUseCase.invoke(any()) } returns IsUserSignedInUseCase.SignedInState.SIGNED_IN

        viewModel.validateSignInAndProceed(FLOW_ACTION)

        verify { getProjectStateUseCase.invoke() }
    }

    @Test
    fun `Triggers alert if project is paused`() = runTest {
        coEvery { isUserSignedInUseCase.invoke(any()) } returns IsUserSignedInUseCase.SignedInState.SIGNED_IN
        coEvery { getProjectStateUseCase.invoke() } returns GetProjectStateUseCase.ProjectState.PAUSED

        viewModel.validateSignInAndProceed(FLOW_ACTION)

        viewModel.showAlert.test().assertValue { it.peekContent() == LoginCheckError.PROJECT_PAUSED }
    }

    @Test
    fun `Triggers alert if project is ending`() = runTest {
        coEvery { isUserSignedInUseCase.invoke(any()) } returns IsUserSignedInUseCase.SignedInState.SIGNED_IN
        coEvery { getProjectStateUseCase.invoke() } returns GetProjectStateUseCase.ProjectState.ENDING

        viewModel.validateSignInAndProceed(FLOW_ACTION)

        viewModel.showAlert.test().assertValue { it.peekContent() == LoginCheckError.PROJECT_ENDING }
    }

    @Test
    fun `Triggers login attempt if project has ended`() = runTest {
        coEvery { isUserSignedInUseCase.invoke(any()) } returns IsUserSignedInUseCase.SignedInState.SIGNED_IN
        coEvery { getProjectStateUseCase.invoke() } returns GetProjectStateUseCase.ProjectState.ENDED

        viewModel.validateSignInAndProceed(FLOW_ACTION)

        viewModel.showLoginFlow.test().assertValue { it.peekContent() == FLOW_ACTION }
    }

    @Test
    fun `Correctly handles if user signed in active project`() = runTest {
        coEvery { isUserSignedInUseCase.invoke(any()) } returns IsUserSignedInUseCase.SignedInState.SIGNED_IN
        coEvery { getProjectStateUseCase.invoke() } returns GetProjectStateUseCase.ProjectState.ACTIVE

        viewModel.validateSignInAndProceed(FLOW_ACTION)

        coVerify {
            updateProjectStateUseCase.invoke()
            updateDatabaseCountsInCurrentSessionUseCase.invoke()
            addAuthorizationEventUseCase.invoke(any(), eq(true))
            extractCrashKeysUseCase.invoke(any())
        }

        viewModel.proceedWithAction.test().assertValue { it.peekContent() == FLOW_ACTION }
    }


    companion object {
        private val FLOW_ACTION = EnrolActionFactory.getValidSimprintsRequest()
        private val FOLLOWUP_ACTON = ConfirmIdentityActionFactory.getValidSimprintsRequest()
    }

}
