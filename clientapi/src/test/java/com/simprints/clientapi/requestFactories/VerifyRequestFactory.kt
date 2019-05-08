package com.simprints.clientapi.requestFactories

import com.simprints.clientapi.clientrequests.builders.VerifyBuilder
import com.simprints.clientapi.clientrequests.extractors.ClientRequestExtractor
import com.simprints.clientapi.clientrequests.extractors.VerifyExtractor
import com.simprints.clientapi.clientrequests.validators.VerifyValidator
import com.simprints.clientapi.domain.requests.BaseRequest
import com.simprints.clientapi.domain.requests.ExtraRequestInfo
import com.simprints.clientapi.domain.requests.VerifyRequest
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.whenever

object VerifyRequestFactory : RequestFactory() {

    override fun getValidSimprintsRequest(): BaseRequest = VerifyRequest(
        projectId = MOCK_PROJECT_ID,
        moduleId = MOCK_MODULE_ID,
        userId = MOCK_USER_ID,
        metadata = MOCK_METADATA,
        verifyGuid = MOCK_VERIFY_GUID,
        extra = ExtraRequestInfo(MOCK_INTEGRATION)
    )

    override fun getBuilder(extractor: ClientRequestExtractor): VerifyBuilder =
        VerifyBuilder(extractor as VerifyExtractor, getValidator(extractor), mock())

    override fun getValidator(extractor: ClientRequestExtractor): VerifyValidator =
        VerifyValidator(extractor as VerifyExtractor)

    override fun getMockExtractor(): VerifyExtractor {
        val mockVerifyExtractor = mock<VerifyExtractor>()
        setMockDefaultExtractor(mockVerifyExtractor)
        whenever(mockVerifyExtractor) { getVerifyGuid() } thenReturn RequestFactory.MOCK_VERIFY_GUID
        return mockVerifyExtractor
    }
}
