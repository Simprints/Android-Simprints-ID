package com.simprints.clientapi.requestFactories

import com.simprints.clientapi.clientrequests.builders.IdentifyBuilder
import com.simprints.clientapi.clientrequests.extractors.ClientRequestExtractor
import com.simprints.clientapi.clientrequests.extractors.IdentifyExtractor
import com.simprints.clientapi.clientrequests.validators.IdentifyValidator
import com.simprints.clientapi.controllers.core.eventData.model.IntegrationInfo
import com.simprints.clientapi.domain.requests.BaseRequest
import com.simprints.clientapi.domain.requests.IdentifyRequest
import com.simprints.infra.config.domain.models.Project
import com.simprints.infra.config.tokenization.TokenizationManager
import io.mockk.mockk

object IdentifyRequestFactory : RequestFactory() {

    override fun getValidSimprintsRequest(integrationInfo: IntegrationInfo): BaseRequest =
        IdentifyRequest(
            projectId = MOCK_PROJECT_ID,
            moduleId = MOCK_MODULE_ID,
            userId = MOCK_USER_ID,
            metadata = MOCK_METADATA,
            unknownExtras = emptyMap()
        )

    override fun getValidator(extractor: ClientRequestExtractor): IdentifyValidator =
        IdentifyValidator(extractor as IdentifyExtractor)

    override fun getBuilder(extractor: ClientRequestExtractor): IdentifyBuilder {
        val project = mockk<Project>()
        val tokenizationManager = mockk<TokenizationManager>()
        return IdentifyBuilder(
            extractor = extractor as IdentifyExtractor,
            project = project,
            tokenizationManager = tokenizationManager,
            validator = getValidator(extractor)
        )
    }

    override fun getMockExtractor(): IdentifyExtractor {
        val mockIdentifyExtractor = mockk<IdentifyExtractor>()
        setMockDefaultExtractor(mockIdentifyExtractor)
        return mockIdentifyExtractor
    }

}
