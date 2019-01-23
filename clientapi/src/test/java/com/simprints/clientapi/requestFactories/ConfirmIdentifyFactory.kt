package com.simprints.clientapi.requestFactories

import com.simprints.clientapi.clientrequests.builders.ConfirmIdentifyBuilder
import com.simprints.clientapi.clientrequests.extractors.ClientRequestExtractor
import com.simprints.clientapi.clientrequests.extractors.ConfirmIdentifyExtractor
import com.simprints.clientapi.clientrequests.validators.ConfirmIdentifyValidator
import com.simprints.clientapi.simprintsrequests.requests.ConfirmIdentifyRequest
import com.simprints.clientapi.simprintsrequests.requests.SimprintsIdRequest
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock


object ConfirmIdentifyFactory : RequestFactory() {

    override fun getValidSimprintsRequest(): SimprintsIdRequest = ConfirmIdentifyRequest(
        projectId = MOCK_PROJECT_ID,
        sessionId = MOCK_SESSION_ID,
        selectedGuid = MOCK_SELECTED_GUID
    )

    override fun getValidator(extractor: ClientRequestExtractor): ConfirmIdentifyValidator =
        ConfirmIdentifyValidator(extractor as ConfirmIdentifyExtractor)

    override fun getBuilder(extractor: ClientRequestExtractor): ConfirmIdentifyBuilder =
        ConfirmIdentifyBuilder(extractor as ConfirmIdentifyExtractor, getValidator(extractor))

    override fun getMockExtractor(withLegacyApiKey: Boolean): ConfirmIdentifyExtractor {
        val mockConfirmIdentifyExtractor = mock(ConfirmIdentifyExtractor::class.java)
        setMockDefaultExtractor(mockConfirmIdentifyExtractor, withLegacyApiKey)
        `when`(mockConfirmIdentifyExtractor.getSessionId()).thenReturn(MOCK_SESSION_ID)
        `when`(mockConfirmIdentifyExtractor.getSelectedGuid()).thenReturn(MOCK_SELECTED_GUID)
        return mockConfirmIdentifyExtractor
    }

}
