package com.simprints.clientapi.activities.libsimprints

import com.google.common.truth.Truth.assertThat
import com.simprints.clientapi.activities.libsimprints.LibSimprintsAction.*
import com.simprints.clientapi.activities.libsimprints.LibSimprintsAction.LibSimprintsActionFollowUpAction.ConfirmIdentity
import com.simprints.clientapi.activities.libsimprints.LibSimprintsAction.LibSimprintsActionFollowUpAction.EnrolLastBiometrics
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.controllers.core.eventData.model.IntegrationInfo.STANDARD
import com.simprints.clientapi.domain.responses.EnrolResponse
import com.simprints.clientapi.domain.responses.ErrorResponse
import com.simprints.clientapi.domain.responses.IdentifyResponse
import com.simprints.clientapi.domain.responses.VerifyResponse
import com.simprints.clientapi.domain.responses.entities.MatchResult
import com.simprints.clientapi.domain.responses.entities.Tier.TIER_1
import com.simprints.clientapi.domain.responses.entities.Tier.TIER_5
import com.simprints.clientapi.exceptions.InvalidIntentActionException
import com.simprints.clientapi.requestFactories.*
import com.simprints.libsimprints.Tier
import com.simprints.libsimprints.Verification
import com.simprints.testtools.unit.BaseUnitTestConfig
import io.kotlintest.shouldThrow
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import java.util.*

class LibSimprintsPresenterTest {

    companion object {
        const val RETURN_FOR_FLOW_COMPLETED_CHECK = true
    }

    @MockK
    lateinit var view: LibSimprintsActivity
    @MockK
    lateinit var clientApiSessionEventsManager: ClientApiSessionEventsManager

    @Before
    fun setup() {
        BaseUnitTestConfig().rescheduleRxMainThread().coroutinesMainThread()
        MockKAnnotations.init(this, relaxed = true)
        coEvery { clientApiSessionEventsManager.isCurrentSessionAnIdentificationOrEnrolment() } returns true
        coEvery { clientApiSessionEventsManager.getCurrentSessionId() } returns RequestFactory.MOCK_SESSION_ID
        coEvery { clientApiSessionEventsManager.createSession(any()) } returns "session_id"
    }

    @Test
    fun startPresenterForRegister_ShouldRequestRegister() {
        val enrolmentExtractor = EnrolRequestFactory.getMockExtractor()
        every { view.enrolExtractor } returns enrolmentExtractor

        LibSimprintsPresenter(
            view,
            Enrol,
            clientApiSessionEventsManager,
            mockk(),
            mockk()
        ).apply {
            runBlocking { start() }
        }

        verify(exactly = 1) { view.sendSimprintsRequest(EnrolRequestFactory.getValidSimprintsRequest(STANDARD)) }
    }

    @Test
    fun startPresenterForIdentify_ShouldRequestIdentify() {
        val identifyExtractor = IdentifyRequestFactory.getMockExtractor()
        every { view.identifyExtractor } returns identifyExtractor

        LibSimprintsPresenter(
            view,
            Identify,
            clientApiSessionEventsManager,
            mockk(),
            mockk()
        ).apply {
            runBlocking { start() }
        }

        verify(exactly = 1) { view.sendSimprintsRequest(IdentifyRequestFactory.getValidSimprintsRequest(STANDARD)) }
    }

    @Test
    fun startPresenterForVerify_ShouldRequestVerify() {
        val verificationExtractor = VerifyRequestFactory.getMockExtractor()
        every { view.verifyExtractor } returns verificationExtractor

        LibSimprintsPresenter(
            view,
            Verify,
            clientApiSessionEventsManager,
            mockk(),
            mockk()
        ).apply { runBlocking { start() } }

        verify(exactly = 1) { view.sendSimprintsRequest(VerifyRequestFactory.getValidSimprintsRequest(STANDARD)) }
    }

    @Test
    fun startPresenterForConfirmIdentify_ShouldRequestConfirmIdentify() {
        val confirmIdentify = ConfirmIdentityFactory.getMockExtractor()
        every { view.confirmIdentityExtractor } returns confirmIdentify

        LibSimprintsPresenter(
            view,
            ConfirmIdentity,
            clientApiSessionEventsManager,
            mockk(),
            mockk()
        ).apply { runBlocking { start() } }

        verify(exactly = 1) { view.sendSimprintsRequest(ConfirmIdentityFactory.getValidSimprintsRequest(STANDARD)) }
    }

