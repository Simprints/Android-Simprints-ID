package com.simprints.clientapi.requestFactories

import com.simprints.clientapi.clientrequests.builders.ConfirmIdentifyBuilder
import com.simprints.clientapi.clientrequests.extractors.ClientRequestExtractor
import com.simprints.clientapi.clientrequests.extractors.ConfirmIdentityExtractor
import com.simprints.clientapi.clientrequests.validators.ConfirmIdentityValidator
import com.simprints.clientapi.controllers.core.eventData.model.IntegrationInfo
import com.simprints.clientapi.domain.requests.BaseRequest
import com.simprints.clientapi.domain.requests.ConfirmIdentityRequest
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.tokenization.TokenizationManager
import io.mockk.every
import io.mockk.mockk

object ConfirmIdentityFactory : RequestFactory() {

    override fun getValidSimprintsRequest(integrationInfo: IntegrationInfo): BaseRequest =
        ConfirmIdentityRequest(
            projectId = MOCK_PROJECT_ID,
            userId = MOCK_USER_ID,
            sessionId = MOCK_SESSION_ID,
            selectedGuid = MOCK_SELECTED_GUID,
            unknownExtras = emptyMap()
        )

    override fun getValidator(extractor: ClientRequestExtractor): ConfirmIdentityValidator =
        ConfirmIdentityValidator(extractor as ConfirmIdentityExtractor, MOCK_SESSION_ID, true)

    override fun getBuilder(extractor: ClientRequestExtractor): ConfirmIdentifyBuilder {
        val project = mockk<Project>()
        val tokenizationManager = mockk<TokenizationManager>()
        return ConfirmIdentifyBuilder(
            extractor = extractor as ConfirmIdentityExtractor,
            project = project,
            tokenizationManager = tokenizationManager,
            validator = getValidator(extractor)
        )
    }

    override fun getMockExtractor(): ConfirmIdentityExtractor {
        val mockConfirmIdentifyExtractor = mockk<ConfirmIdentityExtractor>()
        setMockDefaultExtractor(mockConfirmIdentifyExtractor)
        every { mockConfirmIdentifyExtractor.getSessionId() } returns MOCK_SESSION_ID
        every { mockConfirmIdentifyExtractor.getSelectedGuid() } returns MOCK_SELECTED_GUID
        return mockConfirmIdentifyExtractor
    }
}
