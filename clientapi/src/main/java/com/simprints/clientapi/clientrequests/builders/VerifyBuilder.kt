package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.clientrequests.extractors.VerifyExtractor
import com.simprints.clientapi.clientrequests.validators.VerifyValidator
import com.simprints.clientapi.simprintsrequests.requests.ClientApiBaseRequest
import com.simprints.clientapi.simprintsrequests.requests.ClientApiVerifyRequest


class VerifyBuilder(private val extractor: VerifyExtractor, validator: VerifyValidator)
    : ClientRequestBuilder(validator) {

    override fun buildAppRequest(): ClientApiBaseRequest = ClientApiVerifyRequest(
        projectId = extractor.getProjectId(),
        userId = extractor.getUserId(),
        moduleId = extractor.getModuleId(),
        metadata = extractor.getMetatdata(),
        verifyGuid = extractor.getVerifyGuid()
    )

}
