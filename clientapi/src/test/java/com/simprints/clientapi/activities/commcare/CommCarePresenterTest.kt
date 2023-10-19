package com.simprints.clientapi.activities.commcare

import com.google.common.truth.Truth
import com.simprints.clientapi.activities.commcare.CommCareAction.*
import com.simprints.clientapi.activities.commcare.CommCareAction.CommCareActionFollowUpAction.ConfirmIdentity
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.controllers.core.eventData.model.IntegrationInfo
import com.simprints.clientapi.data.sharedpreferences.SharedPreferencesManager
import com.simprints.clientapi.domain.responses.*
import com.simprints.clientapi.domain.responses.entities.MatchConfidence
import com.simprints.clientapi.domain.responses.entities.MatchResult
import com.simprints.clientapi.domain.responses.entities.Tier
import com.simprints.clientapi.exceptions.InvalidIntentActionException
import com.simprints.clientapi.requestFactories.*
import com.simprints.clientapi.requestFactories.RequestFactory.Companion.MOCK_SESSION_ID
import com.simprints.core.tools.json.JsonHelper
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.models.UpSynchronizationConfiguration.CoSyncUpSynchronizationConfiguration
import com.simprints.infra.config.store.models.UpSynchronizationConfiguration.UpSynchronizationKind.NONE
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.libsimprints.Constants
import com.simprints.testtools.unit.BaseUnitTestConfig
import io.kotest.assertions.throwables.shouldThrow
import io.mockk.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.*

class CommCarePresenterTest {

    companion object {
        private val INTEGRATION_INFO = IntegrationInfo.COMMCARE
        const val RETURN_FOR_FLOW_COMPLETED_CHECK = true
    }

    private val project: Project = mockk()
    private val tokenizationProcessorMock: TokenizationProcessor = mockk {
        every { encrypt(RequestFactory.MOCK_USER_ID, TokenKeyType.AttendantId, project) } returns RequestFactory.MOCK_USER_ID
        every { encrypt(RequestFactory.MOCK_MODULE_ID, TokenKeyType.ModuleId, project) } returns RequestFactory.MOCK_MODULE_ID
    }
    private val view = mockk<CommCareActivity> {
        coEvery { getProject() } returns project
        every { tokenizationProcessor } returns tokenizationProcessorMock
    }
    private val jsonHelper = JsonHelper
    private val configManager = mockk<ConfigManager> {
        coEvery { getProjectConfiguration() } returns mockk {
            every { synchronization } returns mockk {
                every { up } returns mockk {
                    every { coSync } returns CoSyncUpSynchronizationConfiguration(NONE)
                }
            }
        }
    }

    @Before
    fun setup() {
        BaseUnitTestConfig().rescheduleRxMainThread().coroutinesMainThread()
    }

    @Test
    fun startPresenterForRegister_ShouldRequestRegister() {
        val enrolmentExtractor = EnrolRequestFactory.getMockExtractor()
        every { view.enrolExtractor } returns enrolmentExtractor

        getNewPresenter(Enrol, mockSessionManagerToCreateSession()).apply {
            runBlocking { start() }
        }

        verify(exactly = 1) {
            view.sendSimprintsRequest(
                EnrolRequestFactory.getValidSimprintsRequest(
                    INTEGRATION_INFO
                )
            )
        }
    }

    @Test
    fun startPresenterForIdentify_ShouldRequestIdentify() {
        val identifyExtractor = IdentifyRequestFactory.getMockExtractor()
        every { view.identifyExtractor } returns identifyExtractor

        getNewPresenter(Identify, mockSessionManagerToCreateSession()).apply {
            runBlocking { start() }
        }

        verify(exactly = 1) {
            view.sendSimprintsRequest(
                IdentifyRequestFactory.getValidSimprintsRequest(
                    INTEGRATION_INFO
                )
            )
        }
    }

    @Test
    fun startPresenterForVerify_ShouldRequestVerify() {
        val verificationExtractor = VerifyRequestFactory.getMockExtractor()
        every { view.verifyExtractor } returns verificationExtractor

        getNewPresenter(Verify, mockSessionManagerToCreateSession()).apply {
            runBlocking { start() }
        }

        verify(exactly = 1) {
            view.sendSimprintsRequest(
                VerifyRequestFactory.getValidSimprintsRequest(
                    INTEGRATION_INFO
                )
            )
        }
    }

