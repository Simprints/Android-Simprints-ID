package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.clientrequests.extractors.EnrollExtractor
import com.simprints.clientapi.clientrequests.validators.EnrollValidator
import com.simprints.clientapi.domain.requests.BaseRequest
import com.simprints.clientapi.domain.requests.EnrollRequest


class EnrollBuilder(private val extractor: EnrollExtractor,
                    validator: EnrollValidator)
    : ClientRequestBuilder(validator) {

    override fun buildAppRequest(): BaseRequest = EnrollRequest(
        projectId = extractor.getProjectId(),
        userId = extractor.getUserId(),
        moduleId = extractor.getModuleId(),
        metadata = extractor.getMetadata(),
        unknownExtras = extractor.getUnknownExtras()
    )
}
