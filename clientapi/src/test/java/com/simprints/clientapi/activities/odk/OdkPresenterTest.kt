package com.simprints.clientapi.activities.odk

import com.simprints.clientapi.activities.odk.OdkPresenter.Companion.ACTION_CONFIRM_IDENTITY
import com.simprints.clientapi.activities.odk.OdkPresenter.Companion.ACTION_IDENTIFY
import com.simprints.clientapi.activities.odk.OdkPresenter.Companion.ACTION_REGISTER
import com.simprints.clientapi.activities.odk.OdkPresenter.Companion.ACTION_VERIFY
import com.simprints.clientapi.domain.responses.EnrollResponse
import com.simprints.clientapi.domain.responses.IdentifyResponse
import com.simprints.clientapi.domain.responses.VerifyResponse
import com.simprints.clientapi.domain.responses.entities.MatchResult
import com.simprints.clientapi.domain.responses.entities.Tier.TIER_1
import com.simprints.clientapi.domain.responses.entities.Tier.TIER_5
import com.simprints.clientapi.requestFactories.ConfirmIdentifyFactory
import com.simprints.clientapi.requestFactories.EnrollRequestFactory
import com.simprints.clientapi.requestFactories.IdentifyRequestFactory
import com.simprints.clientapi.requestFactories.VerifyRequestFactory
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.verifyOnce
import com.simprints.testtools.common.syntax.whenever
import org.junit.Test
import java.util.*

class OdkPresenterTest {

    private val view = mock<OdkActivity>()

    @Test
    fun startPresenterForRegister_ShouldRequestRegister() {
        val enrollmentExtractor = EnrollRequestFactory.getMockExtractor()
        whenever(view) { enrollExtractor } thenReturn enrollmentExtractor

        OdkPresenter(view, mock(), mock(), ACTION_REGISTER).apply { start() }
        verifyOnce(view) { sendSimprintsRequest(EnrollRequestFactory.getValidSimprintsRequest()) }
    }

    @Test
    fun startPresenterForIdentify_ShouldRequestIdentify() {
        val identificationExtractor = IdentifyRequestFactory.getMockExtractor()
        whenever(view) { identifyExtractor } thenReturn identificationExtractor

        OdkPresenter(view, mock(), mock(), ACTION_IDENTIFY).apply { start() }
        verifyOnce(view) { sendSimprintsRequest(IdentifyRequestFactory.getValidSimprintsRequest()) }
    }

    @Test
    fun startPresenterForVerify_ShouldRequestVerify() {
        val verifyExractor = VerifyRequestFactory.getMockExtractor()
        whenever(view) { verifyExtractor } thenReturn verifyExractor

        OdkPresenter(view, mock(), mock(), ACTION_VERIFY).apply { start() }
        verifyOnce(view) { sendSimprintsRequest(VerifyRequestFactory.getValidSimprintsRequest()) }
    }

    @Test
    fun startPresenterWithGarbage_ShouldReturnActionError() {
        OdkPresenter(view, mock(), mock(), "Garbage").apply { start() }
        verifyOnce(view) { returnIntentActionErrorToClient() }
    }

    @Test
    fun handleRegistration_ShouldReturnValidOdkRegistration() {
        val registerId = UUID.randomUUID().toString()

        OdkPresenter(view, mock(), mock(), ACTION_REGISTER).handleEnrollResponse(EnrollResponse(registerId))
        verifyOnce(view) { returnRegistration(registerId) }
    }

    @Test
    fun handleIdentification_ShouldReturnValidOdkIdentification() {
        val id1 = MatchResult(UUID.randomUUID().toString(), 100, TIER_1)
        val id2 = MatchResult(UUID.randomUUID().toString(), 15, TIER_5)
        val sessionId = UUID.randomUUID().toString()

        OdkPresenter(view, mock(), mock(), ACTION_IDENTIFY).handleIdentifyResponse(
            IdentifyResponse(arrayListOf(id1, id2), sessionId))
        verifyOnce(view) {
            returnIdentification(
                idList = "${id1.guidFound} ${id2.guidFound}",
                confidenceList = "${id1.confidence} ${id2.confidence}",
                tierList = "${id1.tier} ${id2.tier}",
                sessionId = sessionId)
        }
    }

    @Test
    fun handleVerification_ShouldReturnValidOdkVerification() {
        val verification = VerifyResponse(MatchResult(UUID.randomUUID().toString(), 100, TIER_1))

        OdkPresenter(view, mock(), mock(), ACTION_IDENTIFY).handleVerifyResponse(verification)
        verifyOnce(view) {
            returnVerification(
                id = verification.matchResult.guidFound,
                confidence = verification.matchResult.confidence.toString(),
                tier = verification.matchResult.tier.toString())
        }
    }

    @Test
    fun handleResponseError_ShouldCallActionError() {
        OdkPresenter(view, mock(), mock(), "").handleResponseError()
        verifyOnce(view) { returnIntentActionErrorToClient() }
    }

    @Test
    fun startPresenterForConfirmIdentify_ShouldRequestConfirmIdentify() {
        val confirmIdentify = ConfirmIdentifyFactory.getMockExtractor()
        whenever(view) { confirmIdentifyExtractor } thenReturn confirmIdentify

        OdkPresenter(view, mock(), mock(), ACTION_CONFIRM_IDENTITY).apply { start() }
        verifyOnce(view) { sendSimprintsConfirmationAndFinish(ConfirmIdentifyFactory.getValidSimprintsRequest()) }
    }
}
