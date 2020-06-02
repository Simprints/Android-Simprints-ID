package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.clientrequests.extractors.EnrolLastBiometricsExtractor
import com.simprints.clientapi.clientrequests.validators.EnrolLastBiometricsValidator
import com.simprints.clientapi.domain.requests.BaseRequest
import com.simprints.clientapi.domain.requests.EnrolLastBiometricsRequest


class EnrolLastBiometricsBuilder(private val extractor: EnrolLastBiometricsExtractor,
                                 validator: EnrolLastBiometricsValidator)
    : ClientRequestBuilder(validator) {

    override fun buildAppRequest(): BaseRequest = EnrolLastBiometricsRequest(
        projectId = extractor.getProjectId(),
        userId = extractor.getUserId(),
        moduleId = extractor.getModuleId(),
        metadata = extractor.getMetadata(),
        sessionId = extractor.getSessionId(),
        unknownExtras = extractor.getUnknownExtras()
    )
}

