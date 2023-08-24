package com.simprints.feature.clientapi.mappers.request.requestFactories

import com.simprints.feature.clientapi.models.ActionRequest
import com.simprints.feature.clientapi.mappers.request.builders.EnrolRequestBuilder
import com.simprints.feature.clientapi.mappers.request.extractors.EnrolRequestExtractor
import com.simprints.feature.clientapi.mappers.request.extractors.ActionRequestExtractor
import com.simprints.feature.clientapi.mappers.request.validators.EnrolValidator
import io.mockk.mockk

internal object EnrolActionFactory : RequestActionFactory() {

    override fun getValidSimprintsRequest() = ActionRequest.EnrolActionRequest(
        packageName = MOCK_PACKAGE,
        projectId = MOCK_PROJECT_ID,
        userId = MOCK_USER_ID,
        metadata = MOCK_METADATA,
        moduleId = MOCK_MODULE_ID,
        unknownExtras = emptyMap()
    )

    override fun getBuilder(extractor: ActionRequestExtractor): EnrolRequestBuilder =
        EnrolRequestBuilder(MOCK_PACKAGE, extractor as EnrolRequestExtractor, getValidator(extractor))

    override fun getValidator(extractor: ActionRequestExtractor): EnrolValidator =
        EnrolValidator(extractor as EnrolRequestExtractor)

    override fun getMockExtractor(): EnrolRequestExtractor {
        val mockEnrolmentExtractor = mockk<EnrolRequestExtractor>()
        setMockDefaultExtractor(mockEnrolmentExtractor)
        return mockEnrolmentExtractor
    }
}
