package com.simprints.clientapi.requestFactories

import com.simprints.clientapi.clientrequests.builders.EnrollBuilder
import com.simprints.clientapi.clientrequests.extractors.ClientRequestExtractor
import com.simprints.clientapi.clientrequests.extractors.EnrollExtractor
import com.simprints.clientapi.clientrequests.validators.EnrollValidator
import com.simprints.clientapi.controllers.core.eventData.model.IntegrationInfo
import com.simprints.clientapi.domain.requests.BaseRequest
import com.simprints.clientapi.domain.requests.EnrollRequest
import io.mockk.mockk

object EnrollRequestFactory : RequestFactory() {

    override fun getValidSimprintsRequest(integrationInfo: IntegrationInfo): BaseRequest =
        EnrollRequest(
            projectId = MOCK_PROJECT_ID,
            moduleId = MOCK_MODULE_ID,
            userId = MOCK_USER_ID,
            metadata = MOCK_METADATA,
            unknownExtras = emptyMap()
        )

    override fun getBuilder(extractor: ClientRequestExtractor): EnrollBuilder =
        EnrollBuilder(extractor as EnrollExtractor, getValidator(extractor))

    override fun getValidator(extractor: ClientRequestExtractor): EnrollValidator =
        EnrollValidator(extractor as EnrollExtractor)

    override fun getMockExtractor(): EnrollExtractor {
        val mockEnrollmentExtractor = mockk<EnrollExtractor>()
        setMockDefaultExtractor(mockEnrollmentExtractor)
        return mockEnrollmentExtractor
    }
}
