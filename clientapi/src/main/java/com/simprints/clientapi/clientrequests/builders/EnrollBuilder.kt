package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.clientrequests.extractors.EnrollExtractor
import com.simprints.clientapi.clientrequests.validators.EnrollValidator
import com.simprints.clientapi.domain.ClientBase
import com.simprints.clientapi.domain.requests.EnrollRequest
import com.simprints.clientapi.domain.requests.ExtraRequestInfo
import com.simprints.clientapi.domain.requests.IntegrationInfo


class EnrollBuilder(private val extractor: EnrollExtractor,
                    validator: EnrollValidator,
                    private val integrationInfo: IntegrationInfo)
    : ClientRequestBuilder(validator) {

    override fun buildAppRequest(): ClientBase = EnrollRequest(
        projectId = extractor.getProjectId(),
        userId = extractor.getUserId(),
        moduleId = extractor.getModuleId(),
        metadata = extractor.getMetatdata(),
        unknownExtras = extractor.getUnknownExtras(),
        extra = ExtraRequestInfo(integrationInfo)
    )
}
