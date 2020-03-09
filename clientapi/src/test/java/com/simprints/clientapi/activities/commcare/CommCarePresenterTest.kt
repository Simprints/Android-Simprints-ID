package com.simprints.clientapi.activities.commcare

import com.simprints.clientapi.activities.commcare.CommCarePresenter.Companion.ACTION_CONFIRM_IDENTITY
import com.simprints.clientapi.activities.commcare.CommCarePresenter.Companion.ACTION_IDENTIFY
import com.simprints.clientapi.activities.commcare.CommCarePresenter.Companion.ACTION_REGISTER
import com.simprints.clientapi.activities.commcare.CommCarePresenter.Companion.ACTION_VERIFY
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.controllers.core.eventData.model.IntegrationInfo
import com.simprints.clientapi.data.sharedpreferences.SharedPreferencesManager
import com.simprints.clientapi.domain.responses.EnrollResponse
import com.simprints.clientapi.domain.responses.ErrorResponse
import com.simprints.clientapi.domain.responses.IdentifyResponse
import com.simprints.clientapi.domain.responses.VerifyResponse
import com.simprints.clientapi.domain.responses.entities.MatchResult
import com.simprints.clientapi.domain.responses.entities.Tier
import com.simprints.clientapi.requestFactories.ConfirmIdentityFactory
import com.simprints.clientapi.requestFactories.EnrollRequestFactory
import com.simprints.clientapi.requestFactories.IdentifyRequestFactory
import com.simprints.clientapi.requestFactories.RequestFactory.Companion.MOCK_SESSION_ID
import com.simprints.clientapi.requestFactories.VerifyRequestFactory
import com.simprints.libsimprints.Constants
import com.simprints.testtools.unit.BaseUnitTestConfig
import io.mockk.*
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

    @Before
    fun setup() {
        BaseUnitTestConfig().rescheduleRxMainThread().coroutinesMainThread()
    }

    @Test
    fun startPresenterForRegister_ShouldRequestRegister() {
        val enrollmentExtractor = EnrollRequestFactory.getMockExtractor()
        every { view.enrollExtractor } returns enrollmentExtractor

        CommCarePresenter(
            view,
            ACTION_REGISTER,
            mockSessionManagerToCreateSession(),
            mockSharedPrefs(),
            mockk(),
            mockk()
        ).apply {
            runBlocking { start() }
        }

        verify(exactly = 1) { view.sendSimprintsRequest(EnrollRequestFactory.getValidSimprintsRequest(INTEGRATION_INFO)) }
    }

    @Test
    fun startPresenterForIdentify_ShouldRequestIdentify() {
        runBlocking {
            val identifyExtractor = IdentifyRequestFactory.getMockExtractor()
            every { view.identifyExtractor } returns identifyExtractor

            CommCarePresenter(
                view,
                ACTION_IDENTIFY,
                mockSessionManagerToCreateSession(),
                mockSharedPrefs(),
                mockk(relaxed = true),
                mockk(relaxed = true)
            ).apply {
                start()
            }

            verify(exactly = 1) { view.sendSimprintsRequest(IdentifyRequestFactory.getValidSimprintsRequest(INTEGRATION_INFO)) }
        }
    }

    @Test
    fun startPresenterForVerify_ShouldRequestVerify() {
        val verificationExtractor = VerifyRequestFactory.getMockExtractor()
        every { view.verifyExtractor } returns verificationExtractor

        CommCarePresenter(
            view,
            ACTION_VERIFY,
            mockSessionManagerToCreateSession(),
            mockSharedPrefs(),
            mockk(),
            mockk()
        ).apply { runBlocking { start() } }

        verify(exactly = 1) { view.sendSimprintsRequest(VerifyRequestFactory.getValidSimprintsRequest(INTEGRATION_INFO)) }
    }

    @Test
    fun startPresenterForConfirmIdentify_ShouldRequestConfirmIdentify() {
        val confirmIdentify = ConfirmIdentityFactory.getMockExtractor()
        every { view.confirmIdentityExtractor } returns confirmIdentify
        every { view.extras } returns mapOf(Pair(Constants.SIMPRINTS_SESSION_ID, MOCK_SESSION_ID))

        CommCarePresenter(
            view,
            ACTION_CONFIRM_IDENTITY,
            mockSessionManagerToCreateSession(),
            mockSharedPrefs(),
            mockk(),
            mockk()
        ).apply { runBlocking { start() } }

        verify(exactly = 1) { view.sendSimprintsConfirmation(ConfirmIdentityFactory.getValidSimprintsRequest(INTEGRATION_INFO)) }
    }

    @Test
    fun startPresenterWithGarbage_ShouldReturnActionError() {
        CommCarePresenter(
            view,
            "Garbage",
            mockSessionManagerToCreateSession(),
            mockSharedPrefs(),
            mockk(),
            mockk()
        ).apply { runBlocking { start() } }
        verify(exactly = 1) { view.handleClientRequestError(any()) }
    }

    @Test
    fun handleRegistration_ShouldReturnValidRegistration() {
        val registerId = UUID.randomUUID().toString()
        val sessionId = UUID.randomUUID().toString()

        val sessionEventsManagerMock = mockk<ClientApiSessionEventsManager>()
        coEvery { sessionEventsManagerMock.getCurrentSessionId() } returns sessionId

        CommCarePresenter(
            view,
            Constants.SIMPRINTS_REGISTER_INTENT,
            sessionEventsManagerMock,
            mockSharedPrefs(),
            mockk(),
            mockk()
        ).handleEnrollResponse(EnrollResponse(registerId))

        verify(exactly = 1) { view.returnRegistration(registerId, sessionId, RETURN_FOR_FLOW_COMPLETED_CHECK) }
        coVerify(exactly = 1) { sessionEventsManagerMock.addCompletionCheckEvent(RETURN_FOR_FLOW_COMPLETED_CHECK) }
    }

    @Test
    fun handleIdentification_ShouldReturnValidIdentification() {
        val id1 = MatchResult(UUID.randomUUID().toString(), 100, Tier.TIER_1)
        val id2 = MatchResult(UUID.randomUUID().toString(), 15, Tier.TIER_5)
        val idList = arrayListOf(id1, id2)
        val sessionId = UUID.randomUUID().toString()

        CommCarePresenter(
            view,
            Constants.SIMPRINTS_IDENTIFY_INTENT,
            mockk(),
            mockSharedPrefs(),
            mockk(),
            mockk()
        ).handleIdentifyResponse(
            IdentifyResponse(arrayListOf(id1, id2), sessionId))

        verify(exactly = 1) {
            view.returnIdentification(
                ArrayList(idList.map {
                    com.simprints.libsimprints.Identification(it.guidFound, it.confidence, com.simprints.libsimprints.Tier.valueOf(it.tier.name))
                }), sessionId)
        }
    }

    @Test
    fun handleVerification_ShouldReturnValidVerification() {
        val verification = VerifyResponse(MatchResult(UUID.randomUUID().toString(), 100, Tier.TIER_1))
        val sessionId = UUID.randomUUID().toString()

        val sessionEventsManagerMock = mockk<ClientApiSessionEventsManager>()
        coEvery { sessionEventsManagerMock.getCurrentSessionId() } returns sessionId

        CommCarePresenter(
            view,
            Constants.SIMPRINTS_VERIFY_INTENT,
            sessionEventsManagerMock,
            mockSharedPrefs(),
            mockk(),
            mockk()
        ).handleVerifyResponse(verification)

        verify(exactly = 1) {
            view.returnVerification(
                verification.matchResult.confidence,
                com.simprints.libsimprints.Tier.valueOf(verification.matchResult.tier.name),
                verification.matchResult.guidFound,
                sessionId,
                RETURN_FOR_FLOW_COMPLETED_CHECK)
        }
    }

    @Test
    fun handleResponseError_ShouldCallActionError() {
        val error = ErrorResponse(ErrorResponse.Reason.INVALID_USER_ID)
        val sessionId = UUID.randomUUID().toString()
        val sessionEventsManagerMock = mockk<ClientApiSessionEventsManager>()
        coEvery { sessionEventsManagerMock.getCurrentSessionId() } returns sessionId

        CommCarePresenter(
            view,
            "",
            sessionEventsManagerMock,
            mockSharedPrefs(),
            mockk(),
            mockk()
        ).handleResponseError(error)

        verify(exactly = 1) {
            view.returnErrorToClient(error, RETURN_FOR_FLOW_COMPLETED_CHECK, sessionId)
        }
    }

    private fun mockSessionManagerToCreateSession() = mockk<ClientApiSessionEventsManager>().apply {
        coEvery { this@apply.getCurrentSessionId() } returns "session_id"
        coEvery { this@apply.createSession(any()) } returns "session_id"
    }

    private fun mockSharedPrefs() = mockk<SharedPreferencesManager>().apply {
        coEvery { this@apply.peekSessionId() } returns "sessionId"
        coEvery { this@apply.popSessionId() } returns "sessionId"
    }

}
