package com.simprints.feature.clientapi.activity

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.jraska.livedata.test
import com.simprints.feature.clientapi.activity.usecases.ExtractCrashKeysUseCase
import com.simprints.feature.clientapi.activity.usecases.ExtractParametersForAnalyticsUseCase
import com.simprints.feature.clientapi.activity.usecases.IsFlowCompletedWithErrorUseCase
import com.simprints.feature.clientapi.activity.usecases.IsUserSignedInUseCase
import com.simprints.feature.clientapi.exceptions.InvalidRequestException
import com.simprints.feature.clientapi.mappers.request.IntentToActionMapper
import com.simprints.feature.clientapi.mappers.request.requestFactories.ConfirmIdentityActionFactory
import com.simprints.feature.clientapi.mappers.request.requestFactories.EnrolActionFactory
import com.simprints.feature.clientapi.models.ActionResponse
import com.simprints.feature.clientapi.models.ClientApiError
import com.simprints.feature.clientapi.session.ClientSessionManager
import com.simprints.feature.clientapi.session.DeleteSessionEventsIfNeededUseCase
import com.simprints.feature.clientapi.session.GetEnrolmentCreationEventForSubjectUseCase
import com.simprints.feature.clientapi.session.GetEventJsonForSessionUseCase
import com.simprints.feature.clientapi.session.GetProjectStateUseCase
import com.simprints.feature.clientapi.session.ReportActionRequestEventsUseCase
import com.simprints.feature.clientapi.session.UpdateDatabaseCountsInCurrentSessionUseCase
import com.simprints.feature.clientapi.session.UpdateProjectInCurrentSessionUseCase
import com.simprints.feature.login.LoginError
import com.simprints.feature.login.LoginResult
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


