package com.simprints.clientapi.requestFactories

import com.simprints.clientapi.clientrequests.builders.VerifyBuilder
import com.simprints.clientapi.clientrequests.extractors.ClientRequestExtractor
import com.simprints.clientapi.clientrequests.extractors.VerifyExtractor
import com.simprints.clientapi.clientrequests.validators.VerifyValidator
import com.simprints.clientapi.controllers.core.eventData.model.IntegrationInfo
import com.simprints.clientapi.domain.requests.BaseRequest
import com.simprints.clientapi.domain.requests.VerifyRequest
import io.mockk.every
import io.mockk.mockk

object VerifyRequestFactory : RequestFactory() {

    override fun getValidSimprintsRequest(integrationInfo: IntegrationInfo): BaseRequest =
        VerifyRequest(
            projectId = MOCK_PROJECT_ID,
            moduleId = MOCK_MODULE_ID,
            userId = MOCK_USER_ID,
            metadata = MOCK_METADATA,
            verifyGuid = MOCK_VERIFY_GUID,
            unknownExtras = emptyMap()
        )

    override fun getBuilder(extractor: ClientRequestExtractor): VerifyBuilder =
        VerifyBuilder(extractor as VerifyExtractor, getValidator(extractor))

    override fun getValidator(extractor: ClientRequestExtractor): VerifyValidator =
        VerifyValidator(extractor as VerifyExtractor)

    override fun getMockExtractor(): VerifyExtractor {
        val mockVerifyExtractor = mockk<VerifyExtractor>()
        setMockDefaultExtractor(mockVerifyExtractor)
        every { mockVerifyExtractor.getVerifyGuid() } returns MOCK_VERIFY_GUID
        return mockVerifyExtractor
    }

}
