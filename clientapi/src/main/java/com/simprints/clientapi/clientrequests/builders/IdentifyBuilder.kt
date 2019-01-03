package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.clientrequests.extractors.IdentifyExtractor
import com.simprints.clientapi.clientrequests.requests.ClientIdentifyRequest
import com.simprints.clientapi.clientrequests.requests.ClientRequest
import com.simprints.clientapi.clientrequests.requests.legacy.LegacyClientIdentifyRequest
import com.simprints.clientapi.clientrequests.validators.IdentifyValidator


class IdentifyBuilder(val extractor: IdentifyExtractor, validator: IdentifyValidator) :
    ClientRequestBuilder(extractor, validator) {

    override fun buildClientRequest(): ClientRequest = ClientIdentifyRequest(
        projectId = extractor.getProjectId()!!,
        userId = extractor.getUserId()!!,
        moduleId = extractor.getModuleId()!!,
        metadata = extractor.getMetatdata()
    )

    override fun buildLegacyClientRequest(): ClientRequest = LegacyClientIdentifyRequest(
        legacyApiKey = extractor.getLegacyApiKey()!!,
        userId = extractor.getUserId()!!,
        moduleId = extractor.getModuleId()!!,
        metadata = extractor.getMetatdata()
    )

}
