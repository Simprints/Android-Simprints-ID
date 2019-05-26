package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.clientrequests.extractors.VerifyExtractor
import com.simprints.clientapi.clientrequests.validators.VerifyValidator
import com.simprints.clientapi.domain.ClientBase
import com.simprints.clientapi.domain.requests.ExtraRequestInfo
import com.simprints.clientapi.domain.requests.IntegrationInfo
import com.simprints.clientapi.domain.requests.VerifyRequest


class VerifyBuilder(private val extractor: VerifyExtractor,
                    validator: VerifyValidator,
                    private val integrationInfo: IntegrationInfo)
    : ClientRequestBuilder(validator) {

    override fun buildAppRequest(): ClientBase = VerifyRequest(
        projectId = extractor.getProjectId(),
        userId = extractor.getUserId(),
        moduleId = extractor.getModuleId(),
        metadata = extractor.getMetatdata(),
        verifyGuid = extractor.getVerifyGuid(),
        unknownExtras = extractor.getUnknownExtras(),
        extra = ExtraRequestInfo(integrationInfo)
    )

}
