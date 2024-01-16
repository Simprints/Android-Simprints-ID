package com.simprints.feature.clientapi

import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jraska.livedata.test
import com.simprints.feature.clientapi.exceptions.InvalidRequestException
import com.simprints.feature.clientapi.mappers.request.IntentToActionMapper
import com.simprints.feature.clientapi.mappers.response.ActionToIntentMapper
import com.simprints.feature.clientapi.usecases.CreateSessionIfRequiredUseCase
import com.simprints.feature.clientapi.usecases.DeleteSessionEventsIfNeededUseCase
import com.simprints.feature.clientapi.usecases.GetCurrentSessionIdUseCase
import com.simprints.feature.clientapi.usecases.GetEnrolmentCreationEventForSubjectUseCase
import com.simprints.feature.clientapi.usecases.GetEventsForCoSyncUseCase
import com.simprints.feature.clientapi.usecases.IsFlowCompletedWithErrorUseCase
import com.simprints.feature.clientapi.usecases.SimpleEventReporter
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.orchestration.data.ActionRequest
import com.simprints.infra.orchestration.data.ActionRequestIdentifier
import com.simprints.infra.orchestration.data.ActionResponse
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ClientApiViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineRule = TestCoroutineRule()

    @MockK
    lateinit var intentMapper: IntentToActionMapper

    @MockK
    lateinit var resultMapper: ActionToIntentMapper

    @MockK
    lateinit var simpleEventReporter: SimpleEventReporter

    @MockK
    lateinit var getCurrentSessionId: GetCurrentSessionIdUseCase

    @MockK
    lateinit var createSessionIfRequiredUseCase: CreateSessionIfRequiredUseCase

    @MockK
    lateinit var getEventJsonForSession: GetEventsForCoSyncUseCase

    @MockK
    lateinit var getEnrolmentCreationEventForSubject: GetEnrolmentCreationEventForSubjectUseCase

    @MockK
    lateinit var deleteSessionEventsIfNeeded: DeleteSessionEventsIfNeededUseCase

    @MockK
    lateinit var isFlowCompletedWithError: IsFlowCompletedWithErrorUseCase

    @MockK
    lateinit var authStore: AuthStore

    @MockK
    lateinit var configRepository: ConfigRepository

    private lateinit var viewModel: ClientApiViewModel


    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        coEvery { getCurrentSessionId.invoke() } returns "sessionId"
        coEvery { getEventJsonForSession.invoke(any(), any()) } returns "eventsJson"
        coEvery { getEnrolmentCreationEventForSubject.invoke(any(), any()) } returns "recordsJson"
        every { resultMapper.invoke(any()) } returns mockk()
        every { isFlowCompletedWithError.invoke(any()) } returns false
        coEvery { deleteSessionEventsIfNeeded.invoke(any()) } returns mockk()

        viewModel = ClientApiViewModel(
            intentMapper = intentMapper,
            resultMapper = resultMapper,
            simpleEventReporter = simpleEventReporter,
            getCurrentSessionId = getCurrentSessionId,
            createSessionIfRequiredUseCase = createSessionIfRequiredUseCase,
            getEventJsonForSession = getEventJsonForSession,
            getEnrolmentCreationEventForSubject = getEnrolmentCreationEventForSubject,
            deleteSessionEventsIfNeeded = deleteSessionEventsIfNeeded,
            isFlowCompletedWithError = isFlowCompletedWithError,
            authStore = authStore,
            configRepository = configRepository
        )
    }

    @Test
    fun `handleIntent tries creating session when called`() = runTest {
        coEvery { createSessionIfRequiredUseCase.invoke(any()) } returns true
        coEvery {
            intentMapper.invoke(
                action = any(),
                extras = any(),
                project = any()
            )
        } returns mockk()

        viewModel.handleIntent("action", Bundle())

        coVerify { createSessionIfRequiredUseCase("action") }
        viewModel.newSessionCreated.test().assertHasValue()
    }

    @Test
    fun `handleIntent handles invalid intent`() = runTest {
        coEvery { createSessionIfRequiredUseCase.invoke(any()) } returns false
        coEvery {
            intentMapper.invoke(
                action = any(),
                extras = any(),
                project = any()
            )
        } throws InvalidRequestException("Invalid intent")

        viewModel.handleIntent("action", Bundle())

        verify { simpleEventReporter.addInvalidIntentEvent("action", any()) }
        viewModel.showAlert.test().assertHasValue()
    }

    @Test
    fun `handleEnrolResponse saves correct events`() = runTest {
        viewModel.handleEnrolResponse(
            mockRequest(),
            mockk { every { guid } returns "guid" }
        )

        coVerify {
            simpleEventReporter.addCompletionCheckEvent(eq(true))
            simpleEventReporter.closeCurrentSessionNormally()
            getEventJsonForSession(sessionId = any(), project = any())
            deleteSessionEventsIfNeeded(any())
        }
        verify { resultMapper.invoke(withArg { it is ActionResponse.EnrolActionResponse }) }
        viewModel.returnResponse.test().assertHasValue()
    }

    @Test
    fun `handleIdentifyResponse saves correct events`() = runTest {
        viewModel.handleIdentifyResponse(
            mockRequest(),
            mockk { every { identifications } returns emptyList() }
        )

        coVerify { simpleEventReporter.addCompletionCheckEvent(eq(true)) }
        verify { resultMapper.invoke(withArg { it is ActionResponse.IdentifyActionResponse }) }
        viewModel.returnResponse.test().assertHasValue()
    }

    @Test
    fun `handleConfirmResponse saves correct events`() = runTest {
        viewModel.handleConfirmResponse(
            mockRequest(),
            mockk { every { identificationOutcome } returns true }
        )

        coVerify {
            simpleEventReporter.addCompletionCheckEvent(eq(true))
            getEventJsonForSession(sessionId = any(), project = any())
            deleteSessionEventsIfNeeded(any())
        }
        verify { resultMapper.invoke(withArg { it is ActionResponse.ConfirmActionResponse }) }
        viewModel.returnResponse.test().assertHasValue()
    }

    @Test
    fun `handleVerifyResponse saves correct events`() = runTest {
        viewModel.handleVerifyResponse(
            mockRequest(),
            mockk { every { matchResult } returns mockk() }
        )

        coVerify {
            simpleEventReporter.addCompletionCheckEvent(eq(true))
            simpleEventReporter.closeCurrentSessionNormally()
            getEventJsonForSession(sessionId = any(), project = any())
            deleteSessionEventsIfNeeded(any())
        }
        verify { resultMapper.invoke(withArg { it is ActionResponse.VerifyActionResponse }) }
        viewModel.returnResponse.test().assertHasValue()
    }

    @Test
    fun `handleExitFormResponse saves correct events`() = runTest {
        viewModel.handleExitFormResponse(
            mockRequest(),
            mockk {
                every { reason } returns ""
                every { extra } returns ""
            }
        )

        coVerify {
            simpleEventReporter.addCompletionCheckEvent(eq(true))
            simpleEventReporter.closeCurrentSessionNormally()
            getEventJsonForSession(sessionId = any(), project = any())
            deleteSessionEventsIfNeeded(any())
        }
        verify { resultMapper.invoke(withArg { it is ActionResponse.ExitFormActionResponse }) }
        viewModel.returnResponse.test().assertHasValue()
    }

    @Test
    fun `handleErrorResponse saves correct events`() = runTest {
        viewModel.handleErrorResponse(
            "action.package",
            mockk { every { reason } returns mockk() }
        )

        coVerify {
            simpleEventReporter.addCompletionCheckEvent(eq(false))
            simpleEventReporter.closeCurrentSessionNormally()
            getEventJsonForSession(sessionId = any(), project = any())
            deleteSessionEventsIfNeeded(any())
        }
        verify { resultMapper.invoke(withArg { it is ActionResponse.ErrorActionResponse }) }
        viewModel.returnResponse.test().assertHasValue()
    }

    private fun mockRequest(): ActionRequest = mockk {
        every { projectId } returns "projectId"
        every { actionIdentifier } returns ActionRequestIdentifier("action", "package")
    }
}
