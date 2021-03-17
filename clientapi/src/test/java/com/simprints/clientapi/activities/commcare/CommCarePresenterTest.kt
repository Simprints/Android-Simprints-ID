package com.simprints.clientapi.activities.commcare

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
import com.simprints.clientapi.requestFactories.ConfirmIdentityFactory
import com.simprints.clientapi.requestFactories.EnrolRequestFactory
import com.simprints.clientapi.requestFactories.IdentifyRequestFactory
import com.simprints.clientapi.requestFactories.RequestFactory.Companion.MOCK_SESSION_ID
import com.simprints.clientapi.requestFactories.VerifyRequestFactory
import com.simprints.core.tools.json.JsonHelper
import com.simprints.libsimprints.Constants
import com.simprints.testtools.unit.BaseUnitTestConfig
import io.kotlintest.shouldThrow
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import java.util.*

class CommCarePresenterTest {

    companion object {
        private val INTEGRATION_INFO = IntegrationInfo.COMMCARE
        const val RETURN_FOR_FLOW_COMPLETED_CHECK = true
    }

    private val view = mockk<CommCareActivity>()
    private val jsonHelper = JsonHelper()

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

        verify(exactly = 1) { view.sendSimprintsRequest(EnrolRequestFactory.getValidSimprintsRequest(INTEGRATION_INFO)) }
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

        verify(exactly = 1) { view.sendSimprintsRequest(VerifyRequestFactory.getValidSimprintsRequest(INTEGRATION_INFO)) }
    }

    @Test
    fun startPresenterForConfirmIdentify_ShouldRequestConfirmIdentify() {
        val confirmIdentify = ConfirmIdentityFactory.getMockExtractor()
        every { view.confirmIdentityExtractor } returns confirmIdentify
        every { view.extras } returns mapOf(Pair(Constants.SIMPRINTS_SESSION_ID, MOCK_SESSION_ID))

        getNewPresenter(ConfirmIdentity, mockSessionManagerToCreateSession()).apply {
            runBlocking { start() }
        }

        verify(exactly = 1) { view.sendSimprintsRequest(ConfirmIdentityFactory.getValidSimprintsRequest(INTEGRATION_INFO)) }
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

        getNewPresenter(Enrol, sessionEventsManagerMock).handleEnrolResponse(EnrolResponse(registerId))

        verify(exactly = 1) {
            view.returnRegistration(
                registerId,
                sessionId,
                RETURN_FOR_FLOW_COMPLETED_CHECK,
                null,
                null
            )
        }
        coVerify(exactly = 1) { sessionEventsManagerMock.addCompletionCheckEvent(RETURN_FOR_FLOW_COMPLETED_CHECK) }
    }

    @Test
    fun `handleIdentification should return valid identification`() {
        val id1 = MatchResult(UUID.randomUUID().toString(), 100, Tier.TIER_1, MatchConfidence.HIGH)
        val id2 = MatchResult(UUID.randomUUID().toString(), 15, Tier.TIER_5, MatchConfidence.LOW)
        val idList = arrayListOf(id1, id2)
        val sessionId = UUID.randomUUID().toString()

        getNewPresenter(Identify, mockSessionManagerToCreateSession()).handleIdentifyResponse(
            IdentifyResponse(arrayListOf(id1, id2), sessionId)
        )

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
    }

    @Test
    fun `handleVerification should return valid verification`() {
        val verification =
            VerifyResponse(MatchResult(UUID.randomUUID().toString(), 100, Tier.TIER_1, MatchConfidence.HIGH))
        val sessionId = UUID.randomUUID().toString()

        val sessionEventsManagerMock = mockk<ClientApiSessionEventsManager>()
        coEvery { sessionEventsManagerMock.getCurrentSessionId() } returns sessionId

        getNewPresenter(Verify, sessionEventsManagerMock).handleVerifyResponse(verification)

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
    }

    @Test
    fun `handleResponseError should return error to client`() {
        val error = ErrorResponse(ErrorResponse.Reason.INVALID_USER_ID)
        val sessionId = UUID.randomUUID().toString()
        val sessionEventsManagerMock = mockk<ClientApiSessionEventsManager>()
        coEvery { sessionEventsManagerMock.getCurrentSessionId() } returns sessionId
        coEvery { sessionEventsManagerMock.getAllEventsForSession(sessionId) } returns flowOf()

        getNewPresenter(Invalid, sessionEventsManagerMock).handleResponseError(error)

        verify(exactly = 1) {
            view.returnErrorToClient(error, RETURN_FOR_FLOW_COMPLETED_CHECK, sessionId, null)
        }
    }

    @Test
    fun `handleRefusalResponse should return valid refusal`() {
        val sessionId = UUID.randomUUID().toString()

        val sessionEventsManagerMock = mockk<ClientApiSessionEventsManager>()
        coEvery { sessionEventsManagerMock.getCurrentSessionId() } returns sessionId

        getNewPresenter(Enrol, sessionEventsManagerMock)
            .handleRefusalResponse(RefusalFormResponse("APP_NOT_WORKING", ""))

        verify(exactly = 1) {
            view.returnExitForms(
                "APP_NOT_WORKING",
                "",
                sessionId,
                CommCareCoSyncPresenterTest.RETURN_FOR_FLOW_COMPLETED_CHECK,
                null
            )
        }
        coVerify(exactly = 1) { sessionEventsManagerMock.addCompletionCheckEvent(CommCareCoSyncPresenterTest.RETURN_FOR_FLOW_COMPLETED_CHECK) }
    }

    private fun mockSessionManagerToCreateSession() = mockk<ClientApiSessionEventsManager>().apply {
        coEvery { this@apply.getCurrentSessionId() } returns "session_id"
        coEvery { this@apply.createSession(any()) } returns "session_id"
    }

    private fun mockSharedPrefs() = mockk<SharedPreferencesManager>().apply {
        coEvery { this@apply.peekSessionId() } returns "sessionId"
        coEvery { this@apply.popSessionId() } returns "sessionId"
    }

    private fun getNewPresenter(
        action: CommCareAction,
        clientApiSessionEventsManager: ClientApiSessionEventsManager
    ): CommCarePresenter = CommCarePresenter(
        view,
        action,
        clientApiSessionEventsManager,
        mockSharedPrefs(),
        jsonHelper,
        mockk(),
        mockk(),
        mockk(),
        mockk()
    )
}
