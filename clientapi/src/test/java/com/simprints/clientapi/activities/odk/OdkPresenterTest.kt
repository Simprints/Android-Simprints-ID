package com.simprints.clientapi.activities.odk

import com.simprints.clientapi.activities.odk.OdkAction.*
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.controllers.core.eventData.model.IntegrationInfo.ODK
import com.simprints.clientapi.domain.responses.EnrollResponse
import com.simprints.clientapi.domain.responses.ErrorResponse
import com.simprints.clientapi.domain.responses.IdentifyResponse
import com.simprints.clientapi.domain.responses.VerifyResponse
import com.simprints.clientapi.domain.responses.entities.MatchResult
import com.simprints.clientapi.domain.responses.entities.Tier.TIER_1
import com.simprints.clientapi.domain.responses.entities.Tier.TIER_5
import com.simprints.clientapi.requestFactories.ConfirmIdentityFactory
import com.simprints.clientapi.requestFactories.EnrollRequestFactory
import com.simprints.clientapi.requestFactories.IdentifyRequestFactory
import com.simprints.clientapi.requestFactories.VerifyRequestFactory
import com.simprints.testtools.unit.BaseUnitTestConfig
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import java.util.*

class OdkPresenterTest {

    private val view = mockk<OdkActivity>(relaxed = true)

    @Before
    fun setup() {
        BaseUnitTestConfig()
            .rescheduleRxMainThread()
            .coroutinesMainThread()
    }

    @Test
    fun startPresenterForRegister_ShouldRequestRegister() {
        val enrollmentExtractor = EnrollRequestFactory.getMockExtractor()
        every { view.enrollExtractor } returns enrollmentExtractor

        OdkPresenter(view, Register, mockSessionManagerToCreateSession(), mockk(), mockk()).apply {
            runBlocking { start() }
        }

        verify(exactly = 1) { view.sendSimprintsRequest(EnrollRequestFactory.getValidSimprintsRequest(ODK)) }
    }

    @Test
    fun startPresenterForIdentify_ShouldRequestIdentify() {
        val identificationExtractor = IdentifyRequestFactory.getMockExtractor()
        every { view.identifyExtractor } returns identificationExtractor

        OdkPresenter(view, Identify, mockSessionManagerToCreateSession(), mockk(), mockk()).apply {
            runBlocking { start() }
        }

        verify(exactly = 1) { view.sendSimprintsRequest(IdentifyRequestFactory.getValidSimprintsRequest(ODK)) }
    }

    @Test
    fun startPresenterForVerify_ShouldRequestVerify() {
        val verifyExractor = VerifyRequestFactory.getMockExtractor()
        every { view.verifyExtractor } returns verifyExractor

        OdkPresenter(view, Verify, mockSessionManagerToCreateSession(), mockk(), mockk()).apply {
            runBlocking { start() }
        }

        verify(exactly = 1) { view.sendSimprintsRequest(VerifyRequestFactory.getValidSimprintsRequest(ODK)) }
    }

    @Test
    fun startPresenterWithGarbage_ShouldReturnActionError() {
        OdkPresenter(view, Invalid, mockSessionManagerToCreateSession(), mockk(), mockk()).apply {
            runBlocking { start() }
        }
        verify(exactly = 1) { view.handleClientRequestError(any()) }
    }

    @Test
    fun handleRegistration_ShouldReturnValidOdkRegistration() {
        runBlocking {
            val registerId = UUID.randomUUID().toString()
            val sessionId = UUID.randomUUID().toString()

            val sessionEventsManagerMock = mockk<ClientApiSessionEventsManager>()
            coEvery { sessionEventsManagerMock.getCurrentSessionId() } returns sessionId
            OdkPresenter(view, Register, sessionEventsManagerMock, mockk(), mockk()).apply {
                handleEnrollResponse(EnrollResponse(registerId))
            }

            verify(exactly = 1) { view.returnRegistration(registerId, sessionId, RETURN_FOR_FLOW_COMPLETED_CHECK) }
        }
    }

    @Test
    fun handleIdentification_ShouldReturnValidOdkIdentification() {
        val id1 = MatchResult(UUID.randomUUID().toString(), 100, TIER_1)
        val id2 = MatchResult(UUID.randomUUID().toString(), 15, TIER_5)
        val sessionId = UUID.randomUUID().toString()

        OdkPresenter(view, Identify, mockk(), mockk(), mockk()).apply {
            handleIdentifyResponse(IdentifyResponse(arrayListOf(id1, id2), sessionId))
        }

        verify(exactly = 1) {
            view.returnIdentification(
                idList = "${id1.guidFound} ${id2.guidFound}",
                confidenceList = "${id1.confidence} ${id2.confidence}",
                tierList = "${id1.tier} ${id2.tier}",
                sessionId = sessionId,
                flowCompletedCheck = RETURN_FOR_FLOW_COMPLETED_CHECK)
        }
    }

    @Test
    fun handleVerification_ShouldReturnValidOdkVerification() {
        val verification = VerifyResponse(MatchResult(UUID.randomUUID().toString(), 100, TIER_1))
        val sessionId = UUID.randomUUID().toString()

        val sessionEventsManagerMock = mockk<ClientApiSessionEventsManager>()
        coEvery { sessionEventsManagerMock.getCurrentSessionId() } returns sessionId

        OdkPresenter(view, Identify, sessionEventsManagerMock, mockk(), mockk()).apply {
            handleVerifyResponse(verification)
        }

        verify(exactly = 1) {
            view.returnVerification(
                id = verification.matchResult.guidFound,
                confidence = verification.matchResult.confidence.toString(),
                tier = verification.matchResult.tier.toString(),
                sessionId = sessionId,
                flowCompletedCheck = RETURN_FOR_FLOW_COMPLETED_CHECK)
        }
    }

    @Test
    fun handleResponseError_ShouldCallActionError() {
        val error = ErrorResponse(ErrorResponse.Reason.INVALID_USER_ID)
        val sessionId = UUID.randomUUID().toString()
        val sessionEventsManagerMock = mockk<ClientApiSessionEventsManager>()
        coEvery { sessionEventsManagerMock.getCurrentSessionId() } returns sessionId

        OdkPresenter(view, Invalid, sessionEventsManagerMock, mockk(), mockk()).handleResponseError(error)

        verify(exactly = 1) { view.returnErrorToClient(eq(error), eq(RETURN_FOR_FLOW_COMPLETED_CHECK), eq(sessionId)) }
    }

    @Test
    fun startPresenterForConfirmIdentify_ShouldRequestConfirmIdentify() {
        val confirmIdentify = ConfirmIdentityFactory.getMockExtractor()
        every { view.confirmIdentityExtractor } returns confirmIdentify

        OdkPresenter(view, ConfirmIdentity, mockSessionManagerToCreateSession(), mockk(), mockk()).apply {
            runBlocking { start() }
        }

        verify(exactly = 1) { view.sendSimprintsRequest(ConfirmIdentityFactory.getValidSimprintsRequest(ODK)) }
    }

    private fun mockSessionManagerToCreateSession() = mockk<ClientApiSessionEventsManager>().apply {
        coEvery { this@apply.createSession(any()) } returns "session_id"
    }

    companion object {
        internal const val RETURN_FOR_FLOW_COMPLETED_CHECK = true
    }

}