internal class ClientApiViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    lateinit var rootMatchers: SecurityManager

    @MockK
    lateinit var intentMapper: IntentToActionMapper

    @MockK
    lateinit var clientSessionManager: ClientSessionManager

    @MockK
    lateinit var reportActionRequestEventsUseCase: ReportActionRequestEventsUseCase

    @MockK
    lateinit var extractParametersForAnalyticsUseCase: ExtractParametersForAnalyticsUseCase

    @MockK
    lateinit var extractCrashKeysUseCase: ExtractCrashKeysUseCase

    @MockK
    lateinit var isUserSignedInUseCase: IsUserSignedInUseCase

    @MockK
    lateinit var getProjectStateUseCase: GetProjectStateUseCase

    @MockK
    lateinit var getEventJsonForSessionUseCase: GetEventJsonForSessionUseCase

    @MockK
    lateinit var updateDatabaseCountsInCurrentSessionUseCase: UpdateDatabaseCountsInCurrentSessionUseCase

    @MockK
    lateinit var updateProjectStateUseCase: UpdateProjectInCurrentSessionUseCase

    @MockK
    lateinit var getEnrolmentCreationEventForSubjectUseCase: GetEnrolmentCreationEventForSubjectUseCase

    @MockK
    lateinit var deleteSessionEventsIfNeededUseCase: DeleteSessionEventsIfNeededUseCase

    @MockK
    lateinit var isFlowCompletedWithErrorUseCase: IsFlowCompletedWithErrorUseCase


    private lateinit var viewModel: ClientApiViewModel


    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        viewModel = ClientApiViewModel(
            rootMatchers,
            intentMapper,
            clientSessionManager,
            reportActionRequestEventsUseCase,
            extractParametersForAnalyticsUseCase,
            extractCrashKeysUseCase,
            isUserSignedInUseCase,
            getProjectStateUseCase,
            getEventJsonForSessionUseCase,
            updateDatabaseCountsInCurrentSessionUseCase,
            updateProjectStateUseCase,
            getEnrolmentCreationEventForSubjectUseCase,
            deleteSessionEventsIfNeededUseCase,
            isFlowCompletedWithErrorUseCase
        )
    }

    @Test
    fun `Triggers alert if device is rooted`() = runTest {
        every { rootMatchers.checkIfDeviceIsRooted() } throws RootedDeviceException()

        viewModel.handleIntent(TEST_ACTION, emptyMap())

        viewModel.showAlert.test().assertValue { it.peekContent() == ClientApiError.ROOTED_DEVICE }
    }

    @Test
    fun `Triggers alert if intent mapper fails`() = runTest {
        coEvery { intentMapper.invoke(any(), any()) } throws InvalidRequestException(error = ClientApiError.INVALID_SESSION_ID)

        viewModel.handleIntent(TEST_ACTION, emptyMap())

        viewModel.showAlert.test().assertValue { it.peekContent() == ClientApiError.INVALID_SESSION_ID }
    }

    @Test
    fun `Adds invalid intent event if intent mapper fails`() = runTest {
        coEvery { intentMapper.invoke(any(), any()) } throws InvalidRequestException(error = ClientApiError.INVALID_SESSION_ID)

        viewModel.handleIntent(TEST_ACTION, emptyMap())

        verify { clientSessionManager.addInvalidIntentEvent(any(), any()) }
    }

    @Test
    fun `Reports action event and extracts analytics for valid action`() = runTest {
        coEvery { intentMapper.invoke(any(), any()) } returns FLOW_ACTION

        viewModel.handleIntent(TEST_ACTION, emptyMap())

        coVerify {
            reportActionRequestEventsUseCase.invoke(any())
            extractParametersForAnalyticsUseCase.invoke(any())
        }
    }

    @Test
    fun `Triggers alert if mismatched project IDs`() = runTest {
        coEvery { intentMapper.invoke(any(), any()) } returns FLOW_ACTION
        coEvery { isUserSignedInUseCase.invoke(any()) } returns IsUserSignedInUseCase.SignedInState.MISMATCHED_PROJECT_ID

        viewModel.handleIntent(TEST_ACTION, emptyMap())

        viewModel.showAlert.test().assertValue { it.peekContent() == ClientApiError.DIFFERENT_PROJECT_ID }
    }

    @Test
    fun `Returns error response if not signed in and followup action`() = runTest {
        coEvery { intentMapper.invoke(any(), any()) } returns FOLLOWUP_ACTON
        coEvery { isUserSignedInUseCase.invoke(any()) } returns IsUserSignedInUseCase.SignedInState.NOT_SIGNED_IN

        viewModel.handleIntent(TEST_ACTION, emptyMap())

        viewModel.returnResponse.test().assertValue { it.peekContent() is ActionResponse.ErrorActionResponse }
    }

    @Test
    fun `Returns error response if there are several login attempts`() = runTest {
        coEvery { intentMapper.invoke(any(), any()) } returns FLOW_ACTION
        coEvery { isUserSignedInUseCase.invoke(any()) } returns IsUserSignedInUseCase.SignedInState.NOT_SIGNED_IN

        viewModel.handleIntent(TEST_ACTION, emptyMap())
        viewModel.handleIntent(TEST_ACTION, emptyMap())

        viewModel.returnResponse.test().assertValue { it.peekContent() is ActionResponse.ErrorActionResponse }
    }

    @Test
    fun `Triggers login flow if not signed in and flow action`() = runTest {
        coEvery { intentMapper.invoke(any(), any()) } returns FLOW_ACTION
        coEvery { isUserSignedInUseCase.invoke(any()) } returns IsUserSignedInUseCase.SignedInState.NOT_SIGNED_IN

        viewModel.handleIntent(TEST_ACTION, emptyMap())

        coVerify { clientSessionManager.addAuthorizationEvent(any(), eq(false)) }
        viewModel.showLoginFlow.test().assertValue { it.peekContent() == FLOW_ACTION }
    }

    @Test
    fun `Returns error response if login attempt not complete`() = runTest {
        coEvery { intentMapper.invoke(any(), any()) } returns FLOW_ACTION
        coEvery { isUserSignedInUseCase.invoke(any()) } returns IsUserSignedInUseCase.SignedInState.NOT_SIGNED_IN

        viewModel.handleIntent(TEST_ACTION, emptyMap())
        viewModel.handleLoginResult(LoginResult(false, LoginError.LoginNotCompleted))

        viewModel.returnResponse.test().assertValue { it.peekContent() is ActionResponse.ErrorActionResponse }
    }

    @Test
    fun `Triggers alert if login if failed to cache request`() = runTest {
        coEvery { intentMapper.invoke(any(), any()) } returns FLOW_ACTION
        coEvery { isUserSignedInUseCase.invoke(any()) } returns IsUserSignedInUseCase.SignedInState.NOT_SIGNED_IN

        viewModel.handleLoginResult(LoginResult(true, null))

        viewModel.showAlert.test().assertValue { it.peekContent() == ClientApiError.UNEXPECTED_LOGIN_ERROR }
    }

    @Test
    fun `Triggers alert if login attempt failed with integrity error`() = runTest {
        coEvery { intentMapper.invoke(any(), any()) } returns FLOW_ACTION
        coEvery { isUserSignedInUseCase.invoke(any()) } returns IsUserSignedInUseCase.SignedInState.NOT_SIGNED_IN

        viewModel.handleIntent(TEST_ACTION, emptyMap())
        viewModel.handleLoginResult(LoginResult(false, LoginError.IntegrityServiceError))

        viewModel.showAlert.test().assertValue { it.peekContent() == ClientApiError.INTEGRITY_SERVICE_ERROR }
    }

    @Test
    fun `Triggers alert if login attempt failed with missing play services`() = runTest {
        coEvery { intentMapper.invoke(any(), any()) } returns FLOW_ACTION
        coEvery { isUserSignedInUseCase.invoke(any()) } returns IsUserSignedInUseCase.SignedInState.NOT_SIGNED_IN

        viewModel.handleIntent(TEST_ACTION, emptyMap())
        viewModel.handleLoginResult(LoginResult(false, LoginError.MissingPlayServices))

        viewModel.showAlert.test().assertValue { it.peekContent() == ClientApiError.MISSING_GOOGLE_PLAY_SERVICES }
    }

    @Test
    fun `Triggers alert if login attempt failed with outdated play services`() = runTest {
        coEvery { intentMapper.invoke(any(), any()) } returns FLOW_ACTION
        coEvery { isUserSignedInUseCase.invoke(any()) } returns IsUserSignedInUseCase.SignedInState.NOT_SIGNED_IN

        viewModel.handleIntent(TEST_ACTION, emptyMap())
        viewModel.handleLoginResult(LoginResult(false, LoginError.OutdatedPlayServices))

        viewModel.showAlert.test().assertValue { it.peekContent() == ClientApiError.GOOGLE_PLAY_SERVICES_OUTDATED }
    }

    @Test
    fun `Triggers alert if login attempt failed with missing or outdated play services`() = runTest {
        coEvery { intentMapper.invoke(any(), any()) } returns FLOW_ACTION
        coEvery { isUserSignedInUseCase.invoke(any()) } returns IsUserSignedInUseCase.SignedInState.NOT_SIGNED_IN

        viewModel.handleIntent(TEST_ACTION, emptyMap())
        viewModel.handleLoginResult(LoginResult(false, LoginError.MissingOrOutdatedPlayServices))

        viewModel.showAlert.test().assertValue { it.peekContent() == ClientApiError.MISSING_OR_OUTDATED_GOOGLE_PLAY_STORE_APP }
    }

    @Test
    fun `Triggers alert if login attempt failed with unknown error`() = runTest {
        coEvery { intentMapper.invoke(any(), any()) } returns FLOW_ACTION
        coEvery { isUserSignedInUseCase.invoke(any()) } returns IsUserSignedInUseCase.SignedInState.NOT_SIGNED_IN

        viewModel.handleIntent(TEST_ACTION, emptyMap())
        viewModel.handleLoginResult(LoginResult(false, LoginError.Unknown))

        viewModel.showAlert.test().assertValue { it.peekContent() == ClientApiError.UNEXPECTED_LOGIN_ERROR }
    }

    @Test
    fun `Correctly handles successful login attempt`() = runTest {
        coEvery { intentMapper.invoke(any(), any()) } returns FLOW_ACTION
        coEvery { isUserSignedInUseCase.invoke(any()) } returns IsUserSignedInUseCase.SignedInState.NOT_SIGNED_IN

        viewModel.handleIntent(TEST_ACTION, emptyMap())
        viewModel.handleLoginResult(LoginResult(true, null))

        verify { getProjectStateUseCase.invoke() }
    }

    @Test
    fun `Correctly handles signed in users`() = runTest {
        coEvery { intentMapper.invoke(any(), any()) } returns FLOW_ACTION
        coEvery { isUserSignedInUseCase.invoke(any()) } returns IsUserSignedInUseCase.SignedInState.SIGNED_IN

        viewModel.handleIntent(TEST_ACTION, emptyMap())

        verify { getProjectStateUseCase.invoke() }
    }

    @Test
    fun `Triggers alert if project is paused`() = runTest {
        coEvery { intentMapper.invoke(any(), any()) } returns FLOW_ACTION
        coEvery { isUserSignedInUseCase.invoke(any()) } returns IsUserSignedInUseCase.SignedInState.SIGNED_IN
        coEvery { getProjectStateUseCase.invoke() } returns GetProjectStateUseCase.ProjectState.PAUSED

        viewModel.handleIntent(TEST_ACTION, emptyMap())

        viewModel.showAlert.test().assertValue { it.peekContent() == ClientApiError.PROJECT_PAUSED }
    }

    @Test
    fun `Triggers alert if project is ending`() = runTest {
        coEvery { intentMapper.invoke(any(), any()) } returns FLOW_ACTION
        coEvery { isUserSignedInUseCase.invoke(any()) } returns IsUserSignedInUseCase.SignedInState.SIGNED_IN
        coEvery { getProjectStateUseCase.invoke() } returns GetProjectStateUseCase.ProjectState.ENDING

        viewModel.handleIntent(TEST_ACTION, emptyMap())

        viewModel.showAlert.test().assertValue { it.peekContent() == ClientApiError.PROJECT_ENDING }
    }

    @Test
    fun `Triggers login attempt if project has ended`() = runTest {
        coEvery { intentMapper.invoke(any(), any()) } returns FLOW_ACTION
        coEvery { isUserSignedInUseCase.invoke(any()) } returns IsUserSignedInUseCase.SignedInState.SIGNED_IN
        coEvery { getProjectStateUseCase.invoke() } returns GetProjectStateUseCase.ProjectState.ENDED

        viewModel.handleIntent(TEST_ACTION, emptyMap())

        viewModel.showLoginFlow.test().assertValue { it.peekContent() == FLOW_ACTION }
    }

    @Test
    fun `Correctly handles if user signed in active project`() = runTest {
        coEvery { intentMapper.invoke(any(), any()) } returns FLOW_ACTION
        coEvery { isUserSignedInUseCase.invoke(any()) } returns IsUserSignedInUseCase.SignedInState.SIGNED_IN
        coEvery { getProjectStateUseCase.invoke() } returns GetProjectStateUseCase.ProjectState.ACTIVE

        viewModel.handleIntent(TEST_ACTION, emptyMap())

        coVerify {
            updateProjectStateUseCase.invoke()
            updateDatabaseCountsInCurrentSessionUseCase.invoke()
            clientSessionManager.addAuthorizationEvent(any(), eq(true))
            extractCrashKeysUseCase.invoke(any())
        }

        viewModel.proceedWithAction.test().assertValue { it.peekContent() == FLOW_ACTION }
    }


    companion object {
        private const val TEST_ACTION = "com.simprints.id.REGISTER"

        private val FLOW_ACTION = EnrolActionFactory.getValidSimprintsRequest()
        private val FOLLOWUP_ACTON = ConfirmIdentityActionFactory.getValidSimprintsRequest()
    }

}
