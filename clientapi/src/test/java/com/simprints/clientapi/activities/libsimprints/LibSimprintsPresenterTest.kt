package com.simprints.clientapi.activities.libsimprints

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
import com.simprints.libsimprints.Constants
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.Registration
import com.simprints.libsimprints.Tier
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.verifyOnce
import com.simprints.testtools.common.syntax.whenever
import org.junit.Test
import java.util.*

class LibSimprintsPresenterTest {

    private val view = mock<LibSimprintsActivity>()

    @Test
    fun startPresenterForRegister_ShouldRequestRegister() {
        val enrollmentExtractor = EnrollRequestFactory.getMockExtractor()
        whenever(view) { enrollExtractor } thenReturn enrollmentExtractor

        LibSimprintsPresenter(view, mock(), mock(), Constants.SIMPRINTS_REGISTER_INTENT).apply { start() }
        verifyOnce(view) { sendSimprintsRequest(EnrollRequestFactory.getValidSimprintsRequest()) }
    }

    @Test
    fun startPresenterForIdentify_ShouldRequestIdentify() {
        val identifyExtractor = IdentifyRequestFactory.getMockExtractor()
        whenever(view.identifyExtractor) thenReturn identifyExtractor

        LibSimprintsPresenter(view, mock(), mock(), Constants.SIMPRINTS_IDENTIFY_INTENT).apply { start() }
        verifyOnce(view) { sendSimprintsRequest(IdentifyRequestFactory.getValidSimprintsRequest()) }
    }

    @Test
    fun startPresenterForVerify_ShouldRequestVerify() {
        val verificationExtractor = VerifyRequestFactory.getMockExtractor()
        whenever(view.verifyExtractor) thenReturn verificationExtractor

        LibSimprintsPresenter(view, mock(), mock(), Constants.SIMPRINTS_VERIFY_INTENT).apply { start() }
        verifyOnce(view) { sendSimprintsRequest(VerifyRequestFactory.getValidSimprintsRequest()) }
    }

    @Test
    fun startPresenterForConfirmIdentify_ShouldRequestConfirmIdentify() {
        val confirmIdentify = ConfirmIdentifyFactory.getMockExtractor()
        whenever(view) { confirmIdentifyExtractor } thenReturn confirmIdentify

        LibSimprintsPresenter(view, mock(), mock(), Constants.SIMPRINTS_SELECT_GUID_INTENT).apply { start() }
        verifyOnce(view) { sendSimprintsConfirmationAndFinish(ConfirmIdentifyFactory.getValidSimprintsRequest()) }
    }

    @Test
    fun startPresenterWithGarbage_ShouldReturnActionError() {
        LibSimprintsPresenter(view, mock(), mock(), "Garbage").apply { start() }
        verifyOnce(view) { returnIntentActionErrorToClient() }
    }

    @Test
    fun handleRegistration_ShouldReturnValidRegistration() {
        val registerId = UUID.randomUUID().toString()

        LibSimprintsPresenter(view, mock(), mock(), Constants.SIMPRINTS_REGISTER_INTENT)
            .handleEnrollResponse(EnrollResponse(registerId))
        verifyOnce(view) { returnRegistration(Registration(registerId)) }
    }

    @Test
    fun handleIdentification_ShouldReturnValidIdentification() {
        val id1 = MatchResult(UUID.randomUUID().toString(), 100, TIER_1)
        val id2 = MatchResult(UUID.randomUUID().toString(), 15, TIER_5)
        val idList = arrayListOf(id1, id2)
        val sessionId = UUID.randomUUID().toString()

        LibSimprintsPresenter(view, mock(), mock(), Constants.SIMPRINTS_IDENTIFY_INTENT).handleIdentifyResponse(
            IdentifyResponse(arrayListOf(id1, id2), sessionId))
        verifyOnce(view) {
            returnIdentification(
                ArrayList(idList.map {
                    Identification(it.guidFound, it.confidence, Tier.valueOf(it.tier.name))
                }), sessionId)
        }
    }

    @Test
    fun handleVerification_ShouldReturnValidVerification() {
        val verification = VerifyResponse(MatchResult(UUID.randomUUID().toString(), 100, TIER_1))

        LibSimprintsPresenter(view, mock(), mock(), Constants.SIMPRINTS_VERIFY_INTENT).handleVerifyResponse(verification)

        verifyOnce(view) {
            returnVerification(
                verification.matchResult.confidence,
                Tier.valueOf(verification.matchResult.tier.name),
                verification.matchResult.guidFound)
        }
    }

    @Test
    fun handleResponseError_ShouldCallActionError() {
        LibSimprintsPresenter(view, mock(), mock(), "").handleResponseError()
        verifyOnce(view) { returnIntentActionErrorToClient() }
    }
}
