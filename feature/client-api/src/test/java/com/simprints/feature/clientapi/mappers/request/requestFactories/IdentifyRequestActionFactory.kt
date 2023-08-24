package com.simprints.feature.clientapi.mappers.request.requestFactories

import com.simprints.feature.clientapi.models.ActionRequest
import com.simprints.feature.clientapi.mappers.request.builders.IdentifyRequestBuilder
import com.simprints.feature.clientapi.mappers.request.extractors.IdentifyRequestExtractor
import com.simprints.feature.clientapi.mappers.request.extractors.ActionRequestExtractor
import com.simprints.feature.clientapi.mappers.request.validators.IdentifyValidator
import io.mockk.mockk

internal object IdentifyRequestActionFactory : RequestActionFactory() {

    override fun getValidSimprintsRequest() = ActionRequest.IdentifyActionRequest(
        packageName = MOCK_PACKAGE,
        projectId = MOCK_PROJECT_ID,
        moduleId = MOCK_MODULE_ID,
        userId = MOCK_USER_ID,
        metadata = MOCK_METADATA,
        unknownExtras = emptyMap()
    )

    override fun getValidator(extractor: ActionRequestExtractor): IdentifyValidator =
        IdentifyValidator(extractor as IdentifyRequestExtractor)

    override fun getBuilder(extractor: ActionRequestExtractor): IdentifyRequestBuilder =
        IdentifyRequestBuilder(MOCK_PACKAGE, extractor as IdentifyRequestExtractor, getValidator(extractor))

    override fun getMockExtractor(): IdentifyRequestExtractor {
        val mockIdentifyExtractor = mockk<IdentifyRequestExtractor>()
        setMockDefaultExtractor(mockIdentifyExtractor)
        return mockIdentifyExtractor
    }

}