    @Test
    fun startPresenterForConfirmIdentify_ShouldRequestConfirmIdentify() {
        val confirmIdentify = ConfirmIdentityFactory.getMockExtractor()
        every { view.confirmIdentityExtractor } returns confirmIdentify
        every { view.extras } returns mapOf(Pair(Constants.SIMPRINTS_SESSION_ID, MOCK_SESSION_ID))
        val sessionEventsManager = mockSessionManagerToCreateSession().also {
            coEvery { it.isSessionHasIdentificationCallback(any()) } returns true
            coEvery { it.getCurrentSessionId() } returns MOCK_SESSION_ID
        }

        getNewPresenter(ConfirmIdentity, sessionEventsManager).apply { runBlocking { start() } }

        verify(exactly = 1) {
            view.sendSimprintsRequest(
                ConfirmIdentityFactory.getValidSimprintsRequest(
                    INTEGRATION_INFO
                )
            )
        }
    }

    @Test
    fun startPresenterWithGarbage_ShouldReturnActionError() {
        getNewPresenter(Invalid, mockSessionManagerToCreateSession()).apply {
            runBlocking {
                shouldThrow<InvalidIntentActionException> {
                    start()
                }
            }
        }
    }

    @Test
    fun `handleRegistration should return valid registration`() {
        val registerId = UUID.randomUUID().toString()
        val sessionId = UUID.randomUUID().toString()

        val sessionEventsManagerMock = mockk<ClientApiSessionEventsManager>()
        coEvery { sessionEventsManagerMock.getCurrentSessionId() } returns sessionId

        runTest {
            getNewPresenter(
                Enrol,
                sessionEventsManagerMock,
                coroutineScope = this
            ).handleEnrolResponse(EnrolResponse(registerId))
        }

        verify(exactly = 1) {
            view.returnRegistration(
                registerId,
                sessionId,
                RETURN_FOR_FLOW_COMPLETED_CHECK,
                null,
                null
            )
        }
        coVerify(exactly = 1) {
            sessionEventsManagerMock.addCompletionCheckEvent(
                RETURN_FOR_FLOW_COMPLETED_CHECK
            )
        }
        coVerify { sessionEventsManagerMock.closeCurrentSessionNormally() }
    }

    @Test
    fun `handleIdentification should return valid identification`() {
        val id1 = MatchResult(UUID.randomUUID().toString(), 100, Tier.TIER_1, MatchConfidence.HIGH)
        val id2 = MatchResult(UUID.randomUUID().toString(), 15, Tier.TIER_5, MatchConfidence.LOW)
        val idList = arrayListOf(id1, id2)
        val sessionId = UUID.randomUUID().toString()

        val sessionEventsManagerMock = mockSessionManagerToCreateSession()

        runTest {
            getNewPresenter(
                Identify,
                sessionEventsManagerMock,
                coroutineScope = this
            ).handleIdentifyResponse(
                IdentifyResponse(arrayListOf(id1, id2), sessionId)
            )
        }

        verify(exactly = 1) {
            view.returnIdentification(
                ArrayList(idList.map {
                    com.simprints.libsimprints.Identification(
                        it.guidFound,
                        it.confidenceScore,
                        com.simprints.libsimprints.Tier.valueOf(it.tier.name)
                    )
                }),
                sessionId,
                null
            )
        }
        coVerify(exactly = 0) { sessionEventsManagerMock.closeCurrentSessionNormally() }
    }

    @Test
    fun `handleVerification should return valid verification`() {
        val verification =
            VerifyResponse(
                MatchResult(
                    UUID.randomUUID().toString(),
                    100,
                    Tier.TIER_1,
                    MatchConfidence.HIGH
                )
            )
        val sessionId = UUID.randomUUID().toString()

        val sessionEventsManagerMock = mockk<ClientApiSessionEventsManager>()
        coEvery { sessionEventsManagerMock.getCurrentSessionId() } returns sessionId

        runTest {
            getNewPresenter(
                Verify,
                sessionEventsManagerMock,
                coroutineScope = this
            ).handleVerifyResponse(verification)
        }

        verify(exactly = 1) {
            view.returnVerification(
                verification.matchResult.confidenceScore,
                com.simprints.libsimprints.Tier.valueOf(verification.matchResult.tier.name),
                verification.matchResult.guidFound,
                sessionId,
                RETURN_FOR_FLOW_COMPLETED_CHECK,
                null
            )
        }
        coVerify { sessionEventsManagerMock.closeCurrentSessionNormally() }
    }

