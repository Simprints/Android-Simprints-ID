package com.simprints.clientapi.activities.libsimprints

import com.simprints.clientapi.requestFactories.*
import com.simprints.clientapi.simprintsrequests.ConfirmIdentifyRequest
import com.simprints.clientapi.simprintsrequests.EnrollRequest
import com.simprints.clientapi.simprintsrequests.IdentifyRequest
import com.simprints.clientapi.simprintsrequests.VerifyRequest
import com.simprints.clientapi.simprintsrequests.legacy.LegacyConfirmIdentifyRequest
import com.simprints.clientapi.simprintsrequests.legacy.LegacyEnrollRequest
import com.simprints.clientapi.simprintsrequests.legacy.LegacyIdentifyRequest
import com.simprints.clientapi.simprintsrequests.legacy.LegacyVerifyRequest
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
        Mockito.verify(view, Mockito.times(1)).sendSimprintsRequest(EnrollRequest(
            projectId = RequestFactory.MOCK_PROJECT_ID,
            moduleId = RequestFactory.MOCK_MODULE_ID,
            userId = RequestFactory.MOCK_USER_ID,
            metadata = RequestFactory.MOCK_METADATA
        ))
    }

    @Test
    fun startPresenterForLegacyRegister_ShouldRequestLegacyRegister() {
        val enrollmentExtractor = EnrollRequestFactory.getMockExtractor(withLegacyApiKey = true)
        Mockito.`when`(view.enrollExtractor).thenReturn(enrollmentExtractor)

        LibSimprintsPresenter(view, Constants.SIMPRINTS_REGISTER_INTENT).apply { start() }
        Mockito.verify(view, Mockito.times(1)).sendSimprintsRequest(LegacyEnrollRequest(
            legacyApiKey = RequestFactory.MOCK_LEGACY_API_KEY,
            moduleId = RequestFactory.MOCK_MODULE_ID,
            userId = RequestFactory.MOCK_USER_ID,
            metadata = RequestFactory.MOCK_METADATA
        ))
    }

    @Test
    fun startPresenterForIdentify_ShouldRequestIdentify() {
        val identifyExtractor = IdentifyRequestFactory.getMockExtractor()
        Mockito.`when`(view.identifyExtractor).thenReturn(identifyExtractor)

        LibSimprintsPresenter(view, Constants.SIMPRINTS_IDENTIFY_INTENT).apply { start() }
        Mockito.verify(view, Mockito.times(1)).sendSimprintsRequest(IdentifyRequest(
            projectId = RequestFactory.MOCK_PROJECT_ID,
            moduleId = RequestFactory.MOCK_MODULE_ID,
            userId = RequestFactory.MOCK_USER_ID,
            metadata = RequestFactory.MOCK_METADATA
        ))
    }

    @Test
    fun startPresenterForLegacyIdentify_ShouldRequestLegacyIdentify() {
        val identifyExtractor = IdentifyRequestFactory.getMockExtractor(withLegacyApiKey = true)
        Mockito.`when`(view.identifyExtractor).thenReturn(identifyExtractor)

        LibSimprintsPresenter(view, Constants.SIMPRINTS_IDENTIFY_INTENT).apply { start() }
        Mockito.verify(view, Mockito.times(1)).sendSimprintsRequest(LegacyIdentifyRequest(
            legacyApiKey = RequestFactory.MOCK_LEGACY_API_KEY,
            moduleId = RequestFactory.MOCK_MODULE_ID,
            userId = RequestFactory.MOCK_USER_ID,
            metadata = RequestFactory.MOCK_METADATA
        ))
    }

    @Test
    fun startPresenterForVerify_ShouldRequestVerify() {
        val verifyExractor = VerifyRequestFactory.getMockExtractor()
        Mockito.`when`(view.verifyExtractor).thenReturn(verifyExractor)

        LibSimprintsPresenter(view, Constants.SIMPRINTS_VERIFY_INTENT).apply { start() }
        Mockito.verify(view, Mockito.times(1)).sendSimprintsRequest(VerifyRequest(
            projectId = RequestFactory.MOCK_PROJECT_ID,
            moduleId = RequestFactory.MOCK_MODULE_ID,
            userId = RequestFactory.MOCK_USER_ID,
            metadata = RequestFactory.MOCK_METADATA,
            verifyGuid = RequestFactory.MOCK_VERIFY_GUID
        ))
    }

    @Test
    fun startPresenterForLegacyVerify_ShouldRequestLegacyVerify() {
        val verifyExractor = VerifyRequestFactory.getMockExtractor(withLegacyApiKey = true)
        Mockito.`when`(view.verifyExtractor).thenReturn(verifyExractor)

        LibSimprintsPresenter(view, Constants.SIMPRINTS_VERIFY_INTENT).apply { start() }
        Mockito.verify(view, Mockito.times(1)).sendSimprintsRequest(LegacyVerifyRequest(
            legacyApiKey = RequestFactory.MOCK_LEGACY_API_KEY,
            moduleId = RequestFactory.MOCK_MODULE_ID,
            userId = RequestFactory.MOCK_USER_ID,
            metadata = RequestFactory.MOCK_METADATA,
            verifyGuid = RequestFactory.MOCK_VERIFY_GUID
        ))
    }

    @Test
    fun startPresenterForConfirmIdentify_ShouldRequestConfirmIdentify() {
        val confirmIdentify = ConfirmIdentifyFactory.getMockExtractor()
        Mockito.`when`(view.confirmIdentifyExtractor).thenReturn(confirmIdentify)

        LibSimprintsPresenter(view, Constants.SIMPRINTS_SELECT_GUID_INTENT).apply { start() }
        Mockito.verify(view, Mockito.times(1)).sendSimprintsRequest(ConfirmIdentifyRequest(
            projectId = RequestFactory.MOCK_PROJECT_ID,
            sessionId = RequestFactory.MOCK_SESSION_ID,
            selectedGuid = RequestFactory.MOCK_SELECTED_GUID
        ))
    }

    @Test
    fun startPresenterForLegacyConfirmIdentify_ShouldRequestLegacyConfirmIdentify() {
        val confirmIdentify = ConfirmIdentifyFactory.getMockExtractor(withLegacyApiKey = true)
        Mockito.`when`(view.confirmIdentifyExtractor).thenReturn(confirmIdentify)

        LibSimprintsPresenter(view, Constants.SIMPRINTS_SELECT_GUID_INTENT).apply { start() }
        Mockito.verify(view, Mockito.times(1)).sendSimprintsRequest(LegacyConfirmIdentifyRequest(
            legacyApiKey = RequestFactory.MOCK_LEGACY_API_KEY,
            sessionId = RequestFactory.MOCK_SESSION_ID,
            selectedGuid = RequestFactory.MOCK_SELECTED_GUID
        ))
    }

    @Test
    fun startPresenterWithGarbage_ShouldReturnActionError() {
        LibSimprintsPresenter(view, "Garbage").apply { start() }
        Mockito.verify(view, Mockito.times(1)).returnIntentActionErrorToClient()
    }

}
