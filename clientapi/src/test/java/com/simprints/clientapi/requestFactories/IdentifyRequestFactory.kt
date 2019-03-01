package com.simprints.clientapi.requestFactories

import com.simprints.clientapi.clientrequests.builders.IdentifyBuilder
import com.simprints.clientapi.clientrequests.extractors.ClientRequestExtractor
import com.simprints.clientapi.clientrequests.extractors.IdentifyExtractor
import com.simprints.clientapi.clientrequests.validators.IdentifyValidator
import com.simprints.clientapi.domain.requests.BaseRequest
import com.simprints.clientapi.domain.requests.IdentifyRequest
import org.mockito.Mockito


object IdentifyRequestFactory : RequestFactory() {

    override fun getValidSimprintsRequest(): BaseRequest = IdentifyRequest(
        projectId = MOCK_PROJECT_ID,
        moduleId = MOCK_MODULE_ID,
        userId = MOCK_USER_ID,
        metadata = MOCK_METADATA
    )

    override fun getValidator(extractor: ClientRequestExtractor): IdentifyValidator =
        IdentifyValidator(extractor as IdentifyExtractor)

    override fun getBuilder(extractor: ClientRequestExtractor): IdentifyBuilder =
        IdentifyBuilder(extractor as IdentifyExtractor, getValidator(extractor))

    override fun getMockExtractor(): IdentifyExtractor {
        val mockIdentifyExtractor = Mockito.mock(IdentifyExtractor::class.java)
        setMockDefaultExtractor(mockIdentifyExtractor)
        return mockIdentifyExtractor
    }

}
