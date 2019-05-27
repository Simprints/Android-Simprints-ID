package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.clientrequests.extractors.IdentifyExtractor
import com.simprints.clientapi.clientrequests.validators.IdentifyValidator
import com.simprints.clientapi.domain.ClientBase
import com.simprints.clientapi.domain.requests.IdentifyRequest


class IdentifyBuilder(val extractor: IdentifyExtractor,
                      validator: IdentifyValidator) :
    ClientRequestBuilder(validator) {

    override fun buildAppRequest(): ClientBase = IdentifyRequest(
        projectId = extractor.getProjectId(),
        userId = extractor.getUserId(),
        moduleId = extractor.getModuleId(),
        metadata = extractor.getMetadata(),
        unknownExtras = extractor.getUnknownExtras()
    )
}
