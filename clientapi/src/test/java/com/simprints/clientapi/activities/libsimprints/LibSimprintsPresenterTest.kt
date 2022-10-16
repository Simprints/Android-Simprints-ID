package com.simprints.clientapi.activities.libsimprints

import com.google.common.truth.Truth.assertThat
import com.simprints.clientapi.activities.libsimprints.LibSimprintsAction.*
import com.simprints.clientapi.activities.libsimprints.LibSimprintsAction.LibSimprintsActionFollowUpAction.ConfirmIdentity
import com.simprints.clientapi.activities.libsimprints.LibSimprintsAction.LibSimprintsActionFollowUpAction.EnrolLastBiometrics
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.controllers.core.eventData.model.IntegrationInfo.STANDARD
import com.simprints.clientapi.data.sharedpreferences.SharedPreferencesManager
import com.simprints.clientapi.data.sharedpreferences.canCoSyncData
import com.simprints.clientapi.domain.responses.EnrolResponse
import com.simprints.clientapi.domain.responses.ErrorResponse
import com.simprints.clientapi.domain.responses.IdentifyResponse
import com.simprints.clientapi.domain.responses.VerifyResponse
import com.simprints.clientapi.domain.responses.entities.MatchConfidence
import com.simprints.clientapi.domain.responses.entities.MatchResult
import com.simprints.clientapi.domain.responses.entities.Tier.TIER_1
import com.simprints.clientapi.domain.responses.entities.Tier.TIER_5
import com.simprints.clientapi.exceptions.InvalidIntentActionException
import com.simprints.clientapi.requestFactories.*
import com.simprints.libsimprints.Tier
import com.simprints.libsimprints.Verification
import com.simprints.testtools.unit.BaseUnitTestConfig
import io.kotest.assertions.throwables.shouldThrow
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
        mockkStatic("com.simprints.clientapi.data.sharedpreferences.SharedPreferencesManagerImplKt")
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
            mockk(),
            mockk(),
            mockk(),
            mockk()
        ).apply {
            runBlocking { start() }
        }

        verify(exactly = 1) {
            view.sendSimprintsRequest(
                EnrolRequestFactory.getValidSimprintsRequest(
                    STANDARD
                )
            )
        }
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
            mockk(),
            mockk(),
            mockk(),
            mockk()
        ).apply {
            runBlocking { start() }
        }

        verify(exactly = 1) {
            view.sendSimprintsRequest(
                IdentifyRequestFactory.getValidSimprintsRequest(
                    STANDARD
                )
            )
        }
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
            mockk(),
            mockk(),
            mockk(),
            mockk()
        ).apply { runBlocking { start() } }

        verify(exactly = 1) {
            view.sendSimprintsRequest(
                VerifyRequestFactory.getValidSimprintsRequest(
                    STANDARD
                )
            )
        }
    }

    @Test
    fun startPresenterForConfirmIdentify_ShouldRequestConfirmIdentify() {
        val confirmIdentify = ConfirmIdentityFactory.getMockExtractor()
        every { view.confirmIdentityExtractor } returns confirmIdentify
        coEvery { clientApiSessionEventsManager.isSessionHasIdentificationCallback(any()) } returns true
        coEvery { clientApiSessionEventsManager.getCurrentSessionId() } returns RequestFactory.MOCK_SESSION_ID

        LibSimprintsPresenter(
            view,
            ConfirmIdentity,
            clientApiSessionEventsManager,
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            mockk()
        ).apply { runBlocking { start() } }

        verify(exactly = 1) {
            view.sendSimprintsRequest(
                ConfirmIdentityFactory.getValidSimprintsRequest(
                    STANDARD
                )
            )
        }
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
            mockk(),
            mockk(),
            mockk(),
            mockk()
        ).apply { runBlocking { start() } }

        verify(exactly = 1) {
            view.sendSimprintsRequest(
                EnrolLastBiometricsFactory.getValidSimprintsRequest(
                    STANDARD
                )
            )
        }
    }

    @Test
    fun startPresenterWithGarbage_ShouldReturnActionError() {
        LibSimprintsPresenter(
            view,
            Invalid,
            clientApiSessionEventsManager,
            mockk(),
            mockk(),
            mockk(),
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
            view = view,
            action = Enrol,
            sessionEventsManager = clientApiSessionEventsManager,
            rootManager = mockk(),
            timeHelper = mockk(),
            subjectRepository = mockk(),
            jsonHelper = mockk(),
            sharedPreferencesManager = mockSharedPrefs()
        ).handleEnrolResponse(EnrolResponse(registerId))

        verify(exactly = 1) {
            view.returnRegistration(
                withArg {
                    assertThat(it.guid).isEqualTo(registerId)
                },
                sessionId,
                RETURN_FOR_FLOW_COMPLETED_CHECK,
                eventsJson = null, subjectActions = null
            )
        }
        verifyCompletionCheckEventWasAdded()
        coVerify { clientApiSessionEventsManager.closeCurrentSessionNormally() }
    }

    @Test
    fun handleIdentification_ShouldReturnValidIdentification() {
        val id1 = MatchResult(UUID.randomUUID().toString(), 100, TIER_1, MatchConfidence.HIGH)
        val id2 = MatchResult(UUID.randomUUID().toString(), 15, TIER_5, MatchConfidence.LOW)
        val expectedReturnedList = arrayListOf(id1, id2)
        val sessionId = UUID.randomUUID().toString()

        LibSimprintsPresenter(
            view = view,
            action = Identify,
            sessionEventsManager = clientApiSessionEventsManager,
            rootManager = mockk(),
            timeHelper = mockk(),
            subjectRepository = mockk(),
            jsonHelper = mockk(),
            sharedPreferencesManager = mockSharedPrefs()
        ).handleIdentifyResponse(IdentifyResponse(arrayListOf(id1, id2), sessionId))

        verify(exactly = 1) {
            view.returnIdentification(
                withArg { listOfIdentificationsReturned ->
                    expectedReturnedList.forEach { expected ->
                        listOfIdentificationsReturned.find {
                            it.confidence == expected.confidenceScore.toFloat() &&
                                it.guid == expected.guidFound &&
                                it.tier.name == expected.tier.name
                        }
                    }
                },
                sessionId,
                RETURN_FOR_FLOW_COMPLETED_CHECK,
                eventsJson = null
            )
        }

        verifyCompletionCheckEventWasAdded()
        coVerify(exactly = 0) { clientApiSessionEventsManager.closeCurrentSessionNormally() }
    }

    @Test
    fun handleVerification_ShouldReturnValidVerification() {
        val verification = VerifyResponse(
            MatchResult(
                UUID.randomUUID().toString(),
                100,
                TIER_1,
                MatchConfidence.HIGH
            )
        )
        val sessionId = UUID.randomUUID().toString()

        coEvery { clientApiSessionEventsManager.getCurrentSessionId() } returns sessionId


        LibSimprintsPresenter(
            view = view,
            action = Verify,
            sessionEventsManager = clientApiSessionEventsManager,
            rootManager = mockk(),
            timeHelper = mockk(),
            subjectRepository = mockk(),
            jsonHelper = mockk(),
            sharedPreferencesManager = mockSharedPrefs()
        ).apply {
            handleVerifyResponse(verification)
        }

        val libVerification = Verification(
            verification.matchResult.confidenceScore,
            Tier.valueOf(verification.matchResult.tier.name),
            verification.matchResult.guidFound
        )

        verify(exactly = 1) {
            view.returnVerification(
                withArg {
                    assertThat(it.confidence).isEqualTo(libVerification.confidence)
                    assertThat(it.tier).isEqualTo(libVerification.tier)
                    assertThat(it.guid).isEqualTo(libVerification.guid)
                },
                sessionId,
                RETURN_FOR_FLOW_COMPLETED_CHECK,
                eventsJson = null
            )
        }
        verifyCompletionCheckEventWasAdded()
        coVerify { clientApiSessionEventsManager.closeCurrentSessionNormally() }
    }

    @Test
    fun handleResponseError_ShouldCallActionError() {
        val sessionId = UUID.randomUUID().toString()
        coEvery { clientApiSessionEventsManager.getCurrentSessionId() } returns sessionId

        LibSimprintsPresenter(
            view = view,
            action = Invalid,
            sessionEventsManager = clientApiSessionEventsManager,
            rootManager = mockk(),
            timeHelper = mockk(),
            subjectRepository = mockk(),
            jsonHelper = mockk(),
            sharedPreferencesManager = mockSharedPrefs()
        ).handleResponseError(ErrorResponse(ErrorResponse.Reason.INVALID_USER_ID))

        verify(exactly = 1) {
            view.returnErrorToClient(any(), RETURN_FOR_FLOW_COMPLETED_CHECK, sessionId, null)
        }
        verifyCompletionCheckEventWasAdded()
        coVerify { clientApiSessionEventsManager.closeCurrentSessionNormally() }
    }

    private fun verifyCompletionCheckEventWasAdded() {
        coVerify(exactly = 1) {
            clientApiSessionEventsManager.addCompletionCheckEvent(RETURN_FOR_FLOW_COMPLETED_CHECK)
        }
    }

    @Test
    fun shouldNot_closeSession_whenHandling_responseFrom_confirmID_request() {
        val newSessionId = "session_id_changed"
        val confirmIdentify = ConfirmIdentityFactory.getMockExtractor()
        every { view.confirmIdentityExtractor } returns confirmIdentify

        coEvery { clientApiSessionEventsManager.closeCurrentSessionNormally() } answers {
            // return a new sessionId if closeSession is called
            coEvery { clientApiSessionEventsManager.getCurrentSessionId() } returns newSessionId
        }


        LibSimprintsPresenter(
            view,
            ConfirmIdentity,
            clientApiSessionEventsManager,
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            mockk()
        ).handleConfirmationResponse(mockk())



        runBlocking {
            val sessionId = clientApiSessionEventsManager.getCurrentSessionId()
            assertThat(sessionId).isNotEqualTo(newSessionId)
            coVerify(exactly = 0) { clientApiSessionEventsManager.closeCurrentSessionNormally() }
        }

    }

    @Test
    fun should_closeSession_whenHandling_responseFrom_enrolLastBiometrics_request() {
        val newSessionId = "session_id_changed"
        val enrolLastBiometricsExtractor = EnrolLastBiometricsFactory.getMockExtractor()
        every { view.enrolLastBiometricsExtractor } returns enrolLastBiometricsExtractor

        coEvery { clientApiSessionEventsManager.closeCurrentSessionNormally() } answers {
            // return a new sessionId if closeSession is called
            coEvery { clientApiSessionEventsManager.getCurrentSessionId() } returns newSessionId
        }


        LibSimprintsPresenter(
            view,
            EnrolLastBiometrics,
            clientApiSessionEventsManager,
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            mockk()
        ).handleEnrolResponse(mockk())
        runBlocking {
            val sessionId = clientApiSessionEventsManager.getCurrentSessionId()
            assertThat(sessionId).isEqualTo(newSessionId)
            coVerify(exactly = 1) { clientApiSessionEventsManager.closeCurrentSessionNormally() }
        }
    }

    private fun mockSharedPrefs(canCosync: Boolean = false) =
        mockk<SharedPreferencesManager>().apply {
            coEvery { this@apply.canCoSyncData() } returns canCosync
        }
}
