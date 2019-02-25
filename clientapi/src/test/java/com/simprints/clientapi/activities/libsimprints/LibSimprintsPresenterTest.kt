package com.simprints.clientapi.activities.libsimprints

import com.simprints.clientapi.requestFactories.ConfirmIdentifyFactory
import com.simprints.clientapi.requestFactories.EnrollRequestFactory
import com.simprints.clientapi.requestFactories.IdentifyRequestFactory
import com.simprints.clientapi.requestFactories.VerifyRequestFactory
import com.simprints.libsimprints.Constants
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner


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
        Mockito.verify(view, Mockito.times(1)).sendSimprintsRequest(
            ConfirmIdentifyFactory.getValidSimprintsRequest())
    }

    @Test
    fun startPresenterWithGarbage_ShouldReturnActionError() {
        LibSimprintsPresenter(view, "Garbage").apply { start() }
        Mockito.verify(view, Mockito.times(1)).returnIntentActionErrorToClient()
    }

}