    @Test
    fun `handleResponseError should return error to client`() {
        val error = ErrorResponse(ErrorResponse.Reason.INVALID_USER_ID)
        val sessionId = UUID.randomUUID().toString()
        val sessionEventsManagerMock = mockk<ClientApiSessionEventsManager>()
        coEvery { sessionEventsManagerMock.getCurrentSessionId() } returns sessionId
        coEvery { sessionEventsManagerMock.getAllEventsForSession(sessionId) } returns flowOf()

        runTest {
            getNewPresenter(
                Invalid,
                sessionEventsManagerMock,
                coroutineScope = this
            ).handleResponseError(error)
        }

        verify(exactly = 1) {
            view.returnErrorToClient(error, RETURN_FOR_FLOW_COMPLETED_CHECK, sessionId, null)
        }
        coVerify { sessionEventsManagerMock.closeCurrentSessionNormally() }
    }

    @Test
    fun `handleRefusalResponse should return valid refusal`() {
        val sessionId = UUID.randomUUID().toString()

        val sessionEventsManagerMock = mockk<ClientApiSessionEventsManager>()
        coEvery { sessionEventsManagerMock.getCurrentSessionId() } returns sessionId

        runTest {
            getNewPresenter(Enrol, sessionEventsManagerMock, coroutineScope = this)
                .handleRefusalResponse(RefusalFormResponse("APP_NOT_WORKING", ""))
        }

        verify(exactly = 1) {
            view.returnExitForms(
                "APP_NOT_WORKING",
                "",
                sessionId,
                CommCareCoSyncPresenterTest.RETURN_FOR_FLOW_COMPLETED_CHECK,
                null
            )
        }
        coVerify(exactly = 1) {
            sessionEventsManagerMock.addCompletionCheckEvent(CommCareCoSyncPresenterTest.RETURN_FOR_FLOW_COMPLETED_CHECK)
        }
        coVerify { sessionEventsManagerMock.closeCurrentSessionNormally() }
    }

    @Test
    fun shouldNot_closeSession_whenHandling_responseFrom_enrolConfirmID_request() {
        val newSessionId = "session_id_changed"
        val enrolLastBiometricsExtractor = EnrolLastBiometricsFactory.getMockExtractor()
        every { view.enrolLastBiometricsExtractor } returns enrolLastBiometricsExtractor

        val sessionEventsManagerMock = mockk<ClientApiSessionEventsManager>()
        coEvery { sessionEventsManagerMock.getCurrentSessionId() } returns "sessionId"

        coEvery { sessionEventsManagerMock.closeCurrentSessionNormally() } answers {
            // return a new sessionId if closeSession is called
            coEvery { sessionEventsManagerMock.getCurrentSessionId() } returns newSessionId
        }

        runTest {
            getNewPresenter(ConfirmIdentity, sessionEventsManagerMock, coroutineScope = this)
                .handleConfirmationResponse(mockk())


            val sessionId = sessionEventsManagerMock.getCurrentSessionId()
            Truth.assertThat(sessionId).isNotEqualTo(newSessionId)
            coVerify(exactly = 0) { sessionEventsManagerMock.closeCurrentSessionNormally() }
        }
    }

    private fun mockSessionManagerToCreateSession() = mockk<ClientApiSessionEventsManager> {
        coEvery { getCurrentSessionId() } returns "session_id"
        coEvery { createSession(any()) } returns "session_id"
    }

    private fun mockSharedPrefs() =
        mockk<SharedPreferencesManager> {
            coEvery { peekSessionId() } returns "sessionId"
            coEvery { popSessionId() } returns "sessionId"
        }

    private fun getNewPresenter(
        action: CommCareAction,
        clientApiSessionEventsManager: ClientApiSessionEventsManager,
        coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    ): CommCarePresenter = CommCarePresenter(
        view = view,
        action = action,
        sessionEventsManager = clientApiSessionEventsManager,
        sharedPreferencesManager = mockSharedPrefs(),
        jsonHelper = jsonHelper,
        enrolmentRecordManager = mockk(),
        timeHelper = mockk(),
        tokenizationProcessor = mockk(),
        rootManager = mockk(),
        configManager = configManager,
        coroutineScope = coroutineScope
    )
}
