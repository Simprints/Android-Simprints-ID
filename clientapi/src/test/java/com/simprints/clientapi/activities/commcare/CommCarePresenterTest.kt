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
import com.simprints.clientapi.requestFactories.ConfirmIdentifyFactory
import com.simprints.clientapi.requestFactories.EnrollRequestFactory
import com.simprints.clientapi.requestFactories.IdentifyRequestFactory
import com.simprints.clientapi.requestFactories.RequestFactory.Companion.MOCK_SESSION_ID
import com.simprints.clientapi.requestFactories.VerifyRequestFactory
import com.simprints.libsimprints.Constants
import com.simprints.testtools.common.syntax.*
import com.simprints.testtools.unit.BaseUnitTestConfig
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import java.util.*


class CommCarePresenterTest {

    companion object {
        private val INTEGRATION_INFO = IntegrationInfo.COMMCARE
        const val SKIP_CHECK_VALUE_FOR_FLOW_COMPLETED = false
    }

    private val view = mock<CommCareActivity>()

    @Before
    fun setup() {
        BaseUnitTestConfig().rescheduleRxMainThread().coroutinesMainThread()
    }

    @Test
    fun startPresenterForRegister_ShouldRequestRegister() {
        val enrollmentExtractor = EnrollRequestFactory.getMockExtractor()
        whenever(view) { enrollExtractor } thenReturn enrollmentExtractor

        CommCarePresenter(view, ACTION_REGISTER, mockSessionManagerToCreateSession(), mock(), mockSharedPrefs()).apply {
            runBlocking { start() }
        }

        verifyOnce(view) { sendSimprintsRequest(EnrollRequestFactory.getValidSimprintsRequest(INTEGRATION_INFO)) }
    }

    @Test
    fun startPresenterForIdentify_ShouldRequestIdentify() {
        val identifyExtractor = IdentifyRequestFactory.getMockExtractor()
        whenever(view.identifyExtractor) thenReturn identifyExtractor

        CommCarePresenter(view, ACTION_IDENTIFY, mockSessionManagerToCreateSession(), mock(), mockSharedPrefs()).apply {
            runBlocking { start() }
        }

        verifyOnce(view) { sendSimprintsRequest(IdentifyRequestFactory.getValidSimprintsRequest(INTEGRATION_INFO)) }
    }

    @Test
    fun startPresenterForVerify_ShouldRequestVerify() {
        val verificationExtractor = VerifyRequestFactory.getMockExtractor()
        whenever(view.verifyExtractor) thenReturn verificationExtractor

        CommCarePresenter(view, ACTION_VERIFY, mockSessionManagerToCreateSession(), mock(), mockSharedPrefs()).apply { runBlocking { start() } }

        verifyOnce(view) { sendSimprintsRequest(VerifyRequestFactory.getValidSimprintsRequest(INTEGRATION_INFO)) }
    }

    @Test
    fun startPresenterForConfirmIdentify_ShouldRequestConfirmIdentify() {
        val confirmIdentify = ConfirmIdentifyFactory.getMockExtractor()
        whenever(view) { confirmIdentifyExtractor } thenReturn confirmIdentify
        whenever(view) { extras } thenReturn mapOf(Pair(Constants.SIMPRINTS_SESSION_ID, MOCK_SESSION_ID))

        CommCarePresenter(view, ACTION_CONFIRM_IDENTITY, mockSessionManagerToCreateSession(), mock(), mockSharedPrefs()).apply { runBlocking { start() } }

        verifyOnce(view) { sendSimprintsConfirmation(ConfirmIdentifyFactory.getValidSimprintsRequest(INTEGRATION_INFO)) }
    }

    @Test
    fun startPresenterWithGarbage_ShouldReturnActionError() {
        CommCarePresenter(view, "Garbage", mockSessionManagerToCreateSession(), mock(), mockSharedPrefs()).apply { runBlocking { start() } }
        verifyOnce(view) { handleClientRequestError(anyNotNull()) }
    }

    @Test
    fun handleRegistration_ShouldReturnValidRegistration() {
        val registerId = UUID.randomUUID().toString()

        CommCarePresenter(view, Constants.SIMPRINTS_REGISTER_INTENT, mock(), mock(), mockSharedPrefs())
            .handleEnrollResponse(EnrollResponse(registerId))
        verifyOnce(view) { returnRegistration(registerId, SKIP_CHECK_VALUE_FOR_FLOW_COMPLETED) }
    }

    @Test
    fun handleIdentification_ShouldReturnValidIdentification() {
        val id1 = MatchResult(UUID.randomUUID().toString(), 100, Tier.TIER_1)
        val id2 = MatchResult(UUID.randomUUID().toString(), 15, Tier.TIER_5)
        val idList = arrayListOf(id1, id2)
        val sessionId = UUID.randomUUID().toString()

        CommCarePresenter(view, Constants.SIMPRINTS_IDENTIFY_INTENT, mock(), mock(), mockSharedPrefs()).handleIdentifyResponse(
            IdentifyResponse(arrayListOf(id1, id2), sessionId))

        verifyOnce(view) {
            returnIdentification(
                ArrayList(idList.map {
                    com.simprints.libsimprints.Identification(it.guidFound, it.confidence, com.simprints.libsimprints.Tier.valueOf(it.tier.name))
                }), sessionId)
        }
    }

    @Test
    fun handleVerification_ShouldReturnValidVerification() {
        val verification = VerifyResponse(MatchResult(UUID.randomUUID().toString(), 100, Tier.TIER_1))

        CommCarePresenter(view, Constants.SIMPRINTS_VERIFY_INTENT, mock(), mock(), mockSharedPrefs()).handleVerifyResponse(verification)

        verifyOnce(view) {
            returnVerification(
                verification.matchResult.confidence,
                com.simprints.libsimprints.Tier.valueOf(verification.matchResult.tier.name),
                verification.matchResult.guidFound,
                SKIP_CHECK_VALUE_FOR_FLOW_COMPLETED)
        }
    }

    @Test
    fun handleResponseError_ShouldCallActionError() {
        val error = ErrorResponse(ErrorResponse.Reason.INVALID_USER_ID)
        CommCarePresenter(view, "", mock(), mock(), mockSharedPrefs()).handleResponseError(error)
        verifyOnce(view) { returnErrorToClient(error) }
    }

    private fun mockSessionManagerToCreateSession() = mock<ClientApiSessionEventsManager>().apply {
        wheneverOnSuspend(this) { createSession(anyNotNull()) } thenOnBlockingReturn "session_id"
    }

    private fun mockSharedPrefs() = mock<SharedPreferencesManager>().apply {
        whenever(this) { peekSessionId() } thenReturn "sessionId"
        whenever(this) { popSessionId() } thenReturn "sessionId"
    }

}
