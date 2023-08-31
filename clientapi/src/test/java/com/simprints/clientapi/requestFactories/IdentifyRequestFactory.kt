package com.simprints.clientapi.requestFactories

import com.simprints.clientapi.clientrequests.builders.EnrolLastBiometricsBuilder
import com.simprints.clientapi.clientrequests.builders.IdentifyBuilder
import com.simprints.clientapi.clientrequests.extractors.ClientRequestExtractor
import com.simprints.clientapi.clientrequests.extractors.EnrolLastBiometricsExtractor
import com.simprints.clientapi.clientrequests.extractors.IdentifyExtractor
import com.simprints.clientapi.clientrequests.validators.IdentifyValidator
import com.simprints.clientapi.controllers.core.eventData.model.IntegrationInfo
import com.simprints.clientapi.domain.requests.BaseRequest
import com.simprints.clientapi.domain.requests.IdentifyRequest
import com.simprints.core.tools.utils.Tokenization
import com.simprints.infra.config.domain.models.Project
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
        val tokenization = mockk<Tokenization>()
        return IdentifyBuilder(
            extractor = extractor as IdentifyExtractor,
            project = project,
            tokenization = tokenization,
            validator = getValidator(extractor)
        )
    }

    override fun getMockExtractor(): IdentifyExtractor {
        val mockIdentifyExtractor = mockk<IdentifyExtractor>()
        setMockDefaultExtractor(mockIdentifyExtractor)
        return mockIdentifyExtractor
    }

}
