package com.simprints.clientapi.activities.odk

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.clientapi.activities.odk.OdkPresenter.Companion.ACTION_CONFIRM_IDENTITY
import com.simprints.clientapi.activities.odk.OdkPresenter.Companion.ACTION_IDENTIFY
import com.simprints.clientapi.activities.odk.OdkPresenter.Companion.ACTION_REGISTER
import com.simprints.clientapi.activities.odk.OdkPresenter.Companion.ACTION_VERIFY
import com.simprints.clientapi.domain.responses.EnrollResponse
import com.simprints.clientapi.domain.responses.IdentifyResponse
import com.simprints.clientapi.domain.responses.IdentifyResponse.Identification
import com.simprints.clientapi.domain.responses.VerifyResponse
import com.simprints.clientapi.requestFactories.ConfirmIdentifyFactory
import com.simprints.clientapi.requestFactories.EnrollRequestFactory
import com.simprints.clientapi.requestFactories.IdentifyRequestFactory
import com.simprints.clientapi.requestFactories.VerifyRequestFactory
import com.simprints.moduleapi.app.responses.IAppResponseTier.TIER_1
import com.simprints.moduleapi.app.responses.IAppResponseTier.TIER_5
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.verifyOnce
import com.simprints.testtools.common.syntax.whenever
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class OdkPresenterTest {

    private val view = mock<OdkActivity>()

    @Test
    fun startPresenterForRegister_ShouldRequestRegister() {
        val enrollmentExtractor = EnrollRequestFactory.getMockExtractor()
        whenever(view) { enrollExtractor } thenReturn enrollmentExtractor

        OdkPresenter(view, ACTION_REGISTER).apply { start() }
        verifyOnce(view) { sendSimprintsRequest(EnrollRequestFactory.getValidSimprintsRequest()) }
    }

    @Test
    fun startPresenterForIdentify_ShouldRequestIdentify() {
        val identificationExtractor = IdentifyRequestFactory.getMockExtractor()
        whenever(view) { identifyExtractor } thenReturn identificationExtractor

        OdkPresenter(view, ACTION_IDENTIFY).apply { start() }
        verifyOnce(view) { sendSimprintsRequest(IdentifyRequestFactory.getValidSimprintsRequest()) }
    }

    @Test
    fun startPresenterForVerify_ShouldRequestVerify() {
        val verifyExractor = VerifyRequestFactory.getMockExtractor()
        whenever(view) { verifyExtractor } thenReturn verifyExractor

        OdkPresenter(view, ACTION_VERIFY).apply { start() }
        verifyOnce(view) { sendSimprintsRequest(VerifyRequestFactory.getValidSimprintsRequest()) }
    }

    @Test
    fun startPresenterWithGarbage_ShouldReturnActionError() {
        OdkPresenter(view, "Garbage").apply { start() }
        verifyOnce(view) { returnIntentActionErrorToClient() }
    }

    @Test
    fun handleRegistration_ShouldReturnValidOdkRegistration() {
        val registerId = UUID.randomUUID().toString()

        OdkPresenter(view, ACTION_REGISTER).handleEnrollResponse(EnrollResponse(registerId))
        verifyOnce(view) { returnRegistration(registerId) }
    }

    @Test
    fun handleIdentification_ShouldReturnValidOdkIdentification() {
        val id1 = Identification(UUID.randomUUID().toString(), 100, TIER_1)
        val id2 = Identification(UUID.randomUUID().toString(), 15, TIER_5)
        val sessionId = UUID.randomUUID().toString()

        OdkPresenter(view, ACTION_IDENTIFY).handleIdentifyResponse(
            IdentifyResponse(arrayListOf(id1, id2), sessionId))
        verifyOnce(view) {
            returnIdentification(
                idList = "${id1.guid} ${id2.guid}",
                confidenceList = "${id1.confidence} ${id2.confidence}",
                tierList = "${id1.tier} ${id2.tier}",
                sessionId = sessionId)
        }
    }

    @Test
    fun handleVerification_ShouldReturnValidOdkVerification() {
        val verification = VerifyResponse(UUID.randomUUID().toString(), 100, TIER_1)

        OdkPresenter(view, ACTION_IDENTIFY).handleVerifyResponse(verification)
        verifyOnce(view) {
            returnVerification(
                id = verification.guid,
                confidence = verification.confidence.toString(),
                tier = verification.tier.toString())
        }
    }

    @Test
    fun handleResponseError_ShouldCallActionError() {
        OdkPresenter(view, "").handleResponseError()
        verifyOnce(view) { returnIntentActionErrorToClient() }
    }

    @Test
    fun startPresenterForConfirmIdentify_ShouldRequestConfirmIdentify() {
        val confirmIdentify = ConfirmIdentifyFactory.getMockExtractor()
        whenever(view) { confirmIdentifyExtractor } thenReturn confirmIdentify

        OdkPresenter(view, ACTION_CONFIRM_IDENTITY).apply { start() }
        verifyOnce(view) { sendSimprintsConfirmationAndFinish(ConfirmIdentifyFactory.getValidSimprintsRequest()) }
    }
}
