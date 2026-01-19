package com.simprints.feature.clientapi

import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.os.bundleOf
import androidx.test.ext.junit.runners.*
import com.jraska.livedata.test
import com.simprints.core.domain.externalcredential.ExternalCredential
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.clientapi.exceptions.InvalidRequestException
import com.simprints.feature.clientapi.mappers.request.IntentToActionMapper
import com.simprints.feature.clientapi.mappers.response.ActionToIntentMapper
import com.simprints.feature.clientapi.usecases.CreateSessionIfRequiredUseCase
import com.simprints.feature.clientapi.usecases.DeleteSessionEventsIfNeededUseCase
import com.simprints.feature.clientapi.usecases.GetCurrentSessionIdUseCase
import com.simprints.feature.clientapi.usecases.GetEnrolmentCreationEventForRecordUseCase
import com.simprints.feature.clientapi.usecases.IsFlowCompletedWithErrorUseCase
import com.simprints.feature.clientapi.usecases.SimpleEventReporter
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.orchestration.data.ActionRequest
import com.simprints.infra.orchestration.data.ActionRequestIdentifier
import com.simprints.infra.orchestration.data.ActionResponse
import com.simprints.infra.orchestration.data.responses.AppEnrolResponse
import com.simprints.logging.persistent.PersistentLogger
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
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
    lateinit var getEnrolmentCreationEventForRecord: GetEnrolmentCreationEventForRecordUseCase

    @MockK
    lateinit var deleteSessionEventsIfNeeded: DeleteSessionEventsIfNeededUseCase

    @MockK
    lateinit var isFlowCompletedWithError: IsFlowCompletedWithErrorUseCase

    @MockK
    lateinit var configRepository: ConfigRepository

    @MockK
    lateinit var timeHelper: TimeHelper

    @MockK
    lateinit var persistentLogger: PersistentLogger

    @MockK
    lateinit var tokenizationProcessor: TokenizationProcessor

    private lateinit var viewModel: ClientApiViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        coEvery { getCurrentSessionId.invoke() } returns "sessionId"
        coEvery { getEnrolmentCreationEventForRecord.invoke(any(), any()) } returns "recordsJson"
        every { resultMapper.invoke(any()) } returns mockk()
        every { isFlowCompletedWithError.invoke(any()) } returns false
        coEvery { deleteSessionEventsIfNeeded.invoke(any()) } returns mockk()
        every { timeHelper.now() } returns Timestamp(0L)
        coJustRun { persistentLogger.log(any(), any(), any(), any()) }

        viewModel = ClientApiViewModel(
            intentMapper = intentMapper,
            resultMapper = resultMapper,
            simpleEventReporter = simpleEventReporter,
            getCurrentSessionId = getCurrentSessionId,
            createSessionIfRequiredUseCase = createSessionIfRequiredUseCase,
            getEnrolmentCreationEventForRecord = getEnrolmentCreationEventForRecord,
            deleteSessionEventsIfNeeded = deleteSessionEventsIfNeeded,
            isFlowCompletedWithError = isFlowCompletedWithError,
            configRepository = configRepository,
            timeHelper = timeHelper,
            persistentLogger = persistentLogger,
            tokenizationProcessor = tokenizationProcessor,
        )
    }

    @Test
    fun `handleIntent tries creating session when called`() = runTest {
        coEvery { createSessionIfRequiredUseCase.invoke(any()) } returns true
        coEvery {
            intentMapper.invoke(
                action = any(),
                extras = any(),
                project = any(),
            )
        } returns mockk()

        viewModel.handleIntent("action", Bundle())

        coVerify { createSessionIfRequiredUseCase("action") }
        viewModel.newSessionCreated.test().assertHasValue()
    }

    @Test
    fun `handleIntent ensures that contract version is preserved`() = runTest {
        coEvery { createSessionIfRequiredUseCase.invoke(any()) } returns true
        coEvery {
            intentMapper.invoke(
                action = any(),
                extras = any(),
                project = any(),
            )
        } returns mockk()

        viewModel.handleIntent("action", bundleOf("contractVersion" to 42))

        coVerify { intentMapper.invoke(any(), match { it["contractVersion"] == 42 }, any()) }
    }

    @Test
    fun `handleIntent handles invalid intent`() = runTest {
        coEvery { createSessionIfRequiredUseCase.invoke(any()) } returns false
        coEvery {
            intentMapper.invoke(
                action = any(),
                extras = any(),
                project = any(),
            )
        } throws InvalidRequestException("Invalid intent")

        viewModel.handleIntent("action", bundleOf("contractVersion" to 42))

        verify { simpleEventReporter.addInvalidIntentEvent("action", any<Map<String, String>>()) }
        viewModel.showAlert.test().assertHasValue()
    }

    @Test
    fun `handleEnrolResponse saves correct events`() = runTest {
        viewModel.handleEnrolResponse(
            mockRequest(),
            mockk {
                every { guid } returns "guid"
                every { externalCredential } returns null
            },
        )

        coVerify {
            simpleEventReporter.addCompletionCheckEvent(eq(true))
            simpleEventReporter.closeCurrentSessionNormally()
            deleteSessionEventsIfNeeded(any())
            persistentLogger.log(any(), any(), any(), any())
        }
        verify { resultMapper.invoke(match<ActionResponse> { it is ActionResponse.EnrolActionResponse }) }
        viewModel.returnResponse.test().assertHasValue()
    }

    @Test
    fun `handleIdentifyResponse saves correct events`() = runTest {
        viewModel.handleIdentifyResponse(
            mockRequest(),
            mockk {
                every { identifications } returns emptyList()
                every { isMultiFactorIdEnabled } returns false
            },
        )

        coVerify {
            simpleEventReporter.addCompletionCheckEvent(eq(true))
            persistentLogger.log(any(), any(), any(), any())
        }
        verify { resultMapper.invoke(match<ActionResponse> { it is ActionResponse.IdentifyActionResponse }) }
        viewModel.returnResponse.test().assertHasValue()
    }

    @Test
    fun `handleConfirmResponse saves correct events`() = runTest {
        viewModel.handleConfirmResponse(
            mockRequest(),
            mockk {
                every { identificationOutcome } returns true
                every { externalCredential } returns mockk()
            },
        )

        coVerify {
            simpleEventReporter.addCompletionCheckEvent(eq(true))
            deleteSessionEventsIfNeeded(any())
            persistentLogger.log(any(), any(), any(), any())
        }
        verify { resultMapper.invoke(match<ActionResponse> { it is ActionResponse.ConfirmActionResponse }) }
        viewModel.returnResponse.test().assertHasValue()
    }

    @Test
    fun `handleVerifyResponse saves correct events`() = runTest {
        viewModel.handleVerifyResponse(
            mockRequest(),
            mockk { every { matchResult } returns mockk(relaxed = true) },
        )

        coVerify {
            simpleEventReporter.addCompletionCheckEvent(eq(true))
            simpleEventReporter.closeCurrentSessionNormally()
            deleteSessionEventsIfNeeded(any())
            persistentLogger.log(any(), any(), any(), any())
        }
        verify { resultMapper.invoke(match<ActionResponse> { it is ActionResponse.VerifyActionResponse }) }
        viewModel.returnResponse.test().assertHasValue()
    }

    @Test
    fun `handleExitFormResponse saves correct events`() = runTest {
        viewModel.handleExitFormResponse(
            mockRequest(),
            mockk {
                every { reason } returns ""
                every { extra } returns ""
            },
        )

        coVerify {
            simpleEventReporter.addCompletionCheckEvent(eq(true))
            simpleEventReporter.closeCurrentSessionNormally()
            deleteSessionEventsIfNeeded(any())
            persistentLogger.log(any(), any(), any(), any())
        }
        verify { resultMapper.invoke(match<ActionResponse> { it is ActionResponse.ExitFormActionResponse }) }
        viewModel.returnResponse.test().assertHasValue()
    }

    @Test
    fun `handleErrorResponse saves correct events`() = runTest {
        viewModel.handleErrorResponse(
            "action.package",
            mockk { every { reason } returns mockk() },
        )

        coVerify {
            simpleEventReporter.addCompletionCheckEvent(eq(false))
            simpleEventReporter.closeCurrentSessionNormally()
            deleteSessionEventsIfNeeded(any())
            persistentLogger.log(any(), any(), any(), any())
        }
        verify { resultMapper.invoke(match<ActionResponse> { it is ActionResponse.ErrorActionResponse }) }
        viewModel.returnResponse.test().assertHasValue()
    }

    @Test
    fun `handleEnrolResponse with externalCredential decrypts and includes it in response`() = runTest {
        val mockGuid = "mockGuid"
        val expectedCredentialId = "credentialId"
        val expectedType = ExternalCredentialType.NHISCard
        val credential = mockExternalCredential(expectedCredentialId, expectedType)
        val project = mockk<Project>(relaxed = true)
        setupDecryption(project, "decrypted-value".asTokenizableRaw())

        viewModel.handleEnrolResponse(mockRequest(), mockEnrolResponseWithCredential(mockGuid, credential))

        verify {
            resultMapper.invoke(
                match<ActionResponse.EnrolActionResponse> {
                    it.externalCredential?.id == expectedCredentialId &&
                        it.externalCredential?.type == expectedType
                },
            )
        }
    }

    @Test
    fun `handleEnrolResponse with externalCredential but encrypted decryption returns null credential`() = runTest {
        val mockGuid = "mockGuid"
        val expectedCredentialId = "credentialId"
        val expectedType = ExternalCredentialType.NHISCard
        val credential = mockExternalCredential(expectedCredentialId, expectedType)
        val project = mockk<Project>(relaxed = true)
        setupDecryption(project, mockk<TokenizableString.Tokenized>())

        viewModel.handleEnrolResponse(mockRequest(), mockEnrolResponseWithCredential(mockGuid, credential))

        verify {
            resultMapper.invoke(
                match<ActionResponse.EnrolActionResponse> {
                    it.externalCredential == null
                },
            )
        }
    }

    private fun mockEnrolResponseWithCredential(
        mockGuid: String,
        credential: ExternalCredential?,
    ): AppEnrolResponse = mockk {
        every { guid } returns mockGuid
        every { externalCredential } returns credential
    }

    private fun mockExternalCredential(
        mockId: String,
        mockType: ExternalCredentialType,
    ): ExternalCredential = mockk {
        every { id } returns mockId
        every { value } returns mockk()
        every { type } returns mockType
    }

    private fun setupDecryption(
        project: Project,
        returnValue: TokenizableString,
    ) {
        coEvery { configRepository.getProject() } returns project
        every {
            tokenizationProcessor.decrypt(
                encrypted = any(),
                tokenKeyType = TokenKeyType.ExternalCredential,
                project = project,
            )
        } returns returnValue
    }

    private fun mockRequest(): ActionRequest = mockk {
        every { projectId } returns "projectId"
        every { actionIdentifier } returns ActionRequestIdentifier("action", "package", "", 1, 0L)
    }
}
