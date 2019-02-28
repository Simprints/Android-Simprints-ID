package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.clientrequests.extractors.EnrollExtractor
import com.simprints.clientapi.clientrequests.validators.EnrollValidator
import com.simprints.clientapi.simprintsrequests.requests.ClientApiBaseRequest
import com.simprints.clientapi.simprintsrequests.requests.ClientApiEnrollRequest


class EnrollBuilder(private val extractor: EnrollExtractor, validator: EnrollValidator)
    : ClientRequestBuilder(validator) {

    override fun buildAppRequest(): ClientApiBaseRequest = ClientApiEnrollRequest(
        projectId = extractor.getProjectId(),
        userId = extractor.getUserId(),
        moduleId = extractor.getModuleId(),
        metadata = extractor.getMetatdata()
    )

}