    @Test
    fun startPresenterForEnrolLastBiometrics_ShouldRequestEnrolLastBiometrics() {
        val enrolLastBiometricsExtractor = EnrolLastBiometricsFactory.getMockExtractor()
        every { view.enrolLastBiometricsExtractor } returns enrolLastBiometricsExtractor

        LibSimprintsPresenter(
            view,
            EnrolLastBiometrics,
            clientApiSessionEventsManager,
            mockk(),
            mockk()
        ).apply { runBlocking { start() } }

        verify(exactly = 1) { view.sendSimprintsRequest(EnrolLastBiometricsFactory.getValidSimprintsRequest(STANDARD)) }
    }

    @Test
    fun startPresenterWithGarbage_ShouldReturnActionError() {
        LibSimprintsPresenter(
            view,
            Invalid,
            clientApiSessionEventsManager,
            mockk(),
            mockk()
        ).apply {
            runBlocking {
                shouldThrow<InvalidIntentActionException> {
                    start()
                }
            }
        }
    }

    @Test
    fun handleRegistration_ShouldReturnValidRegistration() {
        val registerId = UUID.randomUUID().toString()
        val sessionId = UUID.randomUUID().toString()

        coEvery { clientApiSessionEventsManager.getCurrentSessionId() } returns sessionId
        LibSimprintsPresenter(
            view,
            Enrol,
            clientApiSessionEventsManager,
            mockk(),
            mockk()
        ).handleEnrolResponse(EnrolResponse(registerId))

        verify(exactly = 1) {
            view.returnRegistration(
                withArg {
                    assertThat(it.guid).isEqualTo(registerId)
                },
                sessionId,
                RETURN_FOR_FLOW_COMPLETED_CHECK)
        }
        verifyCompletionCheckEventWasAdded()
    }

    @Test
    fun handleIdentification_ShouldReturnValidIdentification() {
        val id1 = MatchResult(UUID.randomUUID().toString(), 100, TIER_1)
        val id2 = MatchResult(UUID.randomUUID().toString(), 15, TIER_5)
        val expectedReturnedList = arrayListOf(id1, id2)
        val sessionId = UUID.randomUUID().toString()

        LibSimprintsPresenter(
            view,
            Identify,
            clientApiSessionEventsManager,
            mockk(),
            mockk()
        ).handleIdentifyResponse(IdentifyResponse(arrayListOf(id1, id2), sessionId))

        verify(exactly = 1) {
            view.returnIdentification(
                withArg { listOfIdentificationsReturned ->
                    expectedReturnedList.forEach { expected ->
                        listOfIdentificationsReturned.find {
                            it.confidence == expected.confidence.toFloat() &&
                                it.guid == expected.guidFound &&
                                it.tier.name == expected.tier.name
                        }
                    }
                },
                sessionId,
                RETURN_FOR_FLOW_COMPLETED_CHECK)
        }

        verifyCompletionCheckEventWasAdded()
    }

    @Test
    fun handleVerification_ShouldReturnValidVerification() {
        val verification = VerifyResponse(MatchResult(UUID.randomUUID().toString(), 100, TIER_1))
        val sessionId = UUID.randomUUID().toString()

        coEvery { clientApiSessionEventsManager.getCurrentSessionId() } returns sessionId


        LibSimprintsPresenter(
            view,
            Verify,
            clientApiSessionEventsManager,
            mockk(),
            mockk()
        ).apply {
            handleVerifyResponse(verification)
        }

        val libVerification = Verification(
            verification.matchResult.confidence,
            Tier.valueOf(verification.matchResult.tier.name),
            verification.matchResult.guidFound)

        verify(exactly = 1) {
            view.returnVerification(
                withArg {
                    assertThat(it.confidence).isEqualTo(libVerification.confidence)
                    assertThat(it.tier).isEqualTo(libVerification.tier)
                    assertThat(it.guid).isEqualTo(libVerification.guid)
                },
                sessionId,
                RETURN_FOR_FLOW_COMPLETED_CHECK)
        }
        verifyCompletionCheckEventWasAdded()
    }

    @Test
    fun handleResponseError_ShouldCallActionError() {
        val sessionId = UUID.randomUUID().toString()
        coEvery { clientApiSessionEventsManager.getCurrentSessionId() } returns sessionId

        LibSimprintsPresenter(
            view,
            Invalid,
            clientApiSessionEventsManager,
            mockk(),
            mockk()
        ).handleResponseError(ErrorResponse(ErrorResponse.Reason.INVALID_USER_ID))

        verify(exactly = 1) {
            view.returnErrorToClient(any(), RETURN_FOR_FLOW_COMPLETED_CHECK, sessionId)
        }
        verifyCompletionCheckEventWasAdded()
    }

    private fun verifyCompletionCheckEventWasAdded() {
        coVerify(exactly = 1) {
            clientApiSessionEventsManager.addCompletionCheckEvent(RETURN_FOR_FLOW_COMPLETED_CHECK)
        }
    }
}
