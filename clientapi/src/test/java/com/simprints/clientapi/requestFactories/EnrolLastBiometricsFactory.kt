package com.simprints.clientapi.requestFactories

import com.simprints.clientapi.clientrequests.builders.EnrolLastBiometricsBuilder
import com.simprints.clientapi.clientrequests.extractors.ClientRequestExtractor
import com.simprints.clientapi.clientrequests.extractors.EnrolLastBiometricsExtractor
import com.simprints.clientapi.clientrequests.validators.EnrolLastBiometricsValidator
import com.simprints.clientapi.controllers.core.eventData.model.IntegrationInfo
import com.simprints.clientapi.domain.requests.BaseRequest
import com.simprints.clientapi.domain.requests.EnrolLastBiometricsRequest
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.sync.tokenization.TokenizationManager
import io.mockk.every
import io.mockk.mockk

object EnrolLastBiometricsFactory : RequestFactory() {

    override fun getValidSimprintsRequest(integrationInfo: IntegrationInfo): BaseRequest =
        EnrolLastBiometricsRequest(
            projectId = MOCK_PROJECT_ID,
            userId = MOCK_USER_ID,
            moduleId = MOCK_MODULE_ID,
            metadata = MOCK_METADATA,
            sessionId = MOCK_SESSION_ID,
            unknownExtras = emptyMap()
        )

    override fun getValidator(extractor: ClientRequestExtractor): EnrolLastBiometricsValidator =
        EnrolLastBiometricsValidator(
            extractor as EnrolLastBiometricsExtractor,
            MOCK_SESSION_ID,
            true
        )

    override fun getBuilder(extractor: ClientRequestExtractor): EnrolLastBiometricsBuilder {
        val project = mockk<Project>()
        val tokenizationManager = mockk<TokenizationManager>()
        return EnrolLastBiometricsBuilder(
            extractor = extractor as EnrolLastBiometricsExtractor,
            project = project,
            tokenizationManager = tokenizationManager,
            validator = getValidator(extractor)
        )
    }

    override fun getMockExtractor(): EnrolLastBiometricsExtractor {
        val mockEnrolLastBiometricsExtractor = mockk<EnrolLastBiometricsExtractor>()
        setMockDefaultExtractor(mockEnrolLastBiometricsExtractor)
        every { mockEnrolLastBiometricsExtractor.getProjectId() } returns MOCK_PROJECT_ID
        every { mockEnrolLastBiometricsExtractor.getUserId() } returns MOCK_USER_ID.value
        every { mockEnrolLastBiometricsExtractor.getModuleId() } returns MOCK_MODULE_ID.value
        every { mockEnrolLastBiometricsExtractor.getMetadata() } returns MOCK_METADATA
        every { mockEnrolLastBiometricsExtractor.getSessionId() } returns MOCK_SESSION_ID
        every { mockEnrolLastBiometricsExtractor.getUnknownExtras() } returns emptyMap()
        return mockEnrolLastBiometricsExtractor
    }
}
