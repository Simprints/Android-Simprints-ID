package com.simprints.clientapi.activities.odk

import com.google.common.truth.Truth.assertThat
import com.simprints.clientapi.activities.odk.OdkAction.*
import com.simprints.clientapi.activities.odk.OdkAction.OdkActionFollowUpAction.ConfirmIdentity
import com.simprints.clientapi.activities.odk.OdkAction.OdkActionFollowUpAction.EnrolLastBiometrics
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.controllers.core.eventData.model.IntegrationInfo
import com.simprints.clientapi.controllers.core.eventData.model.IntegrationInfo.ODK
import com.simprints.clientapi.domain.responses.EnrolResponse
import com.simprints.clientapi.domain.responses.ErrorResponse
import com.simprints.clientapi.domain.responses.IdentifyResponse
import com.simprints.clientapi.domain.responses.VerifyResponse
import com.simprints.clientapi.domain.responses.entities.MatchConfidence.HIGH
import com.simprints.clientapi.domain.responses.entities.MatchConfidence.LOW
import com.simprints.clientapi.domain.responses.entities.MatchResult
import com.simprints.clientapi.domain.responses.entities.Tier.TIER_1
import com.simprints.clientapi.domain.responses.entities.Tier.TIER_5
import com.simprints.clientapi.exceptions.InvalidIntentActionException
import com.simprints.clientapi.requestFactories.*
import com.simprints.testtools.unit.BaseUnitTestConfig
import io.kotlintest.shouldThrow
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import java.util.*

class OdkPresenterTest {

    private val view = mockk<OdkActivity>(relaxed = true)

    @MockK
    lateinit var clientApiSessionEventsManager: ClientApiSessionEventsManager

    @Before
    fun setup() {
        BaseUnitTestConfig()
            .rescheduleRxMainThread()
            .coroutinesMainThread()


        MockKAnnotations.init(this, relaxed = true)
        coEvery { clientApiSessionEventsManager.isCurrentSessionAnIdentificationOrEnrolment() } returns true
        coEvery { clientApiSessionEventsManager.getCurrentSessionId() } returns RequestFactory.MOCK_SESSION_ID
        coEvery { clientApiSessionEventsManager.createSession(any()) } returns "session_id"
    }

    @Test
    fun startPresenterForRegister_ShouldRequestRegister() {
        val enrolmentExtractor = EnrolRequestFactory.getMockExtractor()
        every { view.enrolExtractor } returns enrolmentExtractor

        OdkPresenter(view, Enrol, clientApiSessionEventsManager, mockk()).apply {
            runBlocking { start() }
        }

        verify(exactly = 1) { view.sendSimprintsRequest(EnrolRequestFactory.getValidSimprintsRequest(ODK)) }
    }

    @Test
    fun startPresenterForIdentify_ShouldRequestIdentify() {
        val identificationExtractor = IdentifyRequestFactory.getMockExtractor()
        every { view.identifyExtractor } returns identificationExtractor

        OdkPresenter(view, Identify, clientApiSessionEventsManager, mockk()).apply {
            runBlocking { start() }
        }

        verify(exactly = 1) { view.sendSimprintsRequest(IdentifyRequestFactory.getValidSimprintsRequest(ODK)) }
    }

    @Test
    fun startPresenterForVerify_ShouldRequestVerify() {
        val verifyExractor = VerifyRequestFactory.getMockExtractor()
        every { view.verifyExtractor } returns verifyExractor

        OdkPresenter(view, Verify, clientApiSessionEventsManager, mockk()).apply {
            runBlocking { start() }
        }

        verify(exactly = 1) { view.sendSimprintsRequest(VerifyRequestFactory.getValidSimprintsRequest(ODK)) }
    }

    @Test
    fun startPresenterWithGarbage_ShouldReturnActionError() {
        OdkPresenter(view, Invalid, clientApiSessionEventsManager, mockk()).apply {
            runBlocking {
                shouldThrow<InvalidIntentActionException> {
                    start()
                }
            }
        }
    }

    @Test
    fun handleRegistration_ShouldReturnValidOdkRegistration() {
        val registerId = UUID.randomUUID().toString()
        val sessionId = UUID.randomUUID().toString()

        val sessionEventsManagerMock = mockk<ClientApiSessionEventsManager>()
        coEvery { sessionEventsManagerMock.getCurrentSessionId() } returns sessionId
        OdkPresenter(view, Enrol, sessionEventsManagerMock, mockk()).apply {
            handleEnrolResponse(EnrolResponse(registerId))
        }

        verify(exactly = 1) { view.returnRegistration(registerId, sessionId, RETURN_FOR_FLOW_COMPLETED_CHECK) }
        coVerify { sessionEventsManagerMock.closeCurrentSessionNormally() }
    }

    @Test
    fun handleIdentification_ShouldReturnValidOdkIdentification() {
        val id1 = MatchResult(UUID.randomUUID().toString(), 100, TIER_1, HIGH)
        val id2 = MatchResult(UUID.randomUUID().toString(), 15, TIER_5, LOW)
        val highestMatchConfidence = HIGH.toString()
        val sessionId = UUID.randomUUID().toString()
        val sessionEventsManagerMock = mockk<ClientApiSessionEventsManager>()

        OdkPresenter(view, Identify, sessionEventsManagerMock, mockk()).apply {
            handleIdentifyResponse(IdentifyResponse(arrayListOf(id1, id2), sessionId))
        }

        verify(exactly = 1) {
            view.returnIdentification(
                idList = "${id1.guidFound} ${id2.guidFound}",
                confidenceScoresList = "${id1.confidenceScore} ${id2.confidenceScore}",
                tierList = "${id1.tier} ${id2.tier}",
                sessionId = sessionId,
                matchConfidencesList = "${id1.matchConfidence} ${id2.matchConfidence}",
                highestMatchConfidence = highestMatchConfidence,
                flowCompletedCheck = RETURN_FOR_FLOW_COMPLETED_CHECK
            )
        }
        coVerify(exactly = 0) { sessionEventsManagerMock.closeCurrentSessionNormally() }
    }

