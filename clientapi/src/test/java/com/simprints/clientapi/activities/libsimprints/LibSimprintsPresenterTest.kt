package com.simprints.clientapi.activities.libsimprints

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.eq
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.controllers.core.eventData.model.IntegrationInfo.STANDARD
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
import com.simprints.libsimprints.Constants
import com.simprints.libsimprints.Tier
import com.simprints.libsimprints.Verification
import com.simprints.testtools.common.syntax.*
import com.simprints.testtools.unit.BaseUnitTestConfig
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.util.*


class LibSimprintsPresenterTest {

    companion object {
        const val RETURN_FOR_FLOW_COMPLETED_CHECK = true
    }

    private val view = mock<LibSimprintsActivity>()
    @Mock lateinit var clientApiSessionEventsManager: ClientApiSessionEventsManager

    @Before
    fun setup() {
        BaseUnitTestConfig().rescheduleRxMainThread().coroutinesMainThread()
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun startPresenterForRegister_ShouldRequestRegister() {
        val enrollmentExtractor = EnrollRequestFactory.getMockExtractor()
        whenever(view) { enrollExtractor } thenReturn enrollmentExtractor

        LibSimprintsPresenter(view, Constants.SIMPRINTS_REGISTER_INTENT, mockSessionManagerToCreateSession(), mock()).apply {
            runBlocking { start() }
        }

        verifyOnce(view) { sendSimprintsRequest(EnrollRequestFactory.getValidSimprintsRequest(STANDARD)) }
    }

    @Test
    fun startPresenterForIdentify_ShouldRequestIdentify() {
        val identifyExtractor = IdentifyRequestFactory.getMockExtractor()
        whenever(view.identifyExtractor) thenReturn identifyExtractor

        LibSimprintsPresenter(view, Constants.SIMPRINTS_IDENTIFY_INTENT, mockSessionManagerToCreateSession(), mock()).apply {
            runBlocking { start() }
        }

        verifyOnce(view) { sendSimprintsRequest(IdentifyRequestFactory.getValidSimprintsRequest(STANDARD)) }
    }

    @Test
    fun startPresenterForVerify_ShouldRequestVerify() {
        val verificationExtractor = VerifyRequestFactory.getMockExtractor()
        whenever(view.verifyExtractor) thenReturn verificationExtractor

        LibSimprintsPresenter(view, Constants.SIMPRINTS_VERIFY_INTENT, mockSessionManagerToCreateSession(), mock()).apply { runBlocking { start() } }

        verifyOnce(view) { sendSimprintsRequest(VerifyRequestFactory.getValidSimprintsRequest(STANDARD)) }
    }

    @Test
    fun startPresenterForConfirmIdentify_ShouldRequestConfirmIdentify() {
        val confirmIdentify = ConfirmIdentityFactory.getMockExtractor()
        whenever(view) { confirmIdentityExtractor } thenReturn confirmIdentify

        LibSimprintsPresenter(view, Constants.SIMPRINTS_SELECT_GUID_INTENT, mockSessionManagerToCreateSession(), mock()).apply { runBlocking { start() } }

        verifyOnce(view) { sendSimprintsConfirmation(ConfirmIdentityFactory.getValidSimprintsRequest(STANDARD)) }
    }

    @Test
    fun startPresenterWithGarbage_ShouldReturnActionError() {
        LibSimprintsPresenter(view, "Garbage", mockSessionManagerToCreateSession(), mock()).apply { runBlocking { start() } }
        verifyOnce(view) { handleClientRequestError(anyNotNull()) }
    }

    @Test
    fun handleRegistration_ShouldReturnValidRegistration() {
        val registerId = UUID.randomUUID().toString()
        val sessionId = UUID.randomUUID().toString()

        wheneverOnSuspend(clientApiSessionEventsManager) { getCurrentSession() } thenOnBlockingReturn sessionId
        LibSimprintsPresenter(view, Constants.SIMPRINTS_REGISTER_INTENT, clientApiSessionEventsManager, mock())
            .handleEnrollResponse(EnrollResponse(registerId))

        verifyOnce(view) {
            returnRegistration(
                argThat {
                    assertThat(it.guid).isEqualTo(registerId)
                },
                eq(sessionId),
                eq(RETURN_FOR_FLOW_COMPLETED_CHECK))
        }
        verifyCompletionCheckEventWasAdded()
    }

    @Test
    fun handleIdentification_ShouldReturnValidIdentification() {
        val id1 = MatchResult(UUID.randomUUID().toString(), 100, TIER_1)
        val id2 = MatchResult(UUID.randomUUID().toString(), 15, TIER_5)
        val expectedReturnedList = arrayListOf(id1, id2)
        val sessionId = UUID.randomUUID().toString()

        LibSimprintsPresenter(view, Constants.SIMPRINTS_IDENTIFY_INTENT, clientApiSessionEventsManager, mock()).handleIdentifyResponse(
            IdentifyResponse(arrayListOf(id1, id2), sessionId))

        verifyOnce(view) {
            returnIdentification(
                argThat { listOfIdentificationsReturned ->
                    expectedReturnedList.forEach { expected ->
                        listOfIdentificationsReturned.find {
                            it.confidence == expected.confidence.toFloat() &&
                                it.guid == expected.guidFound &&
                                it.tier.name == expected.tier.name
                        }
                    }
                },
                eq(sessionId),
                eq(RETURN_FOR_FLOW_COMPLETED_CHECK))
        }

        verifyCompletionCheckEventWasAdded()
    }

    @Test
    fun handleVerification_ShouldReturnValidVerification() {
        val verification = VerifyResponse(MatchResult(UUID.randomUUID().toString(), 100, TIER_1))
        val sessionId = UUID.randomUUID().toString()

        wheneverOnSuspend(clientApiSessionEventsManager) { getCurrentSession() } thenOnBlockingReturn sessionId
        LibSimprintsPresenter(view, Constants.SIMPRINTS_VERIFY_INTENT, clientApiSessionEventsManager, mock()).apply {
            handleVerifyResponse(verification)
        }

        val libVerification = Verification(
            verification.matchResult.confidence,
            Tier.valueOf(verification.matchResult.tier.name),
            verification.matchResult.guidFound)

        verifyOnce(view) {
            returnVerification(
                argThat {
                    assertThat(it.confidence).isEqualTo(libVerification.confidence)
                    assertThat(it.tier).isEqualTo(libVerification.tier)
                    assertThat(it.guid).isEqualTo(libVerification.guid)
                },
                eq(sessionId),
                eq(RETURN_FOR_FLOW_COMPLETED_CHECK))
        }
        verifyCompletionCheckEventWasAdded()
    }

    @Test
    fun handleResponseError_ShouldCallActionError() {
        LibSimprintsPresenter(view, "", clientApiSessionEventsManager, mock()).handleResponseError(ErrorResponse(ErrorResponse.Reason.INVALID_USER_ID))
        verifyOnce(view) {
            returnErrorToClient(anyNotNull(), eq(RETURN_FOR_FLOW_COMPLETED_CHECK))
        }
        verifyCompletionCheckEventWasAdded()
    }

    private fun verifyCompletionCheckEventWasAdded() {
        verifyOnce(clientApiSessionEventsManager) { runBlocking { addCompletionCheckEvent(eq(RETURN_FOR_FLOW_COMPLETED_CHECK)) } }
    }

    private fun mockSessionManagerToCreateSession() = mock<ClientApiSessionEventsManager>().apply {
        wheneverOnSuspend(this) { createSession(anyNotNull()) } thenOnBlockingReturn "session_id"
    }
}
