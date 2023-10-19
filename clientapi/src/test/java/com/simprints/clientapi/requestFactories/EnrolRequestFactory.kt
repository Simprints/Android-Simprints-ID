package com.simprints.clientapi.requestFactories

import com.simprints.clientapi.clientrequests.builders.EnrolBuilder
import com.simprints.clientapi.clientrequests.extractors.ClientRequestExtractor
import com.simprints.clientapi.clientrequests.extractors.EnrolExtractor
import com.simprints.clientapi.clientrequests.validators.EnrolValidator
import com.simprints.clientapi.controllers.core.eventData.model.IntegrationInfo
import com.simprints.clientapi.domain.requests.BaseRequest
import com.simprints.clientapi.domain.requests.EnrolRequest
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.TokenKeyType
import io.mockk.every
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import io.mockk.mockk

object EnrolRequestFactory : RequestFactory() {

    override fun getValidSimprintsRequest(integrationInfo: IntegrationInfo): BaseRequest =
        EnrolRequest(
            projectId = MOCK_PROJECT_ID,
            userId = MOCK_USER_ID,
            metadata = MOCK_METADATA,
            moduleId = MOCK_MODULE_ID,
            unknownExtras = emptyMap()
        )

    override fun getBuilder(extractor: ClientRequestExtractor): EnrolBuilder {
        val project = mockk<Project>()
        val tokenizationProcessor = mockk<TokenizationProcessor> {
            every { encrypt(MOCK_USER_ID, TokenKeyType.AttendantId, project) } returns MOCK_USER_ID
            every { encrypt(MOCK_MODULE_ID, TokenKeyType.ModuleId, project) } returns MOCK_MODULE_ID
        }
        return EnrolBuilder(
            extractor = extractor as EnrolExtractor,
            project = project,
            tokenizationProcessor = tokenizationProcessor,
            validator = getValidator(extractor)
        )
    }

    override fun getValidator(extractor: ClientRequestExtractor): EnrolValidator =
        EnrolValidator(extractor as EnrolExtractor)

    override fun getMockExtractor(): EnrolExtractor {
        val mockEnrolmentExtractor = mockk<EnrolExtractor>()
        setMockDefaultExtractor(mockEnrolmentExtractor)
        return mockEnrolmentExtractor
    }
}
