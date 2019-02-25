package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.clientrequests.extractors.IdentifyExtractor
import com.simprints.clientapi.clientrequests.validators.IdentifyValidator
import com.simprints.clientapi.simprintsrequests.requests.ClientApiIdentifyRequest
import com.simprints.clientapi.simprintsrequests.requests.ClientApiBaseRequest
import com.simprints.clientapi.simprintsrequests.requests.legacy.LegacyIdentifyRequest
import com.simprints.clientapi.simprintsrequests.requests.legacy.LegacySimprintsIdRequest


class IdentifyBuilder(val extractor: IdentifyExtractor, validator: IdentifyValidator) :
    ClientRequestBuilder(extractor, validator) {

    override fun buildSimprintsRequest(): ClientApiBaseRequest = ClientApiIdentifyRequest(
        projectId = extractor.getProjectId(),
        userId = extractor.getUserId(),
        moduleId = extractor.getModuleId(),
        metadata = extractor.getMetatdata()
    )

    override fun buildLegacySimprintsRequest(): LegacySimprintsIdRequest = LegacyIdentifyRequest(
        legacyApiKey = extractor.getLegacyApiKey(),
        userId = extractor.getUserId(),
        moduleId = extractor.getModuleId(),
        metadata = extractor.getMetatdata()
    )

}
