package com.simprints.clientapi.activities.libsimprints

import com.simprints.clientapi.domain.responses.EnrollResponse
import com.simprints.clientapi.domain.responses.IdentifyResponse
import com.simprints.clientapi.domain.responses.VerifyResponse
import com.simprints.clientapi.requestFactories.ConfirmIdentifyFactory
import com.simprints.clientapi.requestFactories.EnrollRequestFactory
import com.simprints.clientapi.requestFactories.IdentifyRequestFactory
import com.simprints.clientapi.requestFactories.VerifyRequestFactory
import com.simprints.libsimprints.Constants
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.Registration
import com.simprints.libsimprints.Tier
import com.simprints.moduleapi.clientapi.responses.IClientApiResponseTier
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.util.*


@RunWith(MockitoJUnitRunner::class)
class LibSimprintsPresenterTest {

    @Mock
    private val view: LibSimprintsContract.View = LibSimprintsActivity()

    @Test
    fun startPresenterForRegister_ShouldRequestRegister() {
        val enrollmentExtractor = EnrollRequestFactory.getMockExtractor()
        Mockito.`when`(view.enrollExtractor).thenReturn(enrollmentExtractor)

        LibSimprintsPresenter(view, Constants.SIMPRINTS_REGISTER_INTENT).apply { start() }
        Mockito.verify(view, Mockito.times(1)).sendSimprintsRequest(
            EnrollRequestFactory.getValidSimprintsRequest())
    }

    @Test
    fun startPresenterForIdentify_ShouldRequestIdentify() {
        val identifyExtractor = IdentifyRequestFactory.getMockExtractor()
        Mockito.`when`(view.identifyExtractor).thenReturn(identifyExtractor)

        LibSimprintsPresenter(view, Constants.SIMPRINTS_IDENTIFY_INTENT).apply { start() }
        Mockito.verify(view, Mockito.times(1)).sendSimprintsRequest(
            IdentifyRequestFactory.getValidSimprintsRequest())
    }

    @Test
    fun startPresenterForVerify_ShouldRequestVerify() {
        val verifyExractor = VerifyRequestFactory.getMockExtractor()
        Mockito.`when`(view.verifyExtractor).thenReturn(verifyExractor)

        LibSimprintsPresenter(view, Constants.SIMPRINTS_VERIFY_INTENT).apply { start() }
        Mockito.verify(view, Mockito.times(1)).sendSimprintsRequest(
            VerifyRequestFactory.getValidSimprintsRequest())
    }

    @Test
    fun startPresenterForConfirmIdentify_ShouldRequestConfirmIdentify() {
        val confirmIdentify = ConfirmIdentifyFactory.getMockExtractor()
        Mockito.`when`(view.confirmIdentifyExtractor).thenReturn(confirmIdentify)

        LibSimprintsPresenter(view, Constants.SIMPRINTS_SELECT_GUID_INTENT).apply { start() }
        Mockito.verify(view, Mockito.times(1))
            .sendSimprintsConfirmationAndFinish(ConfirmIdentifyFactory.getValidSimprintsRequest())
    }

    @Test
    fun startPresenterWithGarbage_ShouldReturnActionError() {
        LibSimprintsPresenter(view, "Garbage").apply { start() }
        Mockito.verify(view, Mockito.times(1)).returnIntentActionErrorToClient()
    }

    @Test
    fun handleRegistration_ShouldReturnValidRegistration() {
        val registerId = UUID.randomUUID().toString()

        LibSimprintsPresenter(view, Constants.SIMPRINTS_REGISTER_INTENT)
            .handleEnrollResponse(EnrollResponse(registerId))
        Mockito.verify(view, Mockito.times(1))
            .returnRegistration(Registration(registerId))
    }

    @Test
    fun handleIdentification_ShouldReturnValidIdentification() {
        val id1 = IdentifyResponse
            .Identification(UUID.randomUUID().toString(), 100, IClientApiResponseTier.TIER_1)
        val id2 = IdentifyResponse
            .Identification(UUID.randomUUID().toString(), 15, IClientApiResponseTier.TIER_5)
        val idList = arrayListOf(id1, id2)
        val sessionId = UUID.randomUUID().toString()

        LibSimprintsPresenter(view, Constants.SIMPRINTS_IDENTIFY_INTENT).handleIdentifyResponse(
            IdentifyResponse(arrayListOf(id1, id2), sessionId))
        Mockito.verify(view, Mockito.times(1)).returnIdentification(
            ArrayList(idList.map {
                Identification(it.guid, it.confidence, Tier.valueOf(it.tier.name))
            }), sessionId
        )
    }

    @Test
    fun handleVerification_ShouldReturnValidVerification() {
        val verification = VerifyResponse(UUID.randomUUID().toString(), 100, IClientApiResponseTier.TIER_1)

        LibSimprintsPresenter(view, Constants.SIMPRINTS_VERIFY_INTENT).handleVerifyResponse(verification)

        Mockito.verify(view, Mockito.times(1)).returnVerification(
            verification.confidence,
            Tier.valueOf(verification.tier.name),
            verification.guid
        )
    }

    @Test
    fun handleResponseError_ShouldCallActionError() {
        LibSimprintsPresenter(view, "").handleResponseError()
        Mockito.verify(view, Mockito.times(1))
            .returnIntentActionErrorToClient()
    }

}


