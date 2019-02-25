package com.simprints.clientapi.requestFactories

import com.simprints.clientapi.clientrequests.builders.VerifyBuilder
import com.simprints.clientapi.clientrequests.extractors.ClientRequestExtractor
import com.simprints.clientapi.clientrequests.extractors.VerifyExtractor
import com.simprints.clientapi.clientrequests.validators.VerifyValidator
import com.simprints.clientapi.simprintsrequests.requests.ClientApiBaseRequest
import com.simprints.clientapi.simprintsrequests.requests.ClientApiVerifyRequest
import org.mockito.Mockito


object VerifyRequestFactory : RequestFactory() {

    override fun getValidSimprintsRequest(): ClientApiBaseRequest = ClientApiVerifyRequest(
        projectId = MOCK_PROJECT_ID,
        moduleId = MOCK_MODULE_ID,
        userId = MOCK_USER_ID,
        metadata = MOCK_METADATA,
        verifyGuid = MOCK_VERIFY_GUID
    )

    override fun getBuilder(extractor: ClientRequestExtractor): VerifyBuilder =
        VerifyBuilder(extractor as VerifyExtractor, getValidator(extractor))

    override fun getValidator(extractor: ClientRequestExtractor): VerifyValidator =
        VerifyValidator(extractor as VerifyExtractor)

    override fun getMockExtractor(withLegacyApiKey: Boolean): VerifyExtractor {
        val mockVerifyExtractor = Mockito.mock(VerifyExtractor::class.java)
        setMockDefaultExtractor(mockVerifyExtractor, withLegacyApiKey)
        Mockito.`when`(mockVerifyExtractor.getVerifyGuid()).thenReturn(RequestFactory.MOCK_VERIFY_GUID)
        return mockVerifyExtractor
    }

}
