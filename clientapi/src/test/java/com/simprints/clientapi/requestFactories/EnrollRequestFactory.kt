package com.simprints.clientapi.requestFactories

import com.simprints.clientapi.clientrequests.builders.EnrollBuilder
import com.simprints.clientapi.clientrequests.extractors.ClientRequestExtractor
import com.simprints.clientapi.clientrequests.extractors.EnrollExtractor
import com.simprints.clientapi.clientrequests.validators.EnrollValidator
import com.simprints.clientapi.models.domain.requests.BaseRequest
import com.simprints.clientapi.models.domain.requests.EnrollRequest
import org.mockito.Mockito

object EnrollRequestFactory : RequestFactory() {

    override fun getValidSimprintsRequest(): BaseRequest = EnrollRequest(
        projectId = MOCK_PROJECT_ID,
        moduleId = MOCK_MODULE_ID,
        userId = MOCK_USER_ID,
        metadata = MOCK_METADATA
    )

    override fun getBuilder(extractor: ClientRequestExtractor): EnrollBuilder =
        EnrollBuilder(extractor as EnrollExtractor, getValidator(extractor))

    override fun getValidator(extractor: ClientRequestExtractor): EnrollValidator =
        EnrollValidator(extractor as EnrollExtractor)

    override fun getMockExtractor(): EnrollExtractor {
        val mockEnrollmentExtractor = Mockito.mock(EnrollExtractor::class.java)
        setMockDefaultExtractor(mockEnrollmentExtractor)
        return mockEnrollmentExtractor
    }


}