    @Test
    fun handleVerification_ShouldReturnValidOdkVerification() {
        val verification = VerifyResponse(MatchResult(UUID.randomUUID().toString(), 100, TIER_1, HIGH))
        val sessionId = UUID.randomUUID().toString()

        val sessionEventsManagerMock = mockk<ClientApiSessionEventsManager>()
        coEvery { sessionEventsManagerMock.getCurrentSessionId() } returns sessionId

        OdkPresenter(view, Identify, sessionEventsManagerMock, mockk()).apply {
            handleVerifyResponse(verification)
        }

        verify(exactly = 1) {
            view.returnVerification(
                id = verification.matchResult.guidFound,
                confidence = verification.matchResult.confidenceScore.toString(),
                tier = verification.matchResult.tier.toString(),
                sessionId = sessionId,
                flowCompletedCheck = RETURN_FOR_FLOW_COMPLETED_CHECK
            )
        }
        coVerify { sessionEventsManagerMock.closeCurrentSessionNormally() }
    }

    @Test
    fun handleResponseError_ShouldCallActionError() {
        val error = ErrorResponse(ErrorResponse.Reason.INVALID_USER_ID)
        val sessionId = UUID.randomUUID().toString()
        val sessionEventsManagerMock = mockk<ClientApiSessionEventsManager>()
        coEvery { sessionEventsManagerMock.getCurrentSessionId() } returns sessionId

        OdkPresenter(view, Invalid, sessionEventsManagerMock, mockk()).handleResponseError(error)

        verify(exactly = 1) { view.returnErrorToClient(eq(error), eq(RETURN_FOR_FLOW_COMPLETED_CHECK), eq(sessionId)) }
        coVerify { sessionEventsManagerMock.closeCurrentSessionNormally() }
    }

    @Test
    fun startPresenterForConfirmIdentify_ShouldRequestConfirmIdentify() {
        val confirmIdentify = ConfirmIdentityFactory.getMockExtractor()
        every { view.confirmIdentityExtractor } returns confirmIdentify

        OdkPresenter(view, ConfirmIdentity, clientApiSessionEventsManager, mockk()).apply {
            runBlocking { start() }
        }

        verify(exactly = 1) { view.sendSimprintsRequest(ConfirmIdentityFactory.getValidSimprintsRequest(ODK)) }
    }

    @Test
    fun startPresenterForEnrolLastBiometrics_ShouldRequestEnrolLastBiometrics() {
        val enrolLastBiometricsExtractor = EnrolLastBiometricsFactory.getMockExtractor()
        every { view.enrolLastBiometricsExtractor } returns enrolLastBiometricsExtractor

        OdkPresenter(
            view,
            EnrolLastBiometrics,
            clientApiSessionEventsManager,
            mockk()
        ).apply { runBlocking { start() } }

        verify(exactly = 1) {
            view.sendSimprintsRequest(
                EnrolLastBiometricsFactory.getValidSimprintsRequest(
                    IntegrationInfo.ODK
                )
            )
        }
    }

    @Test
    fun shouldNot_closeSession_whenHandling_responseFrom_confirmID_request() {
        val newSessionId = "session_id_changed"
        val confirmIdentify = ConfirmIdentityFactory.getMockExtractor()
        every { view.confirmIdentityExtractor } returns confirmIdentify

        coEvery { clientApiSessionEventsManager.closeCurrentSessionNormally() } answers  {
            // return a new sessionId if closeSession is called
            coEvery { clientApiSessionEventsManager.getCurrentSessionId() } returns newSessionId
        }


        OdkPresenter(view, ConfirmIdentity, clientApiSessionEventsManager, mockk()).apply {
            runBlocking {
              handleConfirmationResponse(mockk())
            }
        }


        runBlocking {
            val sessionId = clientApiSessionEventsManager.getCurrentSessionId()
            assertThat(sessionId).isNotEqualTo(newSessionId)
            coVerify(exactly = 0) { clientApiSessionEventsManager.closeCurrentSessionNormally() }
        }
    }

    @Test
    fun shouldNot_closeSession_whenHandling_responseFrom_enrolLastBiometrics_request() {
        val newSessionId = "session_id_changed"
        val enrolLastBiometricsExtractor = EnrolLastBiometricsFactory.getMockExtractor()
        every { view.enrolLastBiometricsExtractor } returns enrolLastBiometricsExtractor

        coEvery { clientApiSessionEventsManager.closeCurrentSessionNormally() } answers  {
            // return a new sessionId if closeSession is called
            coEvery { clientApiSessionEventsManager.getCurrentSessionId() } returns newSessionId
        }


        OdkPresenter(
            view,
            EnrolLastBiometrics,
            clientApiSessionEventsManager,
            mockk(),
        ).apply { runBlocking { handleEnrolResponse(mockk()) } }

        runBlocking {
            val sessionId = clientApiSessionEventsManager.getCurrentSessionId()
            assertThat(sessionId).isNotEqualTo(newSessionId)
            coVerify(exactly = 0) { clientApiSessionEventsManager.closeCurrentSessionNormally() }
        }

    }

    companion object {
        internal const val RETURN_FOR_FLOW_COMPLETED_CHECK = true
    }

}
